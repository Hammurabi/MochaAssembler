package com.riverssen.core.mpp.compiler;

import com.riverssen.core.mpp.Executable;

import java.util.HashMap;
import java.util.Map;

public class AST
{
    private Executable              executable;
    private Map<String, Long>       stack;
    private Map<String, String>     types;
    private Map<String, Long>       memory;
    private Opcode                  opcode;
    private long                    target;
    private boolean                 display_type;

    public AST(Token root, Method method, GlobalSpace space)
    {
        this(root, method, space, null);
    }

    public AST(Token root, Method method, GlobalSpace space, AST ast)
    {
        this.executable = new Executable();
        this.opcode     = new Opcode(-1, method.getName());
        this.stack      = new HashMap<>(ast != null ? ast.memory : new HashMap<>());
        this.memory     = new HashMap<>(ast != null ? ast.stack : new HashMap<>());
        this.types      = new HashMap<>(ast != null ? ast.types : new HashMap<>());
        this.target     = -1;

        if (method.getParent() != null)
        {
            stack.put(method.getName() + ".this", (long) stack.size());
            types.put(method.getName() + ".this", method.getParent().getName());
        }

        for (Field field : method.getArguments())
        {
            stack.put(field.getName(), (long) stack.size());
            types.put(field.getName(), field.getTypeName());
        }

        this.compile(root, method, space, opcode);
    }

    private void compile(Token root, Method method, GlobalSpace space, Opcode opcode)
    {
        long temp = target;
        target = -1;

        for (Token token : root)
        {
            switch (token.getType())
            {
                case IDENTIFIER:
                    Opcode id_c = new Opcode(-1, "identifier access '" + token.toString() + "'");
                    if (token.toString().equals("this"))
                        id_c.add(Opcode.convertLong(stack.get(method.getName() + ".this")));
                    else if (stack.containsKey(token.toString()))
                        id_c.add(new Opcode(-1, "access from stack '" + token.toString() + "'").add(Opcode.convertLong(stack.get(token.toString()))));
                    else {
                        if (method.getParent() != null && method.getParent().contains(token.toString(), method.getParent().getName(), space))
                        {
                            id_c.add(new Opcode(instructions.memory_read, "move pointer to stack '" + method.getParent().getName() + "->" + token.toString() + "'").add(new Opcode(-1, "parent pointer (this)").add(Opcode.convertLong(stack.get(method.getName() + ".this"))), new Opcode(-1, "location").add(Opcode.convertLong(method.getParent().getLocation(token.toString(), method.getParent().getName(), space)))));
                        }
                        else if (space.getGlobalMethods().containsKey(token.toString()) || space.getGlobalTypes().containsKey(token.toString()) || space.getGlobalFields().containsKey(token.toString()))
                        {
                            if (space.getGlobalFields().containsKey(token.toString()))
                            {
                                id_c.add(new Opcode(instructions.memory_load, "move pointer to stack 'globalspace->" + token.toString() + "'").add(new Opcode(-1, "location").add(Opcode.convertLong(space.getLocation(token.toString())))));
                            }
                        } else {
                            System.err.println("'" + token.toString() + "' not found.");
                            System.err.println("at line: " + token.getLine());
                            System.exit(0);
                        }
                    }

                    opcode.add(id_c);
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
                    Opcode multiplication = new Opcode(-1, "multiplication");

                    compile(token, method, space, multiplication);

                    multiplication.add(new Opcode(instructions.op_mul, "multiply"));
                    opcode.add(multiplication);
                    break;
            }
        }

        target = temp;
        if (target >= 0)
            opcode.add(new Opcode(instructions.stack_set, "assign value").add(Opcode.convertLong(target)));
    }

    private int getTypeOf(String string, GlobalSpace space)
    {
        if (types.containsKey(string))
            return space.getGlobalTypes().get(types.get(string)).getType();
//        if (stack.containsKey(string))
//            return types.get(string);
//        else {
//            if (method.getParent() != null && method.getParent().contains(token.toString(), method.getParent().getName(), space))
//            {
//                id_c.add(new Opcode(instructions.memory_read, "move pointer to stack '" + method.getParent().getName() + "->" + token.toString() + "'").add(new Opcode(-1, "parent pointer (this)").add(Opcode.convertLong(stack.get(method.getName() + ".this"))), new Opcode(-1, "location").add(Opcode.convertLong(method.getParent().getLocation(token.toString(), method.getParent().getName(), space)))));
//            }
//            else if (space.getGlobalMethods().containsKey(token.toString()) || space.getGlobalTypes().containsKey(token.toString()) || space.getGlobalFields().containsKey(token.toString()))
//            {
//                if (space.getGlobalFields().containsKey(token.toString()))
//                {
//                    id_c.add(new Opcode(instructions.memory_load, "move pointer to stack 'globalspace->" + token.toString() + "'").add(new Opcode(-1, "location").add(Opcode.convertLong(space.getLocation(token.toString())))));
//                }
//            } else {
//                System.err.println("'" + token.toString() + "' not found.");
//                System.err.println("at line: " + token.getLine());
//                System.exit(0);
//            }
//        }
        return 0;
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
        } else if (isReference)
        {
            System.err.println("reference declaration not allowed.");
            System.err.println("at line: " + token.getLine());
            System.exit(0);
        }

        if (isPointer)
        {
            opcode.add(new Opcode(instructions.malloc_, "empty pointer to '" + name + "' stack('" + stack.size() + "')").add(Opcode.convertLong(space.sizeof(type))));
            target = stack.size();
            stack.put(name, (long) stack.size());
            types.put(name, type + " *");
        }
        else
        {
            opcode.add(new Opcode(instructions.push, "empty stack-pointer to '" + name + "' stack('" + stack.size() + "')").add(Opcode.convertLong(space.sizeof(type))));
            target = stack.size();
            stack.put(name, (long) stack.size());
            types.put(name, type);
        }
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
        } else if (isReference)
        {
            System.err.println("reference declaration not allowed.");
            System.err.println("at line: " + token.getLine());
            System.exit(0);
        }

        Opcode declaration = null;

        if (isPointer)
        {
            opcode.add(declaration = new Opcode(instructions.malloc_, "pointer to '" + name + "' stack('" + stack.size() + "')").add(Opcode.convertLong(space.sizeof(type))));
//            target = memory.size();
//            memory.put(name, (long) memory.size());
            target = stack.size();
            stack.put(name, (long) stack.size());
            types.put(name, type + " *");
        }
        else
        {
            opcode.add(declaration = new Opcode(instructions.push, "stack-pointer to '" + name + "' stack('" + stack.size() + "')").add(Opcode.convertLong(space.sizeof(type))));
            target = stack.size();
            stack.put(name, (long) stack.size());
            types.put(name, type);
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