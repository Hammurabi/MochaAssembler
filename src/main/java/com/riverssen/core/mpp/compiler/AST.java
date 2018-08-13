package com.riverssen.core.mpp.compiler;

import com.riverssen.core.mpp.exceptions.CompileException;
import com.riverssen.core.mpp.Opcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class AST
{
    private List<Opcode> opcodes;

    private HashMap<String, Class>  classes = new HashMap<>();
    private HashMap<String, Method> methods = new HashMap<>();
    private HashMap<String, Integer>sizes   = new HashMap<>();

    private Stack<String>           globalStack = new Stack<>();
    private HashMap<String, Integer>globalMemry = new HashMap<>();
    private int                     globalIndex = 0;

    public AST(ParsedProgram program) throws CompileException
    {
        Token root = program.getRoot();
        this.opcodes = new ArrayList<>();
        enterRoot(root);
    }

    private void enterRoot(Token root) throws CompileException
    {
        List<Token> classes = new ArrayList<>();

        for(Token token : root.getTokens())
            if(token.getType() == Token.Type.CLASS_DECLARATION)
                classes.add(token);

        for(Token clss : classes)
            enterClass(clss);
    }

    private void enterClass(Token clasz) throws CompileException
    {
        Class clss = new Class(clasz, null);
        if(classes.containsKey(clss.getName())) throw new CompileException("Class already exists", clasz);
        classes.put(clss.getName(), clss);

        //Because contracts cannot be initialized more than once, we create an object in their name
        newObject(clss.getName());
    }

    private List<Short> enterMethodBody(Token body) throws CompileException
    {
        List<Short> opcodes = new ArrayList<>();

        for(Token token : body.getTokens())
        {
            switch (token.getType())
            {
                case IF:
                        Token condition = token.getTokens().get(0);
//                        opcodes.add(com.com.thirdparty.thirdparty.core.mpp.compiler.Opcode.IF);
                        opcodes.addAll(enterMethodBody(condition));
                    break;
                case WHILE:
                    break;
                case FOR:
                    break;
                case EMPTY_DECLARATION:
                    break;
                case FULL_DECLARATION:
                    break;
                case PROCEDURAL_ACCESS:
                    break;
            }
        }

        return opcodes;
    }

    protected int push(String name) throws CompileException
    {
        globalStack.push(name);
        return globalStack.size() - 1;
    }

    protected int stackSize() throws CompileException
    {
        return globalStack.size();
    }

    protected int newObject(String name) throws CompileException
    {
        globalMemry.put(name, globalIndex++);

        return globalIndex - 1;
    }

    public List<Opcode> getOpcodes() throws CompileException
    {
        return opcodes;
    }

    public int sizeof(String type) throws CompileException
    {
        return sizes.get(type);
    }
}
