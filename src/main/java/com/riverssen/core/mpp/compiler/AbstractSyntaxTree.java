package com.riverssen.core.mpp.compiler;

import com.riverssen.core.mpp.Executable;

public class AbstractSyntaxTree
{
    private Struct self;
    private Method context;
    private Executable exe;
    private Opcode     ops;

    public AbstractSyntaxTree(Token child, Method method, GlobalSpace space)
    {
        if (method.getParent() == null || method.getParent().getName().equals("VOID") || method.getParent().getName().equals("void"))
            self = null;
        else self = method.getParent();

        context = method;
        exe = new Executable();
        ops = new Opcode(-1, method.getName() + "()");

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
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(1)); break;
            case "char":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(1)); break;
            case "short":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(2)); break;
            case "int":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(4)); break;
            case "long":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(8)); break;
            case "int128":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(16)); break;
            case "int256":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(32)); break;
            case "ubyte":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(1)); break;
            case "uchar":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(1)); break;
            case "ushort":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(2)); break;
            case "uint":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(4)); break;
            case "ulong":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(8)); break;
            case "uint128":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(16)); break;
            case "uint256":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(32)); break;
            case "float":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(4)); break;
            case "double":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(8)); break;
            case "float128":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(16)); break;
            case "float256":
                op = new Opcode(Ops.iconst_e).add(Opcode.convertByte(32)); break;
            case "String":
            case "string":
                op = new Opcode(Ops.aconst_null); break;
                default:
                    op = new Opcode(Ops.aconst_null); break;
        }

        ops.add(new Opcode(-1, "declare(" + name + ", " + type + ")").add(op));
    }

    private void initializeVariable(Token token)
    {
        Token var = token.getTokens().get(0);
        Token val = token.getTokens().get(1).getTokens().get(0);

        ops.add(new Opcode(-1, "init (" + var.toString() + ", " + val.toString() + ")").add(new Opcode(Ops.istore)));
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