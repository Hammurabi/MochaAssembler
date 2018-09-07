package com.riverssen.core.mpp.compiler;

import com.riverssen.core.mpp.Executable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class AbstractSyntaxTree
{
    private Struct self;
    private Method context;
    private Executable exe;
    private Opcode     ops;
    private long       lcllvts;
    private String     lastOnStack;
    private class var{
        String name, type;
        Set<Modifier> modifiers;
//        long stacklocation;
        long localvarlocation;
        boolean isField;

        public var(String name, String type, Set<Modifier> modifiers)
        {
            this.name = name;
            this.type = type;
            this.modifiers = modifiers;
            this.localvarlocation = lcllvts ++;
        }

        public var(String name, String type, Set<Modifier> modifiers, long localvarlocation, boolean isField)
        {
            this.name = name;
            this.type = type;
            this.modifiers = modifiers;
            this.localvarlocation = localvarlocation;
            this.isField = isField;
        }
    }
    private Map<String, var> localVariableTable;

    public AbstractSyntaxTree(Token child, Method method, GlobalSpace space)
    {
        localVariableTable = new HashMap<>();
        precedence         = new HashMap<>();

        precedence.put("char", (byte)0);
        precedence.put("uchar", (byte)1);
        precedence.put("byte", (byte)0);
        precedence.put("ubyte", (byte)1);

        precedence.put("short", (byte)1);
        precedence.put("ushort", (byte)2);

        precedence.put("int", (byte)2);
        precedence.put("uint", (byte)3);

        precedence.put("long", (byte)3);
        precedence.put("ulong", (byte)4);

        precedence.put("int128", (byte)4);
        precedence.put("uint128", (byte)5);

        precedence.put("int256", (byte)6);
        precedence.put("uint256", (byte)7);

        precedence.put("float", (byte)2);
        precedence.put("double", (byte)3);

        precedence.put("float128", (byte)3);
        precedence.put("float256", (byte)4);

        if (method.getParent() == null || method.getParent().getName().equals("VOID") || method.getParent().getName().equals("void"))
            self = null;
        else {
            localVariableTable.put("this", new var("this", method.getParent().getName(), new LinkedHashSet<>()));
            self = method.getParent();
        }

        context = method;
        exe = new Executable();
        ops = new Opcode( method.getName() + "()");

        for (Field field : method.getArguments())
            localVariableTable.put(field.getName(), new var(field.getName(), field.getTypeName(), field.getModifiers()));

        lastOnStack = "";

        compile(child, ops);
    }

    private Opcode loadVariable(String variable, Opcode ops, CompileType compileType)
    {
        Opcode op = null;
        var v = getLocalVariable(variable);

        setLastOnStack(v.type);

        switch (compileType)
        {
            case UNARY_P:
            if (v.isField)
            {
                switch (v.type)
                {
                    case "ubyte":
                    case "uchar":
                    case "byte":
                    case "char":
                        op = new Opcode(Ops.bmov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.binc)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptb).add(Opcode.convertLong(v.localvarlocation))); break;
                    case "short":
                    case "ushort":
                        op = new Opcode(Ops.smov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.sinc)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.pts).add(Opcode.convertLong(v.localvarlocation))); break;
                    case "int":
                    case "uint":
                        op = new Opcode(Ops.imov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.iinc)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.pti).add(Opcode.convertLong(v.localvarlocation))); break;
                    case "long":
                    case "ulong":
                        op = new Opcode(Ops.lmov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.linc)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptl).add(Opcode.convertLong(v.localvarlocation))); break;
                    case "int128":
                    case "uint128":
                        op = new Opcode(Ops.limov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.liinc)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptli).add(Opcode.convertLong(v.localvarlocation))); break;
                    case "int256":
                    case "uint256":
                        op = new Opcode(Ops.llmov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.llinc)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptll).add(Opcode.convertLong(v.localvarlocation))); break;
                    case "float":
                        op = new Opcode(Ops.fmov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.finc)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptf).add(Opcode.convertLong(v.localvarlocation))); break;
                    case "double":
                        op = new Opcode(Ops.dmov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.dinc)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptd).add(Opcode.convertLong(v.localvarlocation))); break;
                    case "float128":
                        op = new Opcode(Ops.dfmov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.dfinc)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptdf).add(Opcode.convertLong(v.localvarlocation))); break;
                    case "float256":
                        op = new Opcode(Ops.ddmov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.ddinc)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptdd).add(Opcode.convertLong(v.localvarlocation))); break;
                    case "String":
                    case "string":
                        svrerr("cannot increment a string.");
                    default:
                        unsvrerr("incrementing a pointer");
                        op = new Opcode(Ops.amov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.linc)); break;
                }

                op = new Opcode(Ops.ebp).add(op);
            } else {
                switch (v.type)
                {
                    case "ubyte":
                    case "uchar":
                    case "byte":
                    case "char":
                        switch ((int) v.localvarlocation)
                        {
                            case 0: op = new Opcode(Ops.bload_0).add(new Opcode(Ops.binc)).add(new Opcode(Ops.bstore_0)); break;
                            case 1: op = new Opcode(Ops.bload_1).add(new Opcode(Ops.binc)).add(new Opcode(Ops.bstore_1)); break;
                            case 2: op = new Opcode(Ops.bload_2).add(new Opcode(Ops.binc)).add(new Opcode(Ops.bstore_2)); break;
                            case 3: op = new Opcode(Ops.bload_3).add(new Opcode(Ops.binc)).add(new Opcode(Ops.bstore_3)); break;
                            default: op = new Opcode(Ops.bload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.binc)).add(new Opcode(Ops.bstore).add(Opcode.convertLong(v.localvarlocation))); break;
                        } break;
                    case "ushort":
                    case "short":
                        switch ((int) v.localvarlocation)
                        {
                            case 0: op =    new Opcode(Ops.sload_0).add(new Opcode(Ops.sinc)).add(new Opcode(Ops.sstore_0)); break;
                            case 1: op =    new Opcode(Ops.sload_1).add(new Opcode(Ops.sinc)).add(new Opcode(Ops.sstore_1)); break;
                            case 2: op =    new Opcode(Ops.sload_2).add(new Opcode(Ops.sinc)).add(new Opcode(Ops.sstore_2)); break;
                            case 3: op =    new Opcode(Ops.sload_3).add(new Opcode(Ops.sinc)).add(new Opcode(Ops.sstore_3)); break;
                            default: op =   new Opcode(Ops.sload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.sinc)).add(new Opcode(Ops.sstore).add(Opcode.convertLong(v.localvarlocation))); break;
                        } break;
                    case "uint":
                    case "int":
                        switch ((int) v.localvarlocation)
                        {
                            case 0: op =    new Opcode(Ops.iload_0).add(new Opcode(Ops.iinc)).add(new Opcode(Ops.istore_0)); break;
                            case 1: op =    new Opcode(Ops.iload_1).add(new Opcode(Ops.iinc)).add(new Opcode(Ops.istore_1)); break;
                            case 2: op =    new Opcode(Ops.iload_2).add(new Opcode(Ops.iinc)).add(new Opcode(Ops.istore_2)); break;
                            case 3: op =    new Opcode(Ops.iload_3).add(new Opcode(Ops.iinc)).add(new Opcode(Ops.istore_3)); break;
                            default: op =   new Opcode(Ops.iload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.iinc)).add(new Opcode(Ops.istore).add(Opcode.convertLong(v.localvarlocation))); break;
                        } break;
                    case "ulong":
                    case "long":
                        switch ((int) v.localvarlocation)
                        {
                            case 0: op =    new Opcode(Ops.lload_0).add(new Opcode(Ops.linc)).add(new Opcode(Ops.lstore_0)); break;
                            case 1: op =    new Opcode(Ops.lload_1).add(new Opcode(Ops.linc)).add(new Opcode(Ops.lstore_1)); break;
                            case 2: op =    new Opcode(Ops.lload_2).add(new Opcode(Ops.linc)).add(new Opcode(Ops.lstore_2)); break;
                            case 3: op =    new Opcode(Ops.lload_3).add(new Opcode(Ops.linc)).add(new Opcode(Ops.lstore_3)); break;
                            default: op =   new Opcode(Ops.lload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.linc)).add(new Opcode(Ops.lstore).add(Opcode.convertLong(v.localvarlocation))); break;
                        } break;
                    case "uint128":
                    case "int128":
                        switch ((int) v.localvarlocation)
                        {
                            case 0: op =    new Opcode(Ops.liload_0).add(new Opcode(Ops.liinc)).add(new Opcode(Ops.listore_0)); break;
                            case 1: op =    new Opcode(Ops.liload_1).add(new Opcode(Ops.liinc)).add(new Opcode(Ops.listore_1)); break;
                            case 2: op =    new Opcode(Ops.liload_2).add(new Opcode(Ops.liinc)).add(new Opcode(Ops.listore_2)); break;
                            case 3: op =    new Opcode(Ops.liload_3).add(new Opcode(Ops.liinc)).add(new Opcode(Ops.listore_3)); break;
                            default: op =   new Opcode(Ops.liload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.liinc)).add(new Opcode(Ops.listore).add(Opcode.convertLong(v.localvarlocation))); break;
                        }
                    case "uint256":
                    case "int256":
                        switch ((int) v.localvarlocation)
                        {
                            case 0: op =    new Opcode(Ops.llload_0).add(new Opcode(Ops.llinc)).add(new Opcode(Ops.llstore_0)); break;
                            case 1: op =    new Opcode(Ops.llload_1).add(new Opcode(Ops.llinc)).add(new Opcode(Ops.llstore_1)); break;
                            case 2: op =    new Opcode(Ops.llload_2).add(new Opcode(Ops.llinc)).add(new Opcode(Ops.llstore_2)); break;
                            case 3: op =    new Opcode(Ops.llload_3).add(new Opcode(Ops.llinc)).add(new Opcode(Ops.llstore_3)); break;
                            default: op =   new Opcode(Ops.llload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.llinc)).add(new Opcode(Ops.llstore).add(Opcode.convertLong(v.localvarlocation))); break;
                        } break;
                    case "float":
                        switch ((int) v.localvarlocation)
                        {
                            case 0: op =    new Opcode(Ops.fload_0).add(new Opcode(Ops.finc)).add(new Opcode(Ops.fstore_0)); break;
                            case 1: op =    new Opcode(Ops.fload_1).add(new Opcode(Ops.finc)).add(new Opcode(Ops.fstore_1)); break;
                            case 2: op =    new Opcode(Ops.fload_2).add(new Opcode(Ops.finc)).add(new Opcode(Ops.fstore_2)); break;
                            case 3: op =    new Opcode(Ops.fload_3).add(new Opcode(Ops.finc)).add(new Opcode(Ops.fstore_3)); break;
                            default: op =   new Opcode(Ops.fload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.finc)).add(new Opcode(Ops.fstore).add(Opcode.convertLong(v.localvarlocation))); break;
                        } break;
                    case "double":
                        switch ((int) v.localvarlocation)
                        {
                            case 0: op =    new Opcode(Ops.dload_0).add(new Opcode(Ops.dinc)).add(new Opcode(Ops.dstore_0)); break;
                            case 1: op =    new Opcode(Ops.dload_1).add(new Opcode(Ops.dinc)).add(new Opcode(Ops.dstore_1)); break;
                            case 2: op =    new Opcode(Ops.dload_2).add(new Opcode(Ops.dinc)).add(new Opcode(Ops.dstore_2)); break;
                            case 3: op =    new Opcode(Ops.dload_3).add(new Opcode(Ops.dinc)).add(new Opcode(Ops.dstore_3)); break;
                            default: op =   new Opcode(Ops.dload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.dinc)).add(new Opcode(Ops.dstore).add(Opcode.convertLong(v.localvarlocation))); break;
                        } break;
                    case "float128":
                        switch ((int) v.localvarlocation)
                        {
                            case 0: op =    new Opcode(Ops.dfload_0).add(new Opcode(Ops.dfinc)).add(new Opcode(Ops.dfstore_0)); break;
                            case 1: op =    new Opcode(Ops.dfload_1).add(new Opcode(Ops.dfinc)).add(new Opcode(Ops.dfstore_1)); break;
                            case 2: op =    new Opcode(Ops.dfload_2).add(new Opcode(Ops.dfinc)).add(new Opcode(Ops.dfstore_2)); break;
                            case 3: op =    new Opcode(Ops.dfload_3).add(new Opcode(Ops.dfinc)).add(new Opcode(Ops.dfstore_3)); break;
                            default: op =   new Opcode(Ops.dfload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.dfinc)).add(new Opcode(Ops.dfstore).add(Opcode.convertLong(v.localvarlocation))); break;
                        } break;
                    case "float256":
                        switch ((int) v.localvarlocation)
                        {
                            case 0: op =    new Opcode(Ops.ddload_0).add(new Opcode(Ops.ddinc)).add(new Opcode(Ops.ddstore_0)); break;
                            case 1: op =    new Opcode(Ops.ddload_1).add(new Opcode(Ops.ddinc)).add(new Opcode(Ops.ddstore_1)); break;
                            case 2: op =    new Opcode(Ops.ddload_2).add(new Opcode(Ops.ddinc)).add(new Opcode(Ops.ddstore_2)); break;
                            case 3: op =    new Opcode(Ops.ddload_3).add(new Opcode(Ops.ddinc)).add(new Opcode(Ops.ddstore_3)); break;
                            default: op =   new Opcode(Ops.ddload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.ddinc)).add(new Opcode(Ops.ddstore).add(Opcode.convertLong(v.localvarlocation))); break;
                        } break;
                    case "String":
                    case "string":
                        svrerr("cannot increment a string.");
                    default:
                        unsvrerr("incrementing a pointer.");
                        switch ((int) v.localvarlocation)
                        {
                            case 0: op =    new Opcode(Ops.aload_0).add(new Opcode(Ops.linc)).add(new Opcode(Ops.astore_0)); break;
                            case 1: op =    new Opcode(Ops.aload_1).add(new Opcode(Ops.linc)).add(new Opcode(Ops.astore_1)); break;
                            case 2: op =    new Opcode(Ops.aload_2).add(new Opcode(Ops.linc)).add(new Opcode(Ops.astore_2)); break;
                            case 3: op =    new Opcode(Ops.aload_3).add(new Opcode(Ops.linc)).add(new Opcode(Ops.astore_3)); break;
                            default: op =   new Opcode(Ops.aload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.linc)).add(new Opcode(Ops.astore).add(Opcode.convertLong(v.localvarlocation))); break;
                        } break;
                }
            }
            break;
            case UNARY_M:
                if (v.isField)
                {
                    switch (v.type)
                    {
                        case "ubyte":
                        case "uchar":
                        case "byte":
                        case "char":
                            op = new Opcode(Ops.bmov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.bdec)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptb).add(Opcode.convertLong(v.localvarlocation))); break;
                        case "short":
                        case "ushort":
                            op = new Opcode(Ops.smov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.sdec)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.pts).add(Opcode.convertLong(v.localvarlocation))); break;
                        case "int":
                        case "uint":
                            op = new Opcode(Ops.imov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.idec)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.pti).add(Opcode.convertLong(v.localvarlocation))); break;
                        case "long":
                        case "ulong":
                            op = new Opcode(Ops.lmov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.ldec)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptl).add(Opcode.convertLong(v.localvarlocation))); break;
                        case "int128":
                        case "uint128":
                            op = new Opcode(Ops.limov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.lidec)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptli).add(Opcode.convertLong(v.localvarlocation))); break;
                        case "int256":
                        case "uint256":
                            op = new Opcode(Ops.llmov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.lldec)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptll).add(Opcode.convertLong(v.localvarlocation))); break;
                        case "float":
                            op = new Opcode(Ops.fmov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.fdec)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptf).add(Opcode.convertLong(v.localvarlocation))); break;
                        case "double":
                            op = new Opcode(Ops.dmov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.ddec)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptd).add(Opcode.convertLong(v.localvarlocation))); break;
                        case "float128":
                            op = new Opcode(Ops.dfmov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.dfdec)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptdf).add(Opcode.convertLong(v.localvarlocation))); break;
                        case "float256":
                            op = new Opcode(Ops.ddmov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.dddec)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptdd).add(Opcode.convertLong(v.localvarlocation))); break;
                        case "String":
                        case "string":
                            svrerr("cannot decrement a string.");
                        default:
                            unsvrerr("decrementing a pointer");
                            op = new Opcode(Ops.amov).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.ldec)); break;
                    }

                    op = new Opcode(Ops.ebp).add(op);
                } else {
                    switch (v.type)
                    {
                        case "ubyte":
                        case "uchar":
                        case "byte":
                        case "char":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op = new Opcode(Ops.bload_0).add(new Opcode(Ops.bdec)).add(new Opcode(Ops.bstore_0)); break;
                                case 1: op = new Opcode(Ops.bload_1).add(new Opcode(Ops.bdec)).add(new Opcode(Ops.bstore_1)); break;
                                case 2: op = new Opcode(Ops.bload_2).add(new Opcode(Ops.bdec)).add(new Opcode(Ops.bstore_2)); break;
                                case 3: op = new Opcode(Ops.bload_3).add(new Opcode(Ops.bdec)).add(new Opcode(Ops.bstore_3)); break;
                                default: op = new Opcode(Ops.bload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.bdec)).add(new Opcode(Ops.bstore).add(Opcode.convertLong(v.localvarlocation))); break;
                            } break;
                        case "ushort":
                        case "short":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.sload_0).add(new Opcode(Ops.sdec)).add(new Opcode(Ops.sstore_0)); break;
                                case 1: op =    new Opcode(Ops.sload_1).add(new Opcode(Ops.sdec)).add(new Opcode(Ops.sstore_1)); break;
                                case 2: op =    new Opcode(Ops.sload_2).add(new Opcode(Ops.sdec)).add(new Opcode(Ops.sstore_2)); break;
                                case 3: op =    new Opcode(Ops.sload_3).add(new Opcode(Ops.sdec)).add(new Opcode(Ops.sstore_3)); break;
                                default: op =   new Opcode(Ops.sload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.sdec)).add(new Opcode(Ops.sstore).add(Opcode.convertLong(v.localvarlocation))); break;
                            } break;
                        case "uint":
                        case "int":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.iload_0).add(new Opcode(Ops.idec)).add(new Opcode(Ops.istore_0)); break;
                                case 1: op =    new Opcode(Ops.iload_1).add(new Opcode(Ops.idec)).add(new Opcode(Ops.istore_1)); break;
                                case 2: op =    new Opcode(Ops.iload_2).add(new Opcode(Ops.idec)).add(new Opcode(Ops.istore_2)); break;
                                case 3: op =    new Opcode(Ops.iload_3).add(new Opcode(Ops.idec)).add(new Opcode(Ops.istore_3)); break;
                                default: op =   new Opcode(Ops.iload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.idec)).add(new Opcode(Ops.istore).add(Opcode.convertLong(v.localvarlocation))); break;
                            } break;
                        case "ulong":
                        case "long":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.lload_0).add(new Opcode(Ops.ldec)).add(new Opcode(Ops.lstore_0)); break;
                                case 1: op =    new Opcode(Ops.lload_1).add(new Opcode(Ops.ldec)).add(new Opcode(Ops.lstore_1)); break;
                                case 2: op =    new Opcode(Ops.lload_2).add(new Opcode(Ops.ldec)).add(new Opcode(Ops.lstore_2)); break;
                                case 3: op =    new Opcode(Ops.lload_3).add(new Opcode(Ops.ldec)).add(new Opcode(Ops.lstore_3)); break;
                                default: op =   new Opcode(Ops.lload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.ldec)).add(new Opcode(Ops.lstore).add(Opcode.convertLong(v.localvarlocation))); break;
                            } break;
                        case "uint128":
                        case "int128":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.liload_0).add(new Opcode(Ops.lidec)).add(new Opcode(Ops.listore_0)); break;
                                case 1: op =    new Opcode(Ops.liload_1).add(new Opcode(Ops.lidec)).add(new Opcode(Ops.listore_1)); break;
                                case 2: op =    new Opcode(Ops.liload_2).add(new Opcode(Ops.lidec)).add(new Opcode(Ops.listore_2)); break;
                                case 3: op =    new Opcode(Ops.liload_3).add(new Opcode(Ops.lidec)).add(new Opcode(Ops.listore_3)); break;
                                default: op =   new Opcode(Ops.liload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.lidec)).add(new Opcode(Ops.listore).add(Opcode.convertLong(v.localvarlocation))); break;
                            }
                        case "uint256":
                        case "int256":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.llload_0).add(new Opcode(Ops.lldec)).add(new Opcode(Ops.llstore_0)); break;
                                case 1: op =    new Opcode(Ops.llload_1).add(new Opcode(Ops.lldec)).add(new Opcode(Ops.llstore_1)); break;
                                case 2: op =    new Opcode(Ops.llload_2).add(new Opcode(Ops.lldec)).add(new Opcode(Ops.llstore_2)); break;
                                case 3: op =    new Opcode(Ops.llload_3).add(new Opcode(Ops.lldec)).add(new Opcode(Ops.llstore_3)); break;
                                default: op =   new Opcode(Ops.llload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.lldec)).add(new Opcode(Ops.llstore).add(Opcode.convertLong(v.localvarlocation))); break;
                            } break;
                        case "float":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.fload_0).add(new Opcode(Ops.fdec)).add(new Opcode(Ops.fstore_0)); break;
                                case 1: op =    new Opcode(Ops.fload_1).add(new Opcode(Ops.fdec)).add(new Opcode(Ops.fstore_1)); break;
                                case 2: op =    new Opcode(Ops.fload_2).add(new Opcode(Ops.fdec)).add(new Opcode(Ops.fstore_2)); break;
                                case 3: op =    new Opcode(Ops.fload_3).add(new Opcode(Ops.fdec)).add(new Opcode(Ops.fstore_3)); break;
                                default: op =   new Opcode(Ops.fload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.fdec)).add(new Opcode(Ops.fstore).add(Opcode.convertLong(v.localvarlocation))); break;
                            } break;
                        case "double":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.dload_0).add(new Opcode(Ops.ddec)).add(new Opcode(Ops.dstore_0)); break;
                                case 1: op =    new Opcode(Ops.dload_1).add(new Opcode(Ops.ddec)).add(new Opcode(Ops.dstore_1)); break;
                                case 2: op =    new Opcode(Ops.dload_2).add(new Opcode(Ops.ddec)).add(new Opcode(Ops.dstore_2)); break;
                                case 3: op =    new Opcode(Ops.dload_3).add(new Opcode(Ops.ddec)).add(new Opcode(Ops.dstore_3)); break;
                                default: op =   new Opcode(Ops.dload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.ddec)).add(new Opcode(Ops.dstore).add(Opcode.convertLong(v.localvarlocation))); break;
                            } break;
                        case "float128":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.dfload_0).add(new Opcode(Ops.dfdec)).add(new Opcode(Ops.dfstore_0)); break;
                                case 1: op =    new Opcode(Ops.dfload_1).add(new Opcode(Ops.dfdec)).add(new Opcode(Ops.dfstore_1)); break;
                                case 2: op =    new Opcode(Ops.dfload_2).add(new Opcode(Ops.dfdec)).add(new Opcode(Ops.dfstore_2)); break;
                                case 3: op =    new Opcode(Ops.dfload_3).add(new Opcode(Ops.dfdec)).add(new Opcode(Ops.dfstore_3)); break;
                                default: op =   new Opcode(Ops.dfload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.dfdec)).add(new Opcode(Ops.dfstore).add(Opcode.convertLong(v.localvarlocation))); break;
                            } break;
                        case "float256":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.ddload_0).add(new Opcode(Ops.dddec)).add(new Opcode(Ops.ddstore_0)); break;
                                case 1: op =    new Opcode(Ops.ddload_1).add(new Opcode(Ops.dddec)).add(new Opcode(Ops.ddstore_1)); break;
                                case 2: op =    new Opcode(Ops.ddload_2).add(new Opcode(Ops.dddec)).add(new Opcode(Ops.ddstore_2)); break;
                                case 3: op =    new Opcode(Ops.ddload_3).add(new Opcode(Ops.dddec)).add(new Opcode(Ops.ddstore_3)); break;
                                default: op =   new Opcode(Ops.ddload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.dddec)).add(new Opcode(Ops.ddstore).add(Opcode.convertLong(v.localvarlocation))); break;
                            } break;
                        case "String":
                        case "string":
                            svrerr("cannot decrement a string.");
                        default:
                            unsvrerr("decrementing a pointer.");
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.aload_0).add(new Opcode(Ops.ldec)).add(new Opcode(Ops.astore_0)); break;
                                case 1: op =    new Opcode(Ops.aload_1).add(new Opcode(Ops.ldec)).add(new Opcode(Ops.astore_1)); break;
                                case 2: op =    new Opcode(Ops.aload_2).add(new Opcode(Ops.ldec)).add(new Opcode(Ops.astore_2)); break;
                                case 3: op =    new Opcode(Ops.aload_3).add(new Opcode(Ops.ldec)).add(new Opcode(Ops.astore_3)); break;
                                default: op =   new Opcode(Ops.aload).add(Opcode.convertLong(v.localvarlocation)).add(new Opcode(Ops.ldec)).add(new Opcode(Ops.astore).add(Opcode.convertLong(v.localvarlocation))); break;
                            } break;
                    }
                }
                break;
            case FOR:
            case STRAIGHT_TO_REGISTER:
                if (v.isField)
                {
                    switch (v.type)
                    {
                        case "ubyte":
                        case "uchar":
                            op = new Opcode(Ops.bmovrld_u).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "byte":
                        case "char":
                            op = new Opcode(Ops.bmovrld).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "short":
                            op = new Opcode(Ops.smovrld).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "ushort":
                            op = new Opcode(Ops.smovrld_u).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "uint":
                            op = new Opcode(Ops.imovrld_u).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "int":
                            op = new Opcode(Ops.imovrld).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "ulong":
                            op = new Opcode(Ops.lmovrld_u).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "long":
                            op = new Opcode(Ops.lmovrld).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "uint128":
                            op = new Opcode(Ops.limovrld_u).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "int128":
                            op = new Opcode(Ops.limovrld).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "uint256":
                            op = new Opcode(Ops.llmovrld_u).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "int256":
                            op = new Opcode(Ops.llmovrld).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "float":
                            op = new Opcode(Ops.fmovrld).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "double":
                            op = new Opcode(Ops.dmovrld).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "float128":
                            op = new Opcode(Ops.dfmovrld).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "float256":
                            op = new Opcode(Ops.ddmovrld).add(Opcode.convertLong(v.localvarlocation)); break;
                        case "String":
                        case "string":
//                            op = new Opcode(Ops.csmovrld); break;
                            svrerr("compiler error: cannot move 'string' to ALU '" + v.name + "'.");
                            break;
                        default:
                            svrerr("compiler error: cannot move 'string' to ALU '" + v.name + "'.");
                    }

                    op = new Opcode(Ops.ebp).add(op);
                } else {
                    switch (v.type)
                    {
                        case "ubyte":
                        case "uchar":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op = new Opcode(Ops.bregld_0_u); break;
                                case 1: op = new Opcode(Ops.bregld_1_u); break;
                                case 2: op = new Opcode(Ops.bregld_2_u); break;
                                case 3: op = new Opcode(Ops.bregld_3_u); break;
                                default: op = new Opcode(Ops.bregld_u).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "byte":
                        case "char":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op = new Opcode(Ops.bregld_0); break;
                                case 1: op = new Opcode(Ops.bregld_1); break;
                                case 2: op = new Opcode(Ops.bregld_2); break;
                                case 3: op = new Opcode(Ops.bregld_3); break;
                                default: op = new Opcode(Ops.bregld).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "ushort":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.sregld_0_u); break;
                                case 1: op =    new Opcode(Ops.sregld_1_u); break;
                                case 2: op =    new Opcode(Ops.sregld_2_u); break;
                                case 3: op =    new Opcode(Ops.sregld_3_u); break;
                                default: op =   new Opcode(Ops.sregld_u).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "short":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.sregld_0); break;
                                case 1: op =    new Opcode(Ops.sregld_1); break;
                                case 2: op =    new Opcode(Ops.sregld_2); break;
                                case 3: op =    new Opcode(Ops.sregld_3); break;
                                default: op =   new Opcode(Ops.sregld).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "uint":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.iregld_0_u); break;
                                case 1: op =    new Opcode(Ops.iregld_1_u); break;
                                case 2: op =    new Opcode(Ops.iregld_2_u); break;
                                case 3: op =    new Opcode(Ops.iregld_3_u); break;
                                default: op =   new Opcode(Ops.iregld_u).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "int":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.iregld_0); break;
                                case 1: op =    new Opcode(Ops.iregld_1); break;
                                case 2: op =    new Opcode(Ops.iregld_2); break;
                                case 3: op =    new Opcode(Ops.iregld_3); break;
                                default: op =   new Opcode(Ops.iregld).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "ulong":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.lregld_0_u); break;
                                case 1: op =    new Opcode(Ops.lregld_1_u); break;
                                case 2: op =    new Opcode(Ops.lregld_2_u); break;
                                case 3: op =    new Opcode(Ops.lregld_3_u); break;
                                default: op =   new Opcode(Ops.lregld_u).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "long":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.lregld_0); break;
                                case 1: op =    new Opcode(Ops.lregld_1); break;
                                case 2: op =    new Opcode(Ops.lregld_2); break;
                                case 3: op =    new Opcode(Ops.lregld_3); break;
                                default: op =   new Opcode(Ops.lregld).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "uint128":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.liregld_0_u); break;
                                case 1: op =    new Opcode(Ops.liregld_1_u); break;
                                case 2: op =    new Opcode(Ops.liregld_2_u); break;
                                case 3: op =    new Opcode(Ops.liregld_3_u); break;
                                default: op =   new Opcode(Ops.liregld_u).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "int128":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.liregld_0); break;
                                case 1: op =    new Opcode(Ops.liregld_1); break;
                                case 2: op =    new Opcode(Ops.liregld_2); break;
                                case 3: op =    new Opcode(Ops.liregld_3); break;
                                default: op =   new Opcode(Ops.liregld).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "uint256":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.llregld_0_u); break;
                                case 1: op =    new Opcode(Ops.llregld_1_u); break;
                                case 2: op =    new Opcode(Ops.llregld_2_u); break;
                                case 3: op =    new Opcode(Ops.llregld_3_u); break;
                                default: op =   new Opcode(Ops.llregld_u).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "int256":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.llregld_0); break;
                                case 1: op =    new Opcode(Ops.llregld_1); break;
                                case 2: op =    new Opcode(Ops.llregld_2); break;
                                case 3: op =    new Opcode(Ops.llregld_3); break;
                                default: op =   new Opcode(Ops.llregld).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "float":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.fregld_0); break;
                                case 1: op =    new Opcode(Ops.fregld_1); break;
                                case 2: op =    new Opcode(Ops.fregld_2); break;
                                case 3: op =    new Opcode(Ops.fregld_3); break;
                                default: op =   new Opcode(Ops.fregld).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "double":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.dregld_0); break;
                                case 1: op =    new Opcode(Ops.dregld_1); break;
                                case 2: op =    new Opcode(Ops.dregld_2); break;
                                case 3: op =    new Opcode(Ops.dregld_3); break;
                                default: op =   new Opcode(Ops.dregld).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "float128":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.dfregld_0); break;
                                case 1: op =    new Opcode(Ops.dfregld_1); break;
                                case 2: op =    new Opcode(Ops.dfregld_2); break;
                                case 3: op =    new Opcode(Ops.dfregld_3); break;
                                default: op =   new Opcode(Ops.dfregld).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "float256":
                            switch ((int) v.localvarlocation)
                            {
                                case 0: op =    new Opcode(Ops.ddregld_0); break;
                                case 1: op =    new Opcode(Ops.ddregld_1); break;
                                case 2: op =    new Opcode(Ops.ddregld_2); break;
                                case 3: op =    new Opcode(Ops.ddregld_3); break;
                                default: op =   new Opcode(Ops.ddregld).add(Opcode.convertLong(v.localvarlocation)); break;
                            } break;
                        case "String":
                        case "string":
                            svrerr("compiler error: cannot load String to ALU.");
                        default:
                            svrerr("compiler error: cannot perform math operation on a reference.");
                            break;
                    }
                }
                break;
                default:
                    if (v.isField)
                    {
                        switch (v.type)
                        {
                            case "byte":
                            case "char":
                                op = new Opcode(Ops.bmov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "short":
                                op = new Opcode(Ops.smov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "int":
                                op = new Opcode(Ops.imov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "long":
                                op = new Opcode(Ops.lmov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "int128":
                                op = new Opcode(Ops.limov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "int256":
                                op = new Opcode(Ops.llmov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "ubyte":
                            case "uchar":
                                op = new Opcode(Ops.bmov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "ushort":
                                op = new Opcode(Ops.smov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "uint":
                                op = new Opcode(Ops.imov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "ulong":
                                op = new Opcode(Ops.lmov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "uint128":
                                op = new Opcode(Ops.limov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "uint256":
                                op = new Opcode(Ops.llmov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "float":
                                op = new Opcode(Ops.fmov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "double":
                                op = new Opcode(Ops.dmov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "float128":
                                op = new Opcode(Ops.dfmov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "float256":
                                op = new Opcode(Ops.ddmov).add(Opcode.convertLong(v.localvarlocation)); break;
                            case "String":
                            case "string":
                                op = new Opcode(Ops.csmov).add(Opcode.convertLong(v.localvarlocation)); break;
                            default:
                                op = new Opcode(Ops.amov).add(Opcode.convertLong(v.localvarlocation)); break;
                        }

                        op = new Opcode(Ops.ebp).add(op);
                    } else {
                        switch (v.type)
                        {
                            case "ubyte":
                            case "uchar":
                            case "byte":
                            case "char":
                                switch ((int) v.localvarlocation)
                                {
                                    case 0: op = new Opcode(Ops.bload_0); break;
                                    case 1: op = new Opcode(Ops.bload_1); break;
                                    case 2: op = new Opcode(Ops.bload_2); break;
                                    case 3: op = new Opcode(Ops.bload_3); break;
                                    default: op = new Opcode(Ops.bload).add(Opcode.convertLong(v.localvarlocation)); break;
                                } break;
                            case "ushort":
                            case "short":
                                switch ((int) v.localvarlocation)
                                {
                                    case 0: op =    new Opcode(Ops.sload_0); break;
                                    case 1: op =    new Opcode(Ops.sload_1); break;
                                    case 2: op =    new Opcode(Ops.sload_2); break;
                                    case 3: op =    new Opcode(Ops.sload_3); break;
                                    default: op =   new Opcode(Ops.sload).add(Opcode.convertLong(v.localvarlocation)); break;
                                } break;
                            case "uint":
                            case "int":
                                switch ((int) v.localvarlocation)
                                {
                                    case 0: op =    new Opcode(Ops.iload_0); break;
                                    case 1: op =    new Opcode(Ops.iload_1); break;
                                    case 2: op =    new Opcode(Ops.iload_2); break;
                                    case 3: op =    new Opcode(Ops.iload_3); break;
                                    default: op =   new Opcode(Ops.iload).add(Opcode.convertLong(v.localvarlocation)); break;
                                } break;
                            case "ulong":
                            case "long":
                                switch ((int) v.localvarlocation)
                                {
                                    case 0: op =    new Opcode(Ops.lload_0); break;
                                    case 1: op =    new Opcode(Ops.lload_1); break;
                                    case 2: op =    new Opcode(Ops.lload_2); break;
                                    case 3: op =    new Opcode(Ops.lload_3); break;
                                    default: op =   new Opcode(Ops.lload).add(Opcode.convertLong(v.localvarlocation)); break;
                                } break;
                            case "uint128":
                            case "int128":
                                switch ((int) v.localvarlocation)
                                {
                                    case 0: op =    new Opcode(Ops.liload_0); break;
                                    case 1: op =    new Opcode(Ops.liload_1); break;
                                    case 2: op =    new Opcode(Ops.liload_2); break;
                                    case 3: op =    new Opcode(Ops.liload_3); break;
                                    default: op =   new Opcode(Ops.liload).add(Opcode.convertLong(v.localvarlocation)); break;
                                }
                            case "uint256":
                            case "int256":
                                switch ((int) v.localvarlocation)
                                {
                                    case 0: op =    new Opcode(Ops.llload_0); break;
                                    case 1: op =    new Opcode(Ops.llload_1); break;
                                    case 2: op =    new Opcode(Ops.llload_2); break;
                                    case 3: op =    new Opcode(Ops.llload_3); break;
                                    default: op =   new Opcode(Ops.llload).add(Opcode.convertLong(v.localvarlocation)); break;
                                } break;
                            case "float":
                                switch ((int) v.localvarlocation)
                                {
                                    case 0: op =    new Opcode(Ops.fload_0); break;
                                    case 1: op =    new Opcode(Ops.fload_1); break;
                                    case 2: op =    new Opcode(Ops.fload_2); break;
                                    case 3: op =    new Opcode(Ops.fload_3); break;
                                    default: op =   new Opcode(Ops.fload).add(Opcode.convertLong(v.localvarlocation)); break;
                                } break;
                            case "double":
                                switch ((int) v.localvarlocation)
                                {
                                    case 0: op =    new Opcode(Ops.dload_0); break;
                                    case 1: op =    new Opcode(Ops.dload_1); break;
                                    case 2: op =    new Opcode(Ops.dload_2); break;
                                    case 3: op =    new Opcode(Ops.dload_3); break;
                                    default: op =   new Opcode(Ops.dload).add(Opcode.convertLong(v.localvarlocation)); break;
                                } break;
                            case "float128":
                                switch ((int) v.localvarlocation)
                                {
                                    case 0: op =    new Opcode(Ops.dfload_0); break;
                                    case 1: op =    new Opcode(Ops.dfload_1); break;
                                    case 2: op =    new Opcode(Ops.dfload_2); break;
                                    case 3: op =    new Opcode(Ops.dfload_3); break;
                                    default: op =   new Opcode(Ops.dfload).add(Opcode.convertLong(v.localvarlocation)); break;
                                } break;
                            case "float256":
                                switch ((int) v.localvarlocation)
                                {
                                    case 0: op =    new Opcode(Ops.ddload_0); break;
                                    case 1: op =    new Opcode(Ops.ddload_1); break;
                                    case 2: op =    new Opcode(Ops.ddload_2); break;
                                    case 3: op =    new Opcode(Ops.ddload_3); break;
                                    default: op =   new Opcode(Ops.ddload).add(Opcode.convertLong(v.localvarlocation)); break;
                                } break;
                            case "String":
                            case "string":
                                switch ((int) v.localvarlocation)
                                {
                                    case 0: op =    new Opcode(Ops.csload_0); break;
                                    case 1: op =    new Opcode(Ops.csload_1); break;
                                    case 2: op =    new Opcode(Ops.csload_2); break;
                                    case 3: op =    new Opcode(Ops.csload_3); break;
                                    default: op =   new Opcode(Ops.csload).add(Opcode.convertLong(v.localvarlocation)); break;
                                } break;
                            default:
                                switch ((int) v.localvarlocation)
                                {
                                    case 0: op =    new Opcode(Ops.aload_0); break;
                                    case 1: op =    new Opcode(Ops.aload_1); break;
                                    case 2: op =    new Opcode(Ops.aload_2); break;
                                    case 3: op =    new Opcode(Ops.aload_3); break;
                                    default: op =   new Opcode(Ops.aload).add(Opcode.convertLong(v.localvarlocation)); break;
                                } break;
                        }
                    }
                    break;
        }

        return op;
    }

    private enum CompileType
    {
        NONE,
        STRAIGHT_TO_REGISTER,
        UNARY_P,
        UNARY_M,
        FOR,
        ;
    }

    private void compile(Token token, Opcode ops)
    {
        this.compile(token, ops, CompileType.NONE);
    }

    private Map<String, Byte> precedence;

    private void setLastOnStack(String lastOnStack)
    {
        if (this.lastOnStack != "")
        {
            if (precedence.containsKey(lastOnStack) && precedence.containsKey(this.lastOnStack))
            {
                if (precedence.get(lastOnStack) > precedence.get(this.lastOnStack))
                    this.lastOnStack = lastOnStack;
            } else return;
        }
        this.lastOnStack = lastOnStack;
    }

    private String resetLastOnStack()
    {
        String los = lastOnStack;
        lastOnStack = "";

        return los;
    }

    private void compileNumberInput(Token token, Opcode ops, CompileType compileType)
    {
        if (true) {//lastOnStack.isEmpty()) {
            switch (compileType) {
                case FOR:
                case STRAIGHT_TO_REGISTER:
                    switch (token.toString()) {
                        case "0":
                            ops.add(new Opcode(Ops.lconstrldu_0));
                            break;
                        case "1":
                            ops.add(new Opcode(Ops.lconstrldu_1));
                            break;
                        case "2":
                            ops.add(new Opcode(Ops.lconstrldu_2));
                            break;
                        case "3":
                            ops.add(new Opcode(Ops.lconstrldu_3));
                            break;
                        default:
                            long l = Long.parseLong(token.toString());

                            if (l >= 0)
                                ops.add(new Opcode(Ops.lconstrld_u).add(Opcode.convertLong(l)));
                            else ops.add(new Opcode(Ops.lconstrld).add(Opcode.convertLong(l)));
                            break;
                    }
                    break;
                default:
                    switch (token.toString()) {
                        case "0":
                            ops.add(new Opcode(Ops.lconst_0));
                            break;
                        case "1":
                            ops.add(new Opcode(Ops.lconst_1));
                            break;
                        case "2":
                            ops.add(new Opcode(Ops.lconst_2));
                            break;
                        case "3":
                            ops.add(new Opcode(Ops.lconst_3));
                            break;
                        default:
                            long l = Long.parseLong(token.toString());

                            ops.add(new Opcode(Ops.lconst).add(Opcode.convertLong(l)));
                            break;
                    }
                    setLastOnStack("long");
                    break;
            }
        } else {
            switch (lastOnStack)
            {
                case "byte":
                case "char":
                case "ubyte":
                case "uchar":
                    switch (token.toString())
                    {
                        case "0":
                            ops.add(new Opcode(Ops.bconst_0));
                            break;
                        case "1":
                            ops.add(new Opcode(Ops.bconst_1));
                            break;
                        case "2":
                            ops.add(new Opcode(Ops.bconst_2));
                            break;
                        case "3":
                            ops.add(new Opcode(Ops.bconst_3));
                            break;
                        default:
                            long l = Long.parseLong(token.toString());

                            if (l >= 0)
                                ops.add(new Opcode(Ops.bconst).add(Opcode.convertByte(l)));
                            else ops.add(new Opcode(Ops.bconst).add(Opcode.convertByte(l)));
                            break;
                    }
                    break;
                case "short":
                case "ushort":
                    switch (token.toString())
                    {
                        case "0":
                            ops.add(new Opcode(Ops.sconst_0));
                            break;
                        case "1":
                            ops.add(new Opcode(Ops.sconst_1));
                            break;
                        case "2":
                            ops.add(new Opcode(Ops.sconst_2));
                            break;
                        case "3":
                            ops.add(new Opcode(Ops.sconst_3));
                            break;
                        default:
                            long l = Long.parseLong(token.toString());

                            if (l >= 0)
                                ops.add(new Opcode(Ops.sconst).add(Opcode.convertShort(l)));
                            else ops.add(new Opcode(Ops.sconst).add(Opcode.convertShort(l)));
                            break;
                    }
                    break;
                case "int":
                case "uint":
                    switch (token.toString())
                    {
                        case "0":
                            ops.add(new Opcode(Ops.iconst_0));
                            break;
                        case "1":
                            ops.add(new Opcode(Ops.iconst_1));
                            break;
                        case "2":
                            ops.add(new Opcode(Ops.iconst_2));
                            break;
                        case "3":
                            ops.add(new Opcode(Ops.iconst_3));
                            break;
                        default:
                            long l = Long.parseLong(token.toString());

                            if (l >= 0)
                                ops.add(new Opcode(Ops.iconst).add(Opcode.convertInteger(l)));
                            else ops.add(new Opcode(Ops.iconst).add(Opcode.convertInteger(l)));
                            break;
                    }
                    break;
                case "long":
                case "ulong":
                    switch (token.toString())
                    {
                        case "0":
                            ops.add(new Opcode(Ops.lconst_0));
                            break;
                        case "1":
                            ops.add(new Opcode(Ops.lconst_1));
                            break;
                        case "2":
                            ops.add(new Opcode(Ops.lconst_2));
                            break;
                        case "3":
                            ops.add(new Opcode(Ops.lconst_3));
                            break;
                        default:
                            long l = Long.parseLong(token.toString());

                            if (l >= 0)
                                ops.add(new Opcode(Ops.lconst).add(Opcode.convertLong(l)));
                            else ops.add(new Opcode(Ops.lconst).add(Opcode.convertLong(l)));
                            break;
                    }
                    break;
                case "int128":
                case "uint128":
                    switch (token.toString())
                    {
                        case "0":
                            ops.add(new Opcode(Ops.liconst_0));
                            break;
                        case "1":
                            ops.add(new Opcode(Ops.liconst_1));
                            break;
                        case "2":
                            ops.add(new Opcode(Ops.liconst_2));
                            break;
                        case "3":
                            ops.add(new Opcode(Ops.liconst_3));
                            break;
                        default:
                            long l = Long.parseLong(token.toString());

                            if (l >= 0)
                                ops.add(new Opcode(Ops.liconst).add(Opcode.convertLong(l)));
                            else ops.add(new Opcode(Ops.liconst).add(Opcode.convertLong(l)));
                            break;
                    }
                    break;
                case "int256":
                case "uint256":
                    switch (token.toString())
                    {
                        case "0":
                            ops.add(new Opcode(Ops.llconst_0));
                            break;
                        case "1":
                            ops.add(new Opcode(Ops.llconst_1));
                            break;
                        case "2":
                            ops.add(new Opcode(Ops.llconst_2));
                            break;
                        case "3":
                            ops.add(new Opcode(Ops.llconst_3));
                            break;
                        default:
                            long l = Long.parseLong(token.toString());

                            if (l >= 0)
                                ops.add(new Opcode(Ops.llconst).add(Opcode.convertLong(l)));
                            else ops.add(new Opcode(Ops.llconst).add(Opcode.convertLong(l)));
                            break;
                    }
                    break;
                case "float":
                    switch (token.toString())
                    {
                        case "0":
                            ops.add(new Opcode(Ops.fconst_0));
                            break;
                        case "1":
                            ops.add(new Opcode(Ops.fconst_1));
                            break;
                        case "2":
                            ops.add(new Opcode(Ops.fconst_2));
                            break;
                        case "3":
                            ops.add(new Opcode(Ops.fconst_3));
                            break;
                        default:
                            float l = Float.parseFloat(token.toString());

                            if (l >= 0)
                                ops.add(new Opcode(Ops.fconst).add(Opcode.convertFloat(l)));
                            else ops.add(new Opcode(Ops.fconst).add(Opcode.convertFloat(l)));
                            break;
                    }
                case "double":
                    switch (token.toString())
                    {
                        case "0":
                            ops.add(new Opcode(Ops.dconst_0));
                            break;
                        case "1":
                            ops.add(new Opcode(Ops.dconst_1));
                            break;
                        case "2":
                            ops.add(new Opcode(Ops.dconst_2));
                            break;
                        case "3":
                            ops.add(new Opcode(Ops.dconst_3));
                            break;
                        default:
                            double l = Double.parseDouble(token.toString());

                            if (l >= 0)
                                ops.add(new Opcode(Ops.dconst).add(Opcode.convertDouble(l)));
                            else ops.add(new Opcode(Ops.dconst).add(Opcode.convertDouble(l)));
                            break;
                    }
                case "float128":
                    switch (token.toString())
                    {
                        case "0":
                            ops.add(new Opcode(Ops.dfconst_0));
                            break;
                        case "1":
                            ops.add(new Opcode(Ops.dfconst_1));
                            break;
                        case "2":
                            ops.add(new Opcode(Ops.dfconst_2));
                            break;
                        case "3":
                            ops.add(new Opcode(Ops.dfconst_3));
                            break;
                        default:
                            long l = Long.parseLong(token.toString());

                            if (l >= 0)
                                ops.add(new Opcode(Ops.dfconst).add(Opcode.convertByte(l)));
                            else ops.add(new Opcode(Ops.dfconst).add(Opcode.convertByte(l)));
                            break;
                    }
                case "float256":
                    switch (token.toString())
                    {
                        case "0":
                            ops.add(new Opcode(Ops.ddconst_0));
                            break;
                        case "1":
                            ops.add(new Opcode(Ops.ddconst_1));
                            break;
                        case "2":
                            ops.add(new Opcode(Ops.ddconst_2));
                            break;
                        case "3":
                            ops.add(new Opcode(Ops.ddconst_3));
                            break;
                        default:
                            long l = Long.parseLong(token.toString());

                            if (l >= 0)
                                ops.add(new Opcode(Ops.ddconst).add(Opcode.convertByte(l)));
                            else ops.add(new Opcode(Ops.ddconst).add(Opcode.convertByte(l)));
                            break;
                    }
                    break;
            }
        }
    }

    private Opcode JUMP_TO = null;

    private void compile(Token token, Opcode ops, CompileType compileType)
    {
        switch (token.getType())
        {
            case EMPTY_DECLARATION:
                declareEmptyVariable(token, ops);
                break;
            case INITIALIZATION:
                initializeVariable(token, ops);
                break;
            case UNARY:
                Opcode unary = new Opcode( "unary operation");
                switch (token.toString())
                {
                    case "+":
                            compile(token.getTokens().get(0), unary, CompileType.UNARY_P);
                        break;
                    case "-":
                            compile(token.getTokens().get(0), unary, CompileType.UNARY_M);
                        break;
                }
                ops.add(unary);
                break;
            case LESS_THAN:
                Opcode lessthan = new Opcode( "less than");

                for (Token t : token)
                    compile(t, lessthan, compileType);

                if (compileType.equals(CompileType.FOR))
                    ops.add(lessthan.add(JUMP_TO = new Opcode(Ops.if_cmplt).add(Opcode.convertInteger(0))));
                else ops.add(lessthan.add(new Opcode(Ops.ltn)));
                break;
            case MORE_THAN:
                Opcode morethan = new Opcode( "more than");

                for (Token t : token)
                    compile(t, morethan, compileType);

                if (compileType.equals(CompileType.FOR))
                    ops.add(morethan.add(JUMP_TO = new Opcode(Ops.if_cmpgt).add(Opcode.convertInteger(0))));
                else ops.add(morethan.add(new Opcode(Ops.gtn)));
                break;
            case LESSTHAN_EQUAL:
                Opcode lessthaneq = new Opcode( "less than equal to");

                for (Token t : token)
                    compile(t, lessthaneq, compileType);

                if (compileType.equals(CompileType.FOR))
                    ops.add(lessthaneq.add(JUMP_TO = new Opcode(Ops.if_cmplteq).add(Opcode.convertInteger(0))));
                else ops.add(lessthaneq.add(new Opcode(Ops.lte)));
                break;
            case MORETHAN_EQUAL:
                Opcode morethaneq = new Opcode( "more than");

                for (Token t : token)
                    compile(t, morethaneq, compileType);

                if (compileType.equals(CompileType.FOR))
                    ops.add(morethaneq.add(JUMP_TO = new Opcode(Ops.if_cmpgteq).add(Opcode.convertInteger(0))));
                else ops.add(morethaneq.add(new Opcode(Ops.gte)));
                break;
            case FOR:
                Token for_case = token.getTokens().get(0);
                Token caseBody = token.getTokens().get(1);

                long lvtable_l = localVariableTable.size();
                long lvtable_t = lcllvts;

                if (for_case.getTokens().size() != 3)
                    svrerr("for loops must contains 3 segments for ( ; ; ; ) {}");

                Opcode for_loop = new Opcode( "for loop");
                Opcode checkLoop = new Opcode( "check");

                Token for_init_ = for_case.getTokens().get(0);
                Token for_midl_ = for_case.getTokens().get(1);
                Token for_last_ = for_case.getTokens().get(2);

                //TODO: make sure the compile type is correct this way.
                compile(for_init_, for_loop, CompileType.NONE);

                long jumplcn = getExecutable().op_codes.size() + for_loop.toExecutable().op_codes.size();

                compile(for_midl_, checkLoop, CompileType.FOR);
                compile(for_last_, checkLoop, CompileType.NONE);
                compile(caseBody, checkLoop, CompileType.NONE);

                JUMP_TO.getChildren().clear();

//                unsvrerr("for loop size: " + (checkLoop.toExecutable().op_codes.size() + for_loop.toExecutable().op_codes.size()) + 10);

                JUMP_TO.add((Opcode.convertInteger(jumplcn + checkLoop.toExecutable().op_codes.size() + 14)));

                for_loop.add(checkLoop.getChildren());
                ops.add(for_loop);
                ops.add(new Opcode(Ops.jump).add(Opcode.convertLong(jumplcn)));

                // DELETE ALL VARIABLES DECLARED IN FOR_LOOP

                lcllvts = lvtable_t;

                while (localVariableTable.size() > lvtable_l)
                {
                    String most_recent = "";

                    for (String v : localVariableTable.keySet())
                       if (localVariableTable.get(v).localvarlocation == (localVariableTable.size() - 1))
                           most_recent = v;

                    localVariableTable.remove(most_recent);
                }
                break;
            case IDENTIFIER:
                ops.add(loadVariable(token.toString(), ops, compileType));
                break;
            case NUMBER:
                compileNumberInput(token, ops, compileType);
                break;
            case MULTIPLICATION:
                compile(token.getTokens().get(0), ops, CompileType.STRAIGHT_TO_REGISTER);
                compile(token.getTokens().get(1), ops, CompileType.STRAIGHT_TO_REGISTER);
                ops.add(new Opcode(Ops.mul));
                break;
            case ADDITION:
                compile(token.getTokens().get(0), ops, CompileType.STRAIGHT_TO_REGISTER);
                compile(token.getTokens().get(1), ops, CompileType.STRAIGHT_TO_REGISTER);
                ops.add(new Opcode(Ops.add));
                break;
            case SUBTRACTION:
                compile(token.getTokens().get(0), ops, CompileType.STRAIGHT_TO_REGISTER);
                compile(token.getTokens().get(1), ops, CompileType.STRAIGHT_TO_REGISTER);
                ops.add(new Opcode(Ops.sub));
                break;
            case SUBDIVISION:
                compile(token.getTokens().get(0), ops, CompileType.STRAIGHT_TO_REGISTER);
                compile(token.getTokens().get(1), ops, CompileType.STRAIGHT_TO_REGISTER);
                ops.add(new Opcode(Ops.div));
                break;
            case MOD:
                compile(token.getTokens().get(0), ops, CompileType.STRAIGHT_TO_REGISTER);
                compile(token.getTokens().get(1), ops, CompileType.STRAIGHT_TO_REGISTER);
                ops.add(new Opcode(Ops.mod));
                break;
            default:
                for (Token t : token)
                    compile(t, ops);
                break;
        }
    }

    private void declareEmptyVariable(Token declaration, Opcode ops)
    {
        String type = declaration.getTokens().get(0).toString();
        String name = declaration.getTokens().get(1).toString();

        Opcode op   = null;
        Opcode stor = null;

        var v = storeLocalVariable(name, type, new LinkedHashSet<>(declaration.getModifiers()));

        switch (type)
        {
            case "byte":
            case "char":
            case "ubyte":
            case "uchar":
                op = new Opcode(Ops.bconst_e);
                if      (v.localvarlocation == 0)
                    stor = new Opcode(Ops.bstore_0);
                else if (v.localvarlocation == 1)
                    stor = new Opcode(Ops.bstore_1);
                else if (v.localvarlocation == 2)
                    stor = new Opcode(Ops.bstore_2);
                else if (v.localvarlocation == 3)
                    stor = new Opcode(Ops.bstore_3);
                else stor = new Opcode(Ops.bstore).add(Opcode.convertLong(v.localvarlocation));
                break;
            case "ushort":
            case "short":
                op = new Opcode(Ops.sconst_e); break;
            case "int":
            case "uint":
                if      (v.localvarlocation == 0)
                    stor = new Opcode(Ops.iconstse_0);
                else if (v.localvarlocation == 1)
                    stor = new Opcode(Ops.iconstse_1);
                else if (v.localvarlocation == 2)
                    stor = new Opcode(Ops.iconstse_2);
                else if (v.localvarlocation == 3)
                    stor = new Opcode(Ops.iconstse_3);
                else stor = new Opcode(Ops.iconstse).add(Opcode.convertLong(v.localvarlocation));
                break;
            case "long":
            case "ulong":
                stor = new Opcode(Ops.lconstse).add(Opcode.convertLong(v.localvarlocation));
                break;
            case "int128":
            case "uint128":
                stor = new Opcode(Ops.liconstse).add(Opcode.convertLong(v.localvarlocation));
            case "int256":
            case "uint256":
                stor = new Opcode(Ops.llconstse).add(Opcode.convertLong(v.localvarlocation));
                break;
            case "float":
                stor = new Opcode(Ops.fconstse).add(Opcode.convertLong(v.localvarlocation));
                break;
            case "double":
                stor = new Opcode(Ops.dconstse).add(Opcode.convertLong(v.localvarlocation));
                break;
            case "float128":
                stor = new Opcode(Ops.dfconstse).add(Opcode.convertLong(v.localvarlocation));
                break;
            case "float256":
                stor = new Opcode(Ops.ddconstse).add(Opcode.convertLong(v.localvarlocation));
                break;
            case "String":
            case "string":
                stor = new Opcode(Ops.aconstse).add(Opcode.convertLong(v.localvarlocation));
                break;
        }

        ops.add(new Opcode( "declare(" + name + ", " + type + ")").add(op).add(stor));
    }

    private var storeLocalVariable(String name, String type, Set<Modifier> modifiers)
    {
        var v = new var(name, type, modifiers);

        var p = null;

        if ((p = localVariableTable.put(name, v)) != null)
        {
            System.err.println("compiler error: variable '" + name + "' already declared as '" + p.name + " of type " + p.type + "'");
            System.exit(0);
        }

        return v;
    }

    private var getLocalVariable(String name)
    {
        if (localVariableTable.containsKey(name))
            return localVariableTable.get(name);

        if (self != null)
        {
            if (self.containsField(name, self.getName()))
            {
                Field field = self.getField(name, self.getName());
                return new var(name, field.getTypeName(), field.getModifiers(), field.getLocation(), true);
            }
        }

        System.err.println("compiler error: local variable '" + name + "' does not exist.");
//        System.exit(0);

        return new var("f", "sd", new LinkedHashSet<>());
    }

    private Opcode storeByteInput(String input, Opcode ops)
    {
        byte b = (byte) Long.parseLong(input);

        switch (b)
        {
            case 0: return new Opcode(Ops.bconst_0);
            case 1: return new Opcode(Ops.bconst_1);
            case 2: return new Opcode(Ops.bconst_2);
            case 3: return new Opcode(Ops.bconst_3);
            default:
                return new Opcode(Ops.bconst).add(Opcode.convertByte(b));
        }
    }

    private Opcode storeShortInput(String input, Opcode ops)
    {
        short b = (short) Long.parseLong(input);

        switch (b)
        {
            case 0: return new Opcode(Ops.sconst_0);
            case 1: return new Opcode(Ops.sconst_1);
            case 2: return new Opcode(Ops.sconst_2);
            case 3: return new Opcode(Ops.sconst_3);
            default:
                return new Opcode(Ops.sconst).add(Opcode.convertShort(b));
        }
    }

    private Opcode storeIntInput(String input, Opcode ops)
    {
        int b = (int) Long.parseLong(input);

        switch (b)
        {
            case 0: return new Opcode(Ops.iconst_0);
            case 1: return new Opcode(Ops.iconst_1);
            case 2: return new Opcode(Ops.iconst_2);
            case 3: return new Opcode(Ops.iconst_3);
            default:
                return new Opcode(Ops.iconst).add(Opcode.convertInteger(b));
        }
    }

    private Opcode storeLongInput(String input)
    {
        short b = (short) Long.parseLong(input);

        switch (b)
        {
            case 0: return new Opcode(Ops.lconst_0);
            case 1: return new Opcode(Ops.lconst_1);
            case 2: return new Opcode(Ops.lconst_2);
            case 3: return new Opcode(Ops.lconst_3);
            default:
                return new Opcode(Ops.lconst).add(Opcode.convertLong(b));
        }
    }

    private Opcode storeFloatInput(String input)
    {
        float b = (float) Double.parseDouble(input);

            if (b == 0) return new Opcode(Ops.fconst_0);
            else if (b == 1) return new Opcode(Ops.fconst_1);
            else if (b == 2) return new Opcode(Ops.fconst_2);
            else if (b == 3) return new Opcode(Ops.fconst_3);
            else
                return new Opcode(Ops.fconst).add(Opcode.convertFloat(b));
    }

    private Opcode storeDoubleInput(String input)
    {
        double b = (double) Double.parseDouble(input);

        if (b == 0) return new Opcode(Ops.dconst_0);
        else if (b == 1) return new Opcode(Ops.dconst_1);
        else if (b == 2) return new Opcode(Ops.dconst_2);
        else if (b == 3) return new Opcode(Ops.dconst_3);
        else
                return new Opcode(Ops.dconst).add(Opcode.convertDouble(b));
    }

    private Opcode storeStringInput(String input)
    {
        if (input.isEmpty())
            return new Opcode(Ops.csconst_e);

        else return new Opcode(Ops.csconst).add(Opcode.convertShort(input.length())).add(Opcode.convertBytes(input.getBytes()));
    }

    private Opcode storeInput(Token input, String type, long variableLocation, Opcode ops)
    {
        Opcode op = new Opcode( "store input");

        switch (type)
        {
            case "byte":
            case "char":
            case "ubyte":
            case "uchar":
                    if (variableLocation == 0)
                        op.add(putByteInput(input, ops)).add(castToChar()).add(new Opcode(Ops.bstore_0));
                    else if (variableLocation == 1)
                        op.add(putByteInput(input, ops)).add(castToChar()).add(new Opcode(Ops.bstore_1));
                    else if (variableLocation == 2)
                        op.add(putByteInput(input, ops)).add(castToChar()).add(new Opcode(Ops.bstore_2));
                    else if (variableLocation == 3)
                        op.add(putByteInput(input, ops)).add(castToChar()).add(new Opcode(Ops.bstore_3));
                    else op.add(putByteInput(input, ops)).add(castToChar()).add(new Opcode(Ops.bstore).add(Opcode.convertLong(variableLocation)));
                break;
            case "short":
            case "ushort":
                if (variableLocation == 0)
                    op.add(putShortInput(input, ops)).add(castToShort()).add(new Opcode(Ops.sstore_0));
                else if (variableLocation == 1)
                    op.add(putShortInput(input, ops)).add(castToShort()).add(new Opcode(Ops.sstore_1));
                else if (variableLocation == 2)
                    op.add(putShortInput(input, ops)).add(castToShort()).add(new Opcode(Ops.sstore_2));
                else if (variableLocation == 3)
                    op.add(putShortInput(input, ops)).add(castToShort()).add(new Opcode(Ops.sstore_3));
                else op.add(putShortInput(input, ops)).add(castToShort()).add(new Opcode(Ops.sstore).add(Opcode.convertLong(variableLocation)));
                break;
            case "uint":
            case "int":
                if (variableLocation == 0)
                    op.add(putIntInput(input, ops)).add(castToInt()).add(new Opcode(Ops.istore_0));
                else if (variableLocation == 1)
                    op.add(putIntInput(input, ops)).add(castToInt()).add(new Opcode(Ops.istore_1));
                else if (variableLocation == 2)
                    op.add(putIntInput(input, ops)).add(castToInt()).add(new Opcode(Ops.istore_2));
                else if (variableLocation == 3)
                    op.add(putIntInput(input, ops)).add(castToInt()).add(new Opcode(Ops.istore_3));
                else op.add(putIntInput(input, ops)).add(castToInt()).add(new Opcode(Ops.istore).add(Opcode.convertLong(variableLocation)));
                break;
            case "ulong":
            case "long":
                if (variableLocation == 0)
                    op.add(putLongInput(input, ops)).add(castToLong()).add(new Opcode(Ops.lstore_0));
                else if (variableLocation == 1)
                    op.add(putLongInput(input, ops)).add(castToLong()).add(new Opcode(Ops.lstore_1));
                else if (variableLocation == 2)
                    op.add(putLongInput(input, ops)).add(castToLong()).add(new Opcode(Ops.lstore_2));
                else if (variableLocation == 3)
                    op.add(putLongInput(input, ops)).add(castToLong()).add(new Opcode(Ops.lstore_3));
                else op.add(putLongInput(input, ops)).add(castToLong()).add(new Opcode(Ops.lstore).add(Opcode.convertLong(variableLocation)));
                break;
            case "int128":
//                op = new Opcode(Ops.liconst_e); break;
            case "int256":
//                op = new Opcode(Ops.llconst_e); break;
            case "uint128":
//                op = new Opcode(Ops.liconst_e); break;
            case "uint256":
//                op = new Opcode(Ops.llconst_e); break;
            case "float":
                if (variableLocation == 0)
                    op.add(putFloatInput(input, ops)).add(castToFloat()).add(new Opcode(Ops.fstore_0));
                else if (variableLocation == 1)
                    op.add(putFloatInput(input, ops)).add(castToFloat()).add(new Opcode(Ops.fstore_1));
                else if (variableLocation == 2)
                    op.add(putFloatInput(input, ops)).add(castToFloat()).add(new Opcode(Ops.fstore_2));
                else if (variableLocation == 3)
                    op.add(putFloatInput(input, ops)).add(castToFloat()).add(new Opcode(Ops.fstore_3));
                else op.add(putFloatInput(input, ops)).add(castToFloat()).add(new Opcode(Ops.fstore).add(Opcode.convertLong(variableLocation)));
                break;
            case "double":
                if (variableLocation == 0)
                    op.add(putDoubleInput(input, ops)).add(castToDouble()).add(new Opcode(Ops.dstore_0));
                else if (variableLocation == 1)
                    op.add(putDoubleInput(input, ops)).add(castToDouble()).add(new Opcode(Ops.dstore_1));
                else if (variableLocation == 2)
                    op.add(putDoubleInput(input, ops)).add(castToDouble()).add(new Opcode(Ops.dstore_2));
                else if (variableLocation == 3)
                    op.add(putDoubleInput(input, ops)).add(castToDouble()).add(new Opcode(Ops.dstore_3));
                else op.add(putDoubleInput(input, ops)).add(castToDouble()).add(new Opcode(Ops.dstore).add(Opcode.convertLong(variableLocation)));
                break;
            case "float128":
//                op = new Opcode(Ops.dfconst_e); break;
            case "float256":
//                op = new Opcode(Ops.ddconst_e); break;
            case "String":
            case "string":
                op.add(putStringInput(input, ops));
                break;
            default:
                op = new Opcode(Ops.aconst_null); break;
        }

        return op;
    }

    private Opcode castToDouble()
    {
        Opcode cast = null;//new Opcode( "");
        String t = null;
        switch (t = resetLastOnStack())
        {
            case "byte":
            case "char":
            case "ubyte":
            case "uchar":
                return cast;
            case "short":
            case "ushort":
                return cast;
            case "int":
            case "uint":
                implicitcasterr(t, "float64_t", "");
                return new Opcode(Ops.i2d);
            case "long":
            case "ulong":
                implicitcasterr(t, "float64_t", "");
                return new Opcode(Ops.l2d);
            case "int128":
            case "uint128":
                implicitcasterr(t, "float64_t", "");
                return new Opcode(Ops.li2d);
            case "int256":
            case "uint256":
                implicitcasterr(t, "float64_t", "");
                return new Opcode(Ops.ll2d);
            case "float":
                return cast;
            case "double":
                return cast;
            case "float128":
                return new Opcode(Ops.df2d);
            case "float256":
                return new Opcode(Ops.dd2d);
            case "":
                return cast;
            default:
                implicitcasterr(t + "(ptr_t)", "float64_t", "");
                return new Opcode(Ops.l2d);
        }
    }

    private Opcode castToFloat()
    {
        Opcode cast = null;//new Opcode( "");
        String t = null;
        switch (t = resetLastOnStack())
        {
            case "byte":
            case "char":
            case "ubyte":
            case "uchar":
                return cast;
            case "short":
            case "ushort":
                implicitcasterr(t, "float32_t", "");
                return new Opcode(Ops.s2f);
            case "int":
            case "uint":
                implicitcasterr(t, "float32_t", "");
                return new Opcode(Ops.i2f);
            case "long":
            case "ulong":
                implicitcasterr(t, "float32_t", "");
                return new Opcode(Ops.l2f);
            case "int128":
            case "uint128":
                implicitcasterr(t, "float32_t", "");
                return new Opcode(Ops.li2f);
            case "int256":
            case "uint256":
                implicitcasterr(t, "float32_t", "");
                return new Opcode(Ops.ll2f);
            case "float":
                return cast;
            case "double":
                implicitcasterr(t, "float32_t", "");
                return new Opcode(Ops.d2f);
            case "float128":
                implicitcasterr(t, "float32_t", "");
                return new Opcode(Ops.df2f);
            case "float256":
                implicitcasterr(t, "float32_t", "");
                return new Opcode(Ops.dd2f);
            case "":
                return cast;
            default:
                implicitcasterr(t + "(ptr_t)", "float32_t", "");
                return new Opcode(Ops.l2f);
        }
    }

    private void initializeVariable(Token token, Opcode ops)
    {
        Token varn = token.getTokens().get(0);
        Token valv = token.getTokens().get(1).getTokens().get(0);

        var v = getLocalVariable(varn.toString());

        if (v.isField)
            ops.add(new Opcode( "init (" + varn.toString() + ", " + valv.toString() + ")").add(putInput(valv, v.type, v.localvarlocation, ops)));
        else
            ops.add(new Opcode( "init (" + varn.toString() + ", " + valv.toString() + ")").add(storeInput(valv, v.type, v.localvarlocation, ops)));
    }

    private Opcode putByteInput(Token input, Opcode ops)
    {
        Opcode op = null;

        if (input.getType().equals(Token.Type.NUMBER) || input.getType().equals(Token.Type.DECIMAL))
        {
            switch (input.toString())
            {
                case "0":
                    op = new Opcode(Ops.bconst_0); break;
                case "1":
                    op = new Opcode(Ops.bconst_1); break;
                case "2":
                    op = new Opcode(Ops.bconst_2); break;
                case "3":
                    op = new Opcode(Ops.bconst_3); break;
                default:
                    if (input.getType().equals(Token.Type.NUMBER))
                        op = new Opcode(Ops.bconst).add(Opcode.convertByte(Long.parseLong(input.toString())));
                    else op = new Opcode(Ops.bconst).add(Opcode.convertByte((long) Double.parseDouble(input.toString())));
                    break;
            }
        } else {
            compile(input, ops);
        }

        return op;
    }

    private Opcode putShortInput(Token input, Opcode ops)
    {
        Opcode op = null;

        if (input.getType().equals(Token.Type.NUMBER) || input.getType().equals(Token.Type.DECIMAL))
        {
            switch (input.toString())
            {
                case "0":
                    op = new Opcode(Ops.sconst_0); break;
                case "1":
                    op = new Opcode(Ops.sconst_1); break;
                case "2":
                    op = new Opcode(Ops.sconst_2); break;
                case "3":
                    op = new Opcode(Ops.sconst_3); break;
                default:
                    if (input.getType().equals(Token.Type.NUMBER))
                        op = new Opcode(Ops.sconst).add(Opcode.convertShort(Long.parseLong(input.toString())));
                    else op = new Opcode(Ops.sconst).add(Opcode.convertShort((long) Double.parseDouble(input.toString())));
                    break;
            }
        } else {
            compile(input, ops);
        }

        return op;
    }

    private Opcode putIntInput(Token input, Opcode ops)
    {
        Opcode op = null;

        if (input.getType().equals(Token.Type.NUMBER) || input.getType().equals(Token.Type.DECIMAL))
        {
            switch (input.toString())
            {
                case "0":
                    op = new Opcode(Ops.iconst_0); break;
                case "1":
                    op = new Opcode(Ops.iconst_1); break;
                case "2":
                    op = new Opcode(Ops.iconst_2); break;
                case "3":
                    op = new Opcode(Ops.iconst_3); break;
                default:
                    if (input.getType().equals(Token.Type.NUMBER))
                        op = new Opcode(Ops.iconst).add(Opcode.convertInteger(Long.parseLong(input.toString())));
                    else op = new Opcode(Ops.iconst).add(Opcode.convertInteger((long) Double.parseDouble(input.toString())));
                    break;
            }
        } else {
            compile(input, ops);
        }

        return op;
    }

    private Opcode getValue(Token token)
    {
        return null;
    }

    private Opcode putLongInput(Token input, Opcode ops)
    {
        Opcode op = null;

        if (input.getType().equals(Token.Type.NUMBER) || input.getType().equals(Token.Type.DECIMAL))
        {
            switch (input.toString())
            {
                case "0":
                    op = new Opcode(Ops.lconst_0); break;
                case "1":
                    op = new Opcode(Ops.lconst_1); break;
                case "2":
                    op = new Opcode(Ops.lconst_2); break;
                case "3":
                    op = new Opcode(Ops.lconst_3); break;
                    default:
                        if (input.getType().equals(Token.Type.NUMBER))
                            op = new Opcode(Ops.lconst).add(Opcode.convertLong(Long.parseLong(input.toString())));
                        else op = new Opcode(Ops.lconst).add(Opcode.convertLong((long) Double.parseDouble(input.toString())));
                        break;
            }
        } else
            compile(input, ops);

        return op;
    }

    private Opcode putLongIntInput(Token input, Opcode ops)
    {
        Opcode op = null;

        return op;
    }

    private Opcode putLongLongInput(Token input, Opcode ops)
    {
        Opcode op = null;

        return op;
    }

    private Opcode putFloatInput(Token input, Opcode ops)
    {
        Opcode op = null;

        if (input.getType().equals(Token.Type.NUMBER) || input.getType().equals(Token.Type.DECIMAL))
        {
            switch (input.toString())
            {
                case "0":
                    op = new Opcode(Ops.fconst_0); break;
                case "1":
                    op = new Opcode(Ops.fconst_1); break;
                case "2":
                    op = new Opcode(Ops.fconst_2); break;
                case "3":
                    op = new Opcode(Ops.fconst_3); break;
                default:
                    if (input.getType().equals(Token.Type.NUMBER))
                        op = new Opcode(Ops.fconst).add(Opcode.convertFloat(Double.parseDouble(input.toString())));
                    else op = new Opcode(Ops.fconst).add(Opcode.convertFloat(Double.parseDouble(input.toString())));
                    break;
            }
        } else {
            compile(input, ops);
        }

        return op;
    }

    private Opcode putDoubleInput(Token input, Opcode ops)
    {
        Opcode op = null;

        if (input.getType().equals(Token.Type.NUMBER) || input.getType().equals(Token.Type.DECIMAL))
        {
            switch (input.toString())
            {
                case "0":
                    op = new Opcode(Ops.dconst_0); break;
                case "1":
                    op = new Opcode(Ops.dconst_1); break;
                case "2":
                    op = new Opcode(Ops.dconst_2); break;
                case "3":
                    op = new Opcode(Ops.dconst_3); break;
                default:
                    if (input.getType().equals(Token.Type.NUMBER))
                        op = new Opcode(Ops.dconst).add(Opcode.convertDouble(Double.parseDouble(input.toString())));
                    else op = new Opcode(Ops.dconst).add(Opcode.convertDouble(Double.parseDouble(input.toString())));
                    break;
            }
        } else {
            compile(input, ops);
        }

        return op;
    }

    private Opcode putDoubleFloatInput(Token input, Opcode ops)
    {
        Opcode op = null;

        return op;
    }

    private Opcode putDoubleDoubleInput(Token input, Opcode ops)
    {
        Opcode op = null;

        return op;
    }

    private Opcode putStringInput(Token input, Opcode ops)
    {
        Opcode op = null;

        if (input.getType().equals(Token.Type.STRING) || input.getType().equals(Token.Type.DECIMAL) || input.getType().equals(Token.Type.NUMBER))
        {
            switch (input.toString())
            {
                default:
                    if (input.getType().equals(Token.Type.NUMBER))
                                                                            op = new Opcode(Ops.csconst).add(Opcode.convertShort(input.toString().getBytes().length)).add(Opcode.convertBytes(input.toString().getBytes()));
                    else if (input.getType().equals(Token.Type.DECIMAL))    op = new Opcode(Ops.csconst).add(Opcode.convertShort(input.toString().getBytes().length)).add(Opcode.convertBytes(input.toString().getBytes()));
                    else                                                    op = new Opcode(Ops.csconst).add(Opcode.convertShort(input.toString().substring(1, input.toString().getBytes().length - 1).getBytes().length)).add(Opcode.convertBytes(input.toString().substring(1, input.toString().getBytes().length - 1).getBytes()));
                    break;
            }
        } else {
            compile(input, ops);
        }

        return op;
    }

    private void unsvrerr(String msg)
    {
        System.err.println("compiler error: " + msg);
    }

    private void svrerr(String msg)
    {
        System.err.println("compiler error: " + msg);
        System.exit(0);
    }

    private void implicitcasterr(String typea, String typeb, String extra)
    {
        unsvrerr("implicit cast from '" + typea + "' to '" + typeb + "' " + extra);
    }

    private Opcode castToChar()
    {
        Opcode cast = null;//new Opcode( "");
        String t = null;
        switch (t = resetLastOnStack())
        {
            case "byte":
            case "char":
            case "ubyte":
            case "uchar":
                return cast;
            case "short":
            case "ushort":
                implicitcasterr(t, "int8_t", "");
                return new Opcode(Ops.s2b);
            case "int":
            case "uint":
                implicitcasterr(t, "int8_t", "");
                return new Opcode(Ops.i2b);
            case "long":
            case "ulong":
                implicitcasterr(t, "int8_t", "");
                return new Opcode(Ops.l2b);
            case "int128":
            case "uint128":
                implicitcasterr(t, "int8_t", "");
                return new Opcode(Ops.li2b);
            case "int256":
            case "uint256":
                implicitcasterr(t, "int8_t", "");
                return new Opcode(Ops.ll2b);
            case "float":
                implicitcasterr(t, "int8_t", "");
                return new Opcode(Ops.f2b);
            case "double":
                implicitcasterr(t, "int8_t", "");
                return new Opcode(Ops.d2b);
            case "float128":
                implicitcasterr(t, "int8_t", "");
                return new Opcode(Ops.df2b);
            case "float256":
                implicitcasterr(t, "int8_t", "");
                return new Opcode(Ops.dd2b);
            case "":
                return cast;
                default:
                    implicitcasterr(t + "(ptr_t)", "int8_t", "");
                    return new Opcode(Ops.l2b);
        }
    }

    private Opcode castToShort()
    {
        Opcode cast = null;//new Opcode( "");
        String t = null;
        switch (t = resetLastOnStack())
        {
            case "byte":
            case "char":
            case "ubyte":
            case "uchar":
                return new Opcode(Ops.b2s);
            case "short":
            case "ushort":
                return cast;
            case "int":
            case "uint":
                implicitcasterr(t, "int16_t", "");
                return new Opcode(Ops.i2s);
            case "long":
            case "ulong":
                implicitcasterr(t, "int16_t", "");
                return new Opcode(Ops.l2s);
            case "int128":
            case "uint128":
                implicitcasterr(t, "int16_t", "");
                return new Opcode(Ops.li2s);
            case "int256":
            case "uint256":
                implicitcasterr(t, "int16_t", "");
                return new Opcode(Ops.ll2s);
            case "float":
                implicitcasterr(t, "int16_t", "");
                return new Opcode(Ops.f2s);
            case "double":
                implicitcasterr(t, "int16_t", "");
                return new Opcode(Ops.d2s);
            case "float128":
                implicitcasterr(t, "int16_t", "");
                return new Opcode(Ops.df2s);
            case "float256":
                implicitcasterr(t, "int16_t", "");
                return new Opcode(Ops.dd2s);
            case "":
                return cast;
            default:
//                System.err.println("compiler error: casting unknown type '" + lastOnStack + "' to short.");
//                System.exit(0);
                implicitcasterr(t + "(ptr_t)", "int16_t", "");
                return new Opcode(Ops.l2s);
        }
    }

    private Opcode castToInt()
    {
        Opcode cast = null;//new Opcode( "");
        String t = null;
        switch (t = resetLastOnStack())
        {
            case "byte":
            case "char":
            case "ubyte":
            case "uchar":
                return new Opcode(Ops.b2i);
            case "short":
            case "ushort":
                return new Opcode(Ops.s2i);
            case "int":
            case "uint":
                return cast;
            case "long":
            case "ulong":
                implicitcasterr(t, "int32_t", "");
                return new Opcode(Ops.l2i);
            case "int128":
            case "uint128":
                implicitcasterr(t, "int32_t", "");
                return new Opcode(Ops.li2i);
            case "int256":
            case "uint256":
                implicitcasterr(t, "int32_t", "");
                return new Opcode(Ops.ll2i);
            case "float":
                implicitcasterr(t, "int32_t", "");
                return new Opcode(Ops.f2i);
            case "double":
                implicitcasterr(t, "int32_t", "");
                return new Opcode(Ops.d2i);
            case "float128":
                implicitcasterr(t, "int32_t", "");
                return new Opcode(Ops.df2i);
            case "float256":
                implicitcasterr(t, "int32_t", "");
                return new Opcode(Ops.dd2i);
            case "":
                return cast;
            default:
//                System.err.println("compiler error: casting unknown type '" + lastOnStack + "' to int.");
//                System.exit(0);
                implicitcasterr(t + "(ptr_t)", "int32_t", "");
                return new Opcode(Ops.l2i);
        }
    }

    private Opcode castToLong()
    {
        Opcode cast = new Opcode( "");
        String t = null;
        switch (t = resetLastOnStack())
        {
            case "byte":
            case "char":
            case "ubyte":
            case "uchar":
                return new Opcode(Ops.b2l);
            case "short":
            case "ushort":
                return new Opcode(Ops.s2l);
            case "int":
            case "uint":
                return new Opcode(Ops.i2l);
            case "long":
            case "ulong":
                return cast;
            case "int128":
            case "uint128":
                implicitcasterr(t, "int64_t", "");
                return new Opcode(Ops.li2l);
            case "int256":
            case "uint256":
                implicitcasterr(t, "int64_t", "");
                return new Opcode(Ops.ll2l);
            case "float":
                implicitcasterr(t, "int64_t", "");
                return new Opcode(Ops.f2l);
            case "double":
                implicitcasterr(t, "int64_t", "");
                return new Opcode(Ops.d2l);
            case "float128":
                implicitcasterr(t, "int64_t", "");
                return new Opcode(Ops.df2l);
            case "float256":
                implicitcasterr(t, "int64_t", "");
                return new Opcode(Ops.dd2l);
            case "":
                return cast;
            default:
                implicitcasterr(t + "(ptr_t)", "int64_t", "");
                return cast;
        }
    }

    private Opcode putInput(Token input, String type, long index, Opcode ops)
    {
        Opcode op = new Opcode( "put into field");

        switch (type)
        {
            case "byte":
            case "char":
            case "ubyte":
            case "uchar":
                op.add(putByteInput(input, op), op).add(castToChar()).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptb)); break;
            case "short":
            case "ushort":
                op.add(putShortInput(input, op), op).add(castToShort()).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.pts).add(Opcode.convertLong(index))); break;
            case "uint":
            case "int":
                op.add(putIntInput(input, op)).add(castToInt()).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.pti).add(Opcode.convertLong(index))); break;
            case "ulong":
            case "long":
                op.add(putLongInput(input, op)).add(castToLong()).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptl).add(Opcode.convertLong(index))); break;
            case "int128":
        //                op = new Opcode(Ops.liconst_e); break;
            case "int256":
        //                op = new Opcode(Ops.llconst_e); break;
            case "uint128":
        //                op = new Opcode(Ops.liconst_e); break;
            case "uint256":
        //                op = new Opcode(Ops.llconst_e); break;
            case "float":
                op.add(putFloatInput(input, op)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptf).add(Opcode.convertLong(index))); break;
            case "double":
                op.add(putDoubleInput(input, op)).add(new Opcode(Ops.ebp)).add(new Opcode(Ops.ptd).add(Opcode.convertLong(index))); break;
            case "float128":
        //                op = new Opcode(Ops.dfconst_e); break;
            case "float256":
        //                op = new Opcode(Ops.ddconst_e); break;
            case "String":
            case "string":
                op.add(putStringInput(input, op)).add(new Opcode(Ops.ebp)).add(castToString()).add(new Opcode(Ops.ptcs).add(Opcode.convertLong(index)));
                break;
            default:
                op = new Opcode(Ops.aconst_null); break;
        }

        return op;
    }

    private Opcode castToString()
    {
        Opcode cast = null;//new Opcode( "");
        switch (resetLastOnStack())
        {
            case "byte":
            case "char":
                return new Opcode(Ops.b2c);
            case "ubyte":
            case "uchar":
                return new Opcode(Ops.b2cu);
            case "short":
                return new Opcode(Ops.s2c);
            case "ushort":
                return new Opcode(Ops.s2cu);
            case "int":
                return new Opcode(Ops.i2c);
            case "uint":
                return new Opcode(Ops.i2cu);
            case "reference":
            case "long":
                return new Opcode(Ops.l2c);
            case "ulong":
                return new Opcode(Ops.l2cu);
            case "int128":
                return new Opcode(Ops.li2c);
            case "uint128":
                return new Opcode(Ops.li2cu);
            case "int256":
                return new Opcode(Ops.ll2c);
            case "uint256":
                return new Opcode(Ops.ll2cu);
            case "float":
                return new Opcode(Ops.f2c);
            case "double":
                return new Opcode(Ops.d2c);
            case "float128":
                return new Opcode(Ops.df2c);
            case "float256":
                return new Opcode(Ops.dd2c);
            case "":
            case "string":
            case "String":
                return cast;
            default:
                return new Opcode(Ops.l2cu);
        }
    }

    public Executable getExecutable()
    {
        return exe;
    }

    public Opcode getOpcode()
    {
        return ops;
    }
}