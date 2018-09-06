package com.riverssen.core.mpp.compiler;

import com.riverssen.core.mpp.Executable;

import java.util.HashMap;
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

        public var(String name, String type, Set<Modifier> modifiers)
        {
            this.name = name;
            this.type = type;
            this.modifiers = modifiers;
            this.localvarlocation = lcllvts ++;
        }
    }
    private Map<String, var> localVariableTable;

    public AbstractSyntaxTree(Token child, Method method, GlobalSpace space)
    {
        localVariableTable = new HashMap<>();
        if (method.getParent() == null || method.getParent().getName().equals("VOID") || method.getParent().getName().equals("void"))
            self = null;
        else self = method.getParent();

        context = method;
        exe = new Executable();
        ops = new Opcode(-1, method.getName() + "()");

        for (Field field : method.getArguments())
            localVariableTable.put(field.getName(), new var(field.getName(), field.getTypeName(), field.getModifiers()));

        compile(child);
    }

    private void compile(Token token)
    {
        switch (token.getType())
        {
            case EMPTY_DECLARATION:
                declareEmptyVariable(token);
                break;
            case INITIALIZATION:
                initializeVariable(token);
                break;
            case BRACES:
                for (Token t : token)
                    compile(t);
                break;
        }
    }

    private void declareEmptyVariable(Token declaration)
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

        System.err.println("compiler error: local variable '" + name + "' does not exist.");
        System.exit(0);

        return null;
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
            default:
                op = new Opcode(Ops.aconst_null); break;
        }

        return op;
    }

    private void initializeVariable(Token token)
    {
        Token varn = token.getTokens().get(0);
        Token valv = token.getTokens().get(1).getTokens().get(0);


        var v = getLocalVariable(varn.toString());

        Opcode op = null;

        storeInput(valv.toString(), v.type);

        ops.add(new Opcode(-1, "init (" + varn.toString() + ", " + valv.toString() + ")").add(new Opcode(Ops.istore)));
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