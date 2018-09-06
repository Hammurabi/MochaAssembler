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
        if (method.getParent() == null || method.getParent().getName().equals("VOID") || method.getParent().getName().equals("void"))
            self = null;
        else {
            localVariableTable.put("this", new var("this", method.getParent().getName(), new LinkedHashSet<>()));
            self = method.getParent();
        }

        context = method;
        exe = new Executable();
        ops = new Opcode(-1, method.getName() + "()");

        for (Field field : method.getArguments())
            localVariableTable.put(field.getName(), new var(field.getName(), field.getTypeName(), field.getModifiers()));

        compile(child, ops);
    }

    private Opcode loadVariable(String variable, Opcode ops, CompileType compileType)
    {
        Opcode op = null;
        var v = getLocalVariable(variable);

        switch (compileType)
        {
            case STRAIGHT_TO_REGISTER:
                if (v.isField)
                {
                    switch (v.type)
                    {
                        case "ubyte":
                        case "uchar":
                            op = new Opcode(Ops.bmovrld_u); break;
                        case "byte":
                        case "char":
                            op = new Opcode(Ops.bmovrld); break;
                        case "short":
                            op = new Opcode(Ops.smovrld); break;
                        case "ushort":
                            op = new Opcode(Ops.smovrld_u); break;
                        case "uint":
                            op = new Opcode(Ops.imovrld_u); break;
                        case "int":
                            op = new Opcode(Ops.imovrld); break;
                        case "ulong":
                            op = new Opcode(Ops.lmovrld_u); break;
                        case "long":
                            op = new Opcode(Ops.lmovrld); break;
                        case "uint128":
                            op = new Opcode(Ops.limovrld_u); break;
                        case "int128":
                            op = new Opcode(Ops.limovrld); break;
                        case "uint256":
                            op = new Opcode(Ops.llmovrld_u); break;
                        case "int256":
                            op = new Opcode(Ops.llmovrld); break;
                        case "float":
                            op = new Opcode(Ops.fmovrld); break;
                        case "double":
                            op = new Opcode(Ops.dmovrld); break;
                        case "float128":
                            op = new Opcode(Ops.dfmovrld); break;
                        case "float256":
                            op = new Opcode(Ops.ddmovrld); break;
                        case "String":
                        case "string":
                            op = new Opcode(Ops.csmovrld); break;
                        default:
                            op = new Opcode(Ops.amovrld); break;
                    }
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
                default:
                    if (v.isField)
                    {
                        switch (v.type)
                        {
                            case "byte":
                            case "char":
                                op = new Opcode(Ops.bmov); break;
                            case "short":
                                op = new Opcode(Ops.smov); break;
                            case "int":
                                op = new Opcode(Ops.imov); break;
                            case "long":
                                op = new Opcode(Ops.lmov); break;
                            case "int128":
                                op = new Opcode(Ops.limov); break;
                            case "int256":
                                op = new Opcode(Ops.llmov); break;
                            case "ubyte":
                            case "uchar":
                                op = new Opcode(Ops.bmov); break;
                            case "ushort":
                                op = new Opcode(Ops.smov); break;
                            case "uint":
                                op = new Opcode(Ops.imov); break;
                            case "ulong":
                                op = new Opcode(Ops.lmov); break;
                            case "uint128":
                                op = new Opcode(Ops.limov); break;
                            case "uint256":
                                op = new Opcode(Ops.llmov); break;
                            case "float":
                                op = new Opcode(Ops.fmov); break;
                            case "double":
                                op = new Opcode(Ops.dmov); break;
                            case "float128":
                                op = new Opcode(Ops.dfmov); break;
                            case "float256":
                                op = new Opcode(Ops.ddmov); break;
                            case "String":
                            case "string":
                                op = new Opcode(Ops.csmov); break;
                            default:
                                op = new Opcode(Ops.amov); break;
                        }
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

    private enum CompileType{
        NONE,
        STRAIGHT_TO_REGISTER,
    }

    private void compile(Token token, Opcode ops)
    {
        this.compile(token, ops, CompileType.NONE);
    }

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
            case IDENTIFIER:
                ops.add(loadVariable(token.toString(), ops, compileType));
                break;
            case MULTIPLICATION:
                compile(token.getTokens().get(0), ops, CompileType.STRAIGHT_TO_REGISTER);
                compile(token.getTokens().get(1), ops, CompileType.STRAIGHT_TO_REGISTER);
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

        switch (type)
        {
            case "byte":
            case "char":
                op = new Opcode(Ops.bconst_e); break;
            case "short":
                op = new Opcode(Ops.sconst_e); break;
            case "int":
                op = new Opcode(Ops.iconst_e); break;
            case "long":
                op = new Opcode(Ops.lconst_e); break;
            case "int128":
                op = new Opcode(Ops.liconst_e); break;
            case "int256":
                op = new Opcode(Ops.llconst_e); break;
            case "ubyte":
            case "uchar":
                op = new Opcode(Ops.bconst_e); break;
            case "ushort":
                op = new Opcode(Ops.sconst_e); break;
            case "uint":
                op = new Opcode(Ops.iconst_e); break;
            case "ulong":
                op = new Opcode(Ops.lconst_e); break;
            case "uint128":
                op = new Opcode(Ops.liconst_e); break;
            case "uint256":
                op = new Opcode(Ops.llconst_e); break;
            case "float":
                op = new Opcode(Ops.fconst_e); break;
            case "double":
                op = new Opcode(Ops.dconst_e); break;
            case "float128":
                op = new Opcode(Ops.dfconst_e); break;
            case "float256":
                op = new Opcode(Ops.ddconst_e); break;
            case "String":
            case "string":
                default:
                    op = new Opcode(Ops.aconst_null); break;
        }

        ops.add(new Opcode(-1, "declare(" + name + ", " + type + ")").add(op));

        storeLocalVariable(name, type, new LinkedHashSet<>(declaration.getModifiers()));
    }

    private var storeLocalVariable(String name, String type, Set<Modifier> modifiers)
    {
        var v = new var(name, type, modifiers);

        localVariableTable.put(name, v);
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

    private Opcode storeByteInput(String input)
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

    private Opcode storeShortInput(String input)
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

    private Opcode storeIntInput(String input)
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

    private Opcode storeInput(String input, String type)
    {
        Opcode op = null;

        switch (type)
        {
            case "byte":
            case "char":
            case "ubyte":
            case "uchar":
                op = storeByteInput(input); break;
            case "short":
            case "ushort":
                op = storeShortInput(input); break;
            case "uint":
            case "int":
                op = storeIntInput(input); break;
            case "ulong":
            case "long":
                op = storeLongInput(input); break;
            case "int128":
//                op = new Opcode(Ops.liconst_e); break;
            case "int256":
//                op = new Opcode(Ops.llconst_e); break;
            case "uint128":
//                op = new Opcode(Ops.liconst_e); break;
            case "uint256":
//                op = new Opcode(Ops.llconst_e); break;
            case "float":
                op = storeFloatInput(input); break;
            case "double":
                op = storeDoubleInput(input); break;
            case "float128":
//                op = new Opcode(Ops.dfconst_e); break;
            case "float256":
//                op = new Opcode(Ops.ddconst_e); break;
            case "String":
            case "string":
                op = storeStringInput(input);
                break;
            default:
                op = new Opcode(Ops.aconst_null); break;
        }

        return op;
    }

    private void initializeVariable(Token token, Opcode ops)
    {
        Token varn = token.getTokens().get(0);
        Token valv = token.getTokens().get(1).getTokens().get(0);

        var v = getLocalVariable(varn.toString());

        if (v.isField)
            ops.add(new Opcode(-1, "init (" + varn.toString() + ", " + valv.toString() + ")").add(putInput(valv, v.type, v.localvarlocation, ops)));
        else
            ops.add(new Opcode(-1, "init (" + varn.toString() + ", " + valv.toString() + ")").add(storeInput(valv.toString(), v.type)));
    }

    private Opcode putByteInput(Token input, Opcode ops)
    {
        Opcode op = null;

        return op;
    }

    private Opcode putShortInput(Token input, Opcode ops)
    {
        Opcode op = null;

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

        return op;
    }

    private Opcode putDoubleInput(Token input, Opcode ops)
    {
        Opcode op = null;

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

        return op;
    }

    private Opcode putInput(Token input, String type, long index, Opcode ops)
    {
        Opcode op = new Opcode(-1, "put into field");

        switch (type)
        {
            case "byte":
            case "char":
            case "ubyte":
            case "uchar":
                op.add(putByteInput(input, op), op).add(new Opcode(Ops.ptb)); break;
            case "short":
            case "ushort":
                op.add(putShortInput(input, op), op).add(new Opcode(Ops.pts).add(Opcode.convertLong(index))); break;
            case "uint":
            case "int":
                op.add(putIntInput(input, op)).add(new Opcode(Ops.pti).add(Opcode.convertLong(index))); break;
            case "ulong":
            case "long":
                op.add(putLongInput(input, op)).add(new Opcode(Ops.ptl).add(Opcode.convertLong(index))); break;
            case "int128":
        //                op = new Opcode(Ops.liconst_e); break;
            case "int256":
        //                op = new Opcode(Ops.llconst_e); break;
            case "uint128":
        //                op = new Opcode(Ops.liconst_e); break;
            case "uint256":
        //                op = new Opcode(Ops.llconst_e); break;
            case "float":
                op.add(putFloatInput(input, op)).add(new Opcode(Ops.ptf).add(Opcode.convertLong(index))); break;
            case "double":
                op.add(putDoubleInput(input, op)).add(new Opcode(Ops.ptd).add(Opcode.convertLong(index))); break;
            case "float128":
        //                op = new Opcode(Ops.dfconst_e); break;
            case "float256":
        //                op = new Opcode(Ops.ddconst_e); break;
            case "String":
            case "string":
                op.add(putStringInput(input, op)).add(new Opcode(Ops.ptcs).add(Opcode.convertLong(index)));
                break;
            default:
                op = new Opcode(Ops.aconst_null); break;
        }

        return op;
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