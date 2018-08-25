package com.riverssen.core.mpp.compiler;

import com.riverssen.core.mpp.Executable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
    private AST                     ast;

    public AST(Token root, Method method, GlobalSpace space)
    {
        this(root, method, space, null);
    }

    private AST()
    {
        this.stack      = new HashMap<>();
        this.memory     = new HashMap<>();
        this.types      = new HashMap<>();
    }

    public AST(Token root, Method method, GlobalSpace space, AST ast)
    {
        if (ast == null)
            ast = new AST();
        this.ast = ast;

        this.executable = new Executable();
        this.opcode     = new Opcode(-1, method.getName());
        this.stack      = new HashMap<>(ast.stack);
        this.memory     = new HashMap<>(ast.memory);
        this.types      = new HashMap<>(ast.types);
        this.target     = -1;

        if (method.getParent() != null)
        {
            stack.put(qualifiedName("this", method), (long) stack.size());
            types.put(qualifiedName("this", method), method.getParent().getName());
        }

        for (Field field : method.getArguments())
        {
            stack.put(qualifiedName(field.getName(), method), (long) stack.size());
            types.put(qualifiedName(field.getName(), method), field.getTypeName());
        }

        this.compile(root, method, space, opcode);
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private String sha(String data)
    {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            return bytesToHex(hash);
        } catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

        return null;
    }

    private String qualifiedName(String name, Method method)
    {
        return sha(method.getName() + " " + this) + " " + name;
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
                        id_c.add(Opcode.convertLong(stack.get(qualifiedName("this", method))));
                    else if (stack.containsKey(qualifiedName(token.toString(), method)))
                        id_c.add(new Opcode(-1, "access from stack '" + token.toString() + "'").add(Opcode.convertLong(stack.get(qualifiedName(token.toString(), method)))));
                    else {
                        if (method.getParent() != null && method.getParent().contains(token.toString(), method.getParent().getName(), space))
                        {
                            id_c.add(new Opcode(instructions.memory_read, "move pointer to stack '" + method.getParent().getName() + "->" + token.toString() + "'")
                                    .add(new Opcode(-1, "parent pointer (this)").add(Opcode.convertLong(stack.get(qualifiedName("this", method)))),
                                    new Opcode(-1, "location").add(Opcode.convertLong(method.getParent().getLocation(token.toString(), method.getParent().getName(), space)))));
                        }
                        else if (space.getGlobalMethods().containsKey(token.toString()) || space.getGlobalTypes().containsKey(token.toString()) || space.getGlobalFields().containsKey(token.toString()))
                        {
                            if (space.getGlobalFields().containsKey(token.toString()))
                            {
                                id_c.add(new Opcode(instructions.memory_load, "move pointer to stack 'globalspace->" + token.toString() + "'").add(new Opcode(-1, "location").add(Opcode.convertLong(space.getLocation(token.toString())))));
                            }
                        } else {
                            System.err.println("'" + token.toString() + "' not found 'resolve identifier'.");
                            System.err.println("at line: " + token.getLine());
                            System.err.println(stack);
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

                    Token a = token.getTokens().get(0);
                    Token b = token.getTokens().get(0);

                    /** check in case of object * object multiplication **/
                    if ((a.getType().equals(Token.Type.IDENTIFIER) && getTypeOf(a, a.toString(), method, space) < 0) || b.getType().equals(Token.Type.IDENTIFIER) && getTypeOf(b, b.toString(), method, space) < 0)
                    {
                        if (a.getType().equals(Token.Type.IDENTIFIER))
                        {
                            Method op = getStructOf(a, a.toString(), method, space).getMethod("*");

                            opcode.add(new Opcode(-1, "method call 'operator *(a, b)'").add(op.inline(this, space).getChildren()));
//                            if (! op.getReturnType().equals("void"))
//                                stack.put(System.currentTimeMillis() + "", (long) stack.size());
                        }
                    }
                    else
                    {
                        Opcode multiplication = new Opcode(-1, "multiplication");
                        compile(token, method, space, multiplication);
                        multiplication.add(new Opcode(instructions.op_mul, "multiply"));
                        opcode.add(multiplication);
                    }

                    break;
                case METHOD_CALL:
                    if (method.getParent() != null)
                    {
                        if (method.getParent().containsMethod(token.getTokens().get(0).toString()))
                        {
                            opcode.add(new Opcode(-1, "method call '" + token.getTokens().get(0).toString() + "'").add(method.getParent().getMethod(token.getTokens().get(0).toString()).inline(this, space)));
//                            if (! method.getParent().getMethod(token.getTokens().get(0).toString()).getReturnType().equals("void"))
//                                stack.put(System.currentTimeMillis() + "", (long) stack.size());
                        }
                        else {
                            System.err.println("method '" + token.getTokens().get(0).toString() + "' not found.");
                            System.out.println("at line: " + token.getLine());
                            System.exit(0);
                        }
                    } else {
                        if (space.getGlobalMethods().containsKey(token.getTokens().get(0).toString()))
                        {
                            opcode.add(new Opcode(-1, "method call '" + token.getTokens().get(0).toString() + "'").add(space.getGlobalMethods().get(token.getTokens().get(0).toString()).inline(this, space)));

//                            if (! space.getGlobalMethods().get(token.getTokens().get(0).toString()).getReturnType().equals("void"))
//                                stack.put(System.currentTimeMillis() + "", (long) stack.size());
                        }
                        else {
                            System.err.println("method '" + token.getTokens().get(0).toString() + "' not found in globalspace.");
                            System.out.println("at line: " + token.getLine());
                            System.exit(0);
                        }
                    }
                    break;
                case RETURN:
                        compile(token, method, space, opcode);
                    return;
            }
        }

        target = temp;
        if (target >= 0)
            opcode.add(new Opcode(instructions.stack_set, "assign value").add(Opcode.convertLong(target)));
    }

    private int getTypeOf(Token token, String string, Method method, GlobalSpace space)
    {
        if (types.containsKey(qualifiedName(string, method)))
            return space.getGlobalTypes().get(types.get(qualifiedName(string, method))).getType();
        else {
            if (stack.containsKey(qualifiedName(string, method)))
                return space.getGlobalTypes().get(qualifiedName(string, method)).getType();
            else {
                if (method.getParent() != null && method.getParent().contains(string, method.getParent().getName(), space))
                    return method.getParent().getField(string, method.getParent().getName()).getTypeStruct(space).getType();
                else if (space.getGlobalMethods().containsKey(string) || space.getGlobalTypes().containsKey(string) || space.getGlobalFields().containsKey(string))
                {
                    if (space.getGlobalFields().containsKey(string))
                        return space.getGlobalFields().get(string).getTypeStruct(space).getType();
                } else {
                    System.err.println("'" + string + "' not found 'getTypeOf'.");
                    System.err.println("at line: " + token.getLine());
                    System.exit(0);
                }
            }
        }

        return -1;
    }

    private Struct getStructOf(Token token, String string, Method method, GlobalSpace space)
    {
        if (types.containsKey(qualifiedName(string, method)))
            return space.getGlobalTypes().get(types.get(qualifiedName(string, method)));
        else {
            if (stack.containsKey(qualifiedName(string, method)))
                return space.getGlobalTypes().get(qualifiedName(string, method));
            else {
                if (method.getParent() != null && method.getParent().contains(string, method.getParent().getName(), space))
                    return method.getParent().getField(string, method.getParent().getName()).getTypeStruct(space);
                else if (space.getGlobalMethods().containsKey(string) || space.getGlobalTypes().containsKey(string) || space.getGlobalFields().containsKey(string))
                {
                    if (space.getGlobalFields().containsKey(string))
                        return space.getGlobalFields().get(string).getTypeStruct(space);
                } else {
                    System.err.println("'" + string + "' not found 'getStructOf'.");
                    System.err.println("at line: " + token.getLine());
                    System.exit(0);
                }
            }
        }

        return null;
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
            stack.put(qualifiedName(name, method), (long) stack.size());
            types.put(qualifiedName(name, method), type + " *");
        }
        else
        {
            opcode.add(new Opcode(instructions.push, "empty stack-pointer to '" + name + "' stack('" + stack.size() + "')").add(Opcode.convertLong(space.sizeof(type))));
            target = stack.size();
            stack.put(qualifiedName(name, method), (long) stack.size());
            types.put(qualifiedName(name, method), type);
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
            stack.put(qualifiedName(name, method), (long) stack.size());
            types.put(qualifiedName(name, method), type + " *");
        }
        else
        {
            opcode.add(declaration = new Opcode(instructions.push, "stack-pointer to '" + name + "' stack('" + stack.size() + "')").add(Opcode.convertLong(space.sizeof(type))));
            target = stack.size();
            stack.put(qualifiedName(name, method), (long) stack.size());
            types.put(qualifiedName(name, method), type);
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