package com.riverssen.core.mpp.compiler;

import com.riverssen.core.mpp.Executable;

import java.util.HashMap;
import java.util.Map;

public class AST
{
    private Executable              executable;
    private Map<String, Long>       stack;
    private Map<String, Long>       memory;
    private Opcode                  opcode;
    private long                    target;

    public AST(Token root, Method method, GlobalSpace space)
    {
        this.executable = new Executable();
        this.opcode     = new Opcode(-1, method.getName());
        this.stack      = new HashMap<>();
        this.memory     = new HashMap<>();
        this.compile(root, method, space, opcode);
    }

    private void compile(Token root, Method method, GlobalSpace space, Opcode opcode)
    {
        for (Token token : root)
        {
            switch (token.getType())
            {
                case IDENTIFIER:
                    break;
                case INITIALIZATION:
                    break;
                case EMPTY_DECLARATION:
                        emptyDeclaration(root, token, method, space, opcode);
                    break;
                case FULL_DECLARATION:
                        fullDeclaration(root, token, method, space, opcode);
                    break;
                case MULTIPLICATION:
                    break;
            }
        }
    }

    private void emptyDeclaration(Token root, Token token, Method method, GlobalSpace space, Opcode opcode)
    {
        String type = token.getTokens().get(0).toString();
        String name = token.getTokens().get(1).toString();

        boolean isReference = token.isModifier(Modifier.REFERENCE);
        boolean isPointer = token.isModifier(Modifier.POINTER);

        if (isReference && isPointer)
        {
            System.err.println("reference to pointer not allowed.");
            System.err.println("at line: " + token.getLine());
            System.exit(0);
        }

        if (isPointer)
            opcode.add(new Opcode(instructions.malloc_, "empty pointer to '" + name + "'").add(Opcode.convertLong(space.sizeof(type))));
        else
            opcode.add(new Opcode(instructions.push, "empty stack-pointer to '" + name + "'").add(Opcode.convertLong(space.sizeof(type))));
    }

    private void fullDeclaration(Token root, Token token, Method method, GlobalSpace space, Opcode opcode)
    {
        String type = token.getTokens().get(0).toString();
        String name = token.getTokens().get(1).toString();
        Token value = token.getTokens().get(2);

        boolean isReference = token.isModifier(Modifier.REFERENCE);
        boolean isPointer = token.isModifier(Modifier.POINTER);

        if (isReference && isPointer)
        {
            System.err.println("reference to pointer not allowed.");
            System.err.println("at line: " + token.getLine());
            System.exit(0);
        }

        Opcode declaration = null;

        if (isPointer)
        {
            opcode.add(declaration = new Opcode(instructions.malloc_, "pointer to '" + name + "'").add(Opcode.convertLong(space.sizeof(type))));
//            target = memory.size();
//            memory.put(name, (long) memory.size());
            target = stack.size();
            stack.put(name, (long) stack.size());
        }
        else
        {
            opcode.add(declaration = new Opcode(instructions.push, "stack-pointer to '" + name + "'").add(Opcode.convertLong(space.sizeof(type))));
            target = stack.size();
            stack.put(name, (long) stack.size());
        }

        compile(value, method, space, declaration);
    }

    public Executable getExecutable()
    {
        return executable;
    }

    public Opcode getOpcode()
    {
        return opcode;
    }
}