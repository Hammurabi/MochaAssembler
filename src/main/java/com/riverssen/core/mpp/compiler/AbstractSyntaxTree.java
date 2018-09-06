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
    private Map<String, Long> localVariableTable;

    public AbstractSyntaxTree(Token child, Method method, GlobalSpace space)
    {
        localVariableTable = new HashMap<>();
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

        String type;

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