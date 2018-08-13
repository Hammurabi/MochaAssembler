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
import com.riverssen.core.mpp.objects.Void;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static com.riverssen.core.mpp.compiler.Opcode.*;

public abstract class Container implements Serializable
{
    protected static final Container                    VOID = new Void();
    public    static final Container                    EMPTY = new Void();
    public                 Container                    global;
    protected              String                       name;
    private                Map<String, Container>       globalMap;
//    private                ByteBuffer                   opcodes;

    public Container()
    {
        this.globalMap = new HashMap<>();
    }

    protected void addNameSpace(Token token) throws CompileException
    {
        Namespace ns = new Namespace(token);
        ns.setGlobal(getGlobal());
        if(this.globalMap.containsKey(ns.getName())) throw new CompileException("namespace '" + ns.getName() + "' already defined.", token);
        this.globalMap.put(ns.getName(), ns);
    }

    protected void addClass(Token token) throws CompileException
    {
        Class ns = new Class(token, getGlobal());
        if(this.globalMap.containsKey(ns.getName())) throw new CompileException("class '" + ns.getName() + "' already defined.", token);
        this.globalMap.put(ns.getName(), ns);
    }

    protected void addMethod(Token token) throws CompileException
    {
        Method ns = new Method(token);
        ns.setGlobal(getGlobal());
        if(this.globalMap.containsKey(ns.getName())) throw new CompileException("method '" + ns.getName() + "' already defined.", token);
        this.globalMap.put(ns.getName(), ns);
    }

    protected void addDeclaration(Token token) throws CompileException
    {
        if(token.getType().equals(Token.Type.FULL_DECLARATION))
            token.interpret(this, this, this, this, false);
        else {
            Field ns = new Field(token);
            if(this.globalMap.containsKey(ns.getName())) throw new CompileException("field '" + ns.getName() + "' already defined.", token);
            this.globalMap.put(ns.getName(), ns);
        }
    }

    protected static Container execute(ByteBuffer opcodes, Stack<Container> stack, Container context, Container self, Container ...args)
    {
        stack.push(self);
        stack.push(context);

        while(opcodes.remaining() > 0)
        {
            short opcode = opcodes.getShort();

            switch (opcode)
            {
                case POP:
                    stack.pop();
                    break;
                case PUSH:
                    break;

                case ADD:
                    stack.push(stack.pop().addition(stack.pop()));
                    break;
                case SUB:
                    stack.push(stack.pop().submission(stack.pop()));
                    break;
                case MUL:
                    stack.push(stack.pop().multiplication(stack.pop()));
                    break;
                case DIV:
                    stack.push(stack.pop().subdivision(stack.pop()));
                    break;
                case MOD:
                    stack.push(stack.pop().modulo(stack.pop()));
                    break;
                case POW:
                    stack.push(stack.pop().power(stack.pop()));
                    break;

                case MOV:
                    stack.get(opcodes.get()).setField(getString(opcodes), stack.pop());
                    break;
                case LOD:
                    stack.push(stack.get(opcodes.getInt()).get(getString(opcodes)));
                    break;

                case CALL:
                    stack.get(opcodes.getInt()).callMethod(getString(opcodes));
                    break;
            }
        }

        return null;
    }

    private static String getString(ByteBuffer buffer)
    {
        byte b[] = new byte[buffer.get()];
        buffer.get(b);

        return new String(b);
    }

    public void setField(String name, Container value)
    {
        this.globalMap.put(name, value);
    }

    public String getName()
    {
        return name;
    }

    public Map<String, Container> getGlobalMap()
    {
        return globalMap;
    }

    public Container get(String name)
    {
        return globalMap.get(name);
    }

    public Container callMethod(String method, Container ...args)
    {
        return get(method).call(this, args);
    }

    public Container call(Container self, Container...args)
    {
        return VOID;
    }

    public java.lang.Object asJavaObject()
    {
        return this;
    }

    public Container addition(Container b)
    {
        return EMPTY;
    }

    public Container submission(Container b)
    {
        return EMPTY;
    }

    public Container multiplication(Container b)
    {
        return EMPTY;
    }

    public Container subdivision(Container b)
    {
        return EMPTY;
    }

    public Container modulo(Container b)
    {
        return EMPTY;
    }

    public Container sin(Container b)
    {
        return EMPTY;
    }

    public Container cos(Container b)
    {
        return EMPTY;
    }

    public Container power(Container b)
    {
        return EMPTY;
    }

    protected void setGlobal(Container global)
    {
        this.global = global;

        for(Container container : globalMap.values())
            container.setGlobal(global);
    }

    public Container bracketGet(Container container)
    {
        return VOID;
    }

    public Container bracketSet(Container container)
    {
        return VOID;
    }

    @Override
    public boolean equals(java.lang.Object obj)
    {
        return super.equals(obj);
    }

    public abstract void write(DataOutputStream stream);

    public abstract void read(DataInputStream stream);

    public Container getGlobal()
    {
        return global;
    }

    protected Container newInstance(Object ...args)
    {
        return null;
    }

    protected void setOpcodes(ByteBuffer byteBuffer)
    {
//        this.opcodes = byteBuffer;
    }

    public ByteBuffer getOpcodes()
    {
        return null;//opcodes;
    }

    public String getType()
    {
        return "object";
    }

    protected void initializeField(Container parent) throws CompileException
    {
    }
}
