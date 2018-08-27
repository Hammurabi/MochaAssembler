/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Riverssen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.riverssen.core.mpp.compiler;

import java.util.*;

public class GlobalSpace
{
    private Map<String, Struct>     __globaltypes__;
    private Set<Method>             __globalmethods__;
    private Map<String, Field>      __globalfields__;
    private long                    __allmethods__;
    private List<Method>            __allthemethods__;

    public GlobalSpace()
    {
        __globaltypes__ =               new HashMap<>();
        __globalmethods__ =             new LinkedHashSet<>();
        __globalfields__ =              new HashMap<>();
        __allthemethods__ =             new ArrayList<>();

        __globaltypes__.put("char",     new Struct("__int8_t", 1, this, type.char_));
        __globaltypes__.put("uchar",    new Struct("u_int8_t", 1, this, type.uchar_));
        __globaltypes__.put("short",    new Struct("u_int8_t", 2, this, type.short_));
        __globaltypes__.put("ushort",   new Struct("u_int8_t", 2, this, type.ushort_));
        __globaltypes__.put("int",      new Struct("__int32_t", 4, this, type.int_));
        __globaltypes__.put("uint",     new Struct("u_int32_t", 4, this, type.uint_));
        __globaltypes__.put("long",     new Struct("__int64_t", 8, this, type.long_));
        __globaltypes__.put("ulong",    new Struct("u_int64_t", 8, this, type.ulong_));
        __globaltypes__.put("float",    new Struct("__float32_t", 4, this, type.float32_));
        __globaltypes__.put("double",   new Struct("__float64_t", 8, this, type.float64_));
        __globaltypes__.put("float128", new Struct("__float256_t", 8, this, type.float128_));
        __globaltypes__.put("float256", new Struct("__float256_t", 8, this, type.float256_));
        __globaltypes__.put("int128",   new Struct("__int128_t", 16, this, type.int128_));
        __globaltypes__.put("uint128",  new Struct("u_int128_t", 16, this, type.uint128_));
        __globaltypes__.put("int256",   new Struct("__int256_t", 32, this, type.int256_));
        __globaltypes__.put("uint256",  new Struct("u_int256_t", 32, this, type.uint256_));

        __globaltypes__.put("pointer",  new Struct("__pointer__", 8, this, type.pointer_));
        __globaltypes__.put("string",   new Struct("__string__", 8, this, type.c_string));
        __globaltypes__.put("ARRAY",    new Struct("__array__", 8, this, type.pointer_));
        addMethod("sizeof", new Method("sizeof", this)
        {
            @Override
            public Object call(Token token)
            {
                if (token.getTokens().size() != 1)
                {
                    System.err.println("sizeof(x) does not take more than one argument of (type-identifier).");
                    System.exit(0);
                }

                if (!token.getTokens().get(0).getType().equals(Token.Type.IDENTIFIER))
                {
                    System.err.println("sizeof(x) does not take more than one argument of (type-identifier).");
                    System.exit(0);
                }

                return __globaltypes__.get(token.getTokens().get(0).toString()).size();
            }
        });
        addMethod("memcpy", new Method("memcpy", this)
        {
            {
                getOpCodes().add((byte)0);
                setOpcodes(new Opcode(-1, "copy pointer"));
            }

            @Override
            public Opcode inline(AST ast, GlobalSpace space)
            {
                return getOpcodes();
            }
        });
        addMethod("free", new Method("free", this)
        {
            {
                getOpCodes().add((byte)0);
                setOpcodes(new Opcode(-1, "free pointer").add(new Opcode(instructions.free_, "free")));
            }

            @Override
            public Opcode inline(AST ast, GlobalSpace space)
            {
                return getOpcodes();
            }
        });
        addMethod("out", new Method("out", this)
        {
            {
                getOpCodes().add((byte)0);
            }
        });
        addMethod("exception", new Method("exception", this)
        {
            {
                getOpCodes().add((byte)0);
                setOpcodes(new Opcode(-1, "print exception and halt").add(new Opcode(instructions.print, "print").add(new Opcode(-1, "type")).add(new Opcode(type.c_string, "string"))).add(new Opcode(instructions.url_write, "halt")));
            }

            @Override
            public Opcode inline(AST ast, GlobalSpace space)
            {
                return getOpcodes();
            }
        });
        addMethod("wait", new Method("wait", this)
        {
            {
                getOpCodes().add((byte)0);
                setOpcodes(new Opcode(-1, "wait"));
            }

            @Override
            public Opcode inline(AST ast, GlobalSpace space)
            {
                return getOpcodes();
            }
        });
        addMethod("out", new Method("out", this)
        {
            {
                getOpCodes().add((byte)0);
                setOpcodes(new Opcode(-1, "print out").add(new Opcode(instructions.print, "print").add(new Opcode(-1, "type")).add(new Opcode(type.c_string, "string"))));
            }

            @Override
            public Opcode inline(AST ast, GlobalSpace space)
            {
                return getOpcodes();
            }
        });
        addMethod("array_equal", new Method("array_equal", this)
        {
            {
                getOpCodes().add((byte)0);
                setOpcodes(new Opcode(-1, "array_equal"));
            }

            @Override
            public Opcode inline(AST ast, GlobalSpace space)
            {
                return getOpcodes();
            }
        });
    }

    public Map<String, Struct> getGlobalTypes()
    {
        return __globaltypes__;
    }

//    public Map<String, Set<Method>> getGlobalMethods()
//    {
//        return __globalmethods__;
//    }

    public Map<String, Field> getGlobalFields()
    {
        return __globalfields__;
    }

    public long addMethod(Method method)
    {
        __allthemethods__.add(method);
        return __allmethods__ ++;
    }

    public long sizeof(String type)
    {
        if (__globaltypes__.containsKey(type))
            return __globaltypes__.get(type).size();

        else {
            System.err.println("type '" + type + "' doesn't exist in global space.");
            System.exit(0);
            return 0;
        }
    }

    public long getLocation(String name)
    {
        if (getGlobalFields().containsKey(name))
            return getGlobalFields().get(name).getLocation();
        return -1;
    }

    public static String getMethodName(String name, Set<Field> args)
    {
        if (args.size() == 0)
            return name + "()";

        String arguments = "(";

        for (Field field : args)
            arguments += field.getTypeName() + ", ";

        return name + arguments.substring(0, arguments.length() - 2) + ")";
    }

    public void addMethod(String methodName, Method method)
    {
        String qualifiedMethodName = getMethodName(methodName, method.getArguments());

        if (containsMethod(methodName, method.getArguments()))
        {
            System.err.println("err: method '" + qualifiedMethodName + "' already exists in global space.");
            System.exit(0);
        }

        __globalmethods__.add(method);
    }

    public Method getMethod(String methodName, Set<Field> arguments)
    {
        String method = getMethodName(methodName, arguments);

        if (containsMethod(methodName, arguments))
        {
            for (Method method_ : __globalmethods__)
            {
                if (methodName.equals(method_.getName()) && method_.matches(this, arguments))
                {

                }
            }
        }
        else {
            System.err.println("err: method '" + method + "' not found in globalspace.");
            System.exit(0);
        }

        return null;
    }

    public boolean containsMethod(String methodName, Set<Field> args)
    {
        String method = getMethodName(methodName, args);

        return __globalmethods__.containsKey(method);
    }
}
