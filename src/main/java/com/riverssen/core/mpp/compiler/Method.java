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

import com.riverssen.core.mpp.exceptions.CompileException;
import com.riverssen.core.mpp.Opcode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.*;

public class Method extends Container implements Serializable
{
    private static final Stack<Container> stack = new Stack<>();
    private Set<String> arguments;
    private String      returnType;
    private int         offset;
    private Token       body;

    public Method(Token token) throws CompileException
    {
        this.name       = token.getTokens().get(0).toString();
        this.arguments  = new LinkedHashSet<>();
        for(Token tok : token.getTokens().get(2).getTokens())
            this.arguments.add(tok.getTokens().get(1).toString());
        this.returnType = token.getTokens().get(1).toString();

        this.body       = token.getTokens().get(3);
    }

    public Method(String name)
    {
        this.name       = name;
    }

    public Method(String name, Opcode opcode[])
    {
        this.name   = name;
    }

    protected   void    setOffset(int offset)
    {
        this.offset = offset;
    }

    public      int     getOffset()
    {
        return this.offset;
    }

    public String getReturnType()
    {
        return returnType;
    }

    private static ByteBuffer createBufferFromTokens(Token token) throws CompileException
    {
        HashMap<String, java.lang.Integer> stack = new HashMap<>();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream      stream1= new DataOutputStream(stream);

        try{
            for(Token tok : token.getTokens())
            {
                switch (tok.getType())
                {
                    case FULL_DECLARATION:
                            String name  = tok.getTokens().get(0).toString();
                            String type  = tok.getTokens().get(1).toString();
                            Token  value = tok.getTokens().get(2);

                            if(stack.containsKey(name)) throw new CompileException("object '" + name + "' already defined", tok);
                            stack.put(name, stack.size());

//                            stream1.writeShort(PUSH_FLOAT);
                            if(type.equals("int"))
                            {
                                if(value.getTokens().get(0).getType().equals(Token.Type.INPUT))
                                {
//                                    if(!value.getRoot().get(0).getRoot().get(0).getType().equals(Token.Type.NUMBER)) throw new CompileException("initialization exception: cannot cast from ")
//                                    stream1.writeShort(PUSH_INT);
                                    stream1.writeLong(Long.parseLong(value.getTokens().get(0).getTokens().get(0)
                                            .toString()));
                                }
                            }
                        break;
                }
            }

            stream1.flush();
            stream1.close();
        } catch (Exception e)
        {
            throw new CompileException("failed to compile function", token);
        }
        return ByteBuffer.wrap(stream.toByteArray());
    }

    @Override
    public Container call(Container self, Container... args)
    {
        setField("this", self);
        setField("self", self);

        int i = 0;

        for(String string : arguments)
            setField(string, args[i ++]);

        Container value = VOID;

        for(Token token : body.getTokens())
        {
            try
            {
                value = token.interpret(this, self, this, self, false, args);
            } catch (CompileException e)
            {
                e.printStackTrace();
            }
        }

        return value;

//        stack.clear();
//        stack.push(self);
//        for(Container arg : args)
//            stack.push(arg);
//
//        Container a = null;
//        Container b = null;
//
//        while (opcodes.remaining() > 0)
//        {
//            switch (opcodes.getShort())
//            {
//                case PUSH_INT:
//                    stack.push(new Integer(opcodes.getLong()));
//                    break;
//                case PUSH_UINT:
//                    stack.push(new Uint(opcodes.getLong()));
//                    break;
//                case PUSH_UINT256:
//                    byte bi[] = new byte[32];
//                    opcodes.get(bi);
//                    stack.push(new uint256(new BigInteger(bi)));
//                    break;
//                case PUSH_FLOAT:
//                    stack.push(new Float(opcodes.getDouble()));
//                    break;
//
//                case ADD:
//                    b = stack.pop();
//                    a = stack.pop();
//                    stack.push(a.addition(b));
//                    break;
//                case SUB:
//                    b = stack.pop();
//                    a = stack.pop();
//                    stack.push(a.submission(b));
//                    break;
//                case MLT:
//                    b = stack.pop();
//                    a = stack.pop();
//                    stack.push(a.multiplication(b));
//                    break;
//                case DIV:
//                    b = stack.pop();
//                    a = stack.pop();
//                    stack.push(a.subdivision(b));
//                    break;
//                case POW:
//                    b = stack.pop();
//                    a = stack.pop();
//                    stack.push(a.power(b));
//                    break;
//                case ASSERT:
//                    b = stack.pop();
//                    a = stack.pop();
//                    stack.push(new Boolean(a.equals(b)));
//                    break;
//            }
//        }
    }

    @Override
    public void write(DataOutputStream stream)
    {

    }

    @Override
    public void read(DataInputStream stream)
    {

    }

    @Override
    public String toString()
    {
        return "method: " + name + " " + returnType + " {}";
    }
}
