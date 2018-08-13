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

import com.riverssen.core.RiverCoin;
import com.riverssen.core.headers.HashAlgorithm;
import com.riverssen.core.mpp.exceptions.CompileException;
import com.riverssen.core.mpp.objects.*;
import com.riverssen.core.mpp.objects.Float;
import com.riverssen.core.mpp.objects.Integer;

import java.io.*;

public class Namespace extends Container implements Serializable
{
    public Namespace(Token ns) throws CompileException
    {
        if (ns.getType().equals(Token.Type.ROOT)) {
            name = "global";
            setGlobal();
        }
        else if (ns.getType().equals(Token.Type.NAMESPACE)) name = ns.toString();

        for (Token token : ns.getTokens())
            if (token.getType().equals(Token.Type.NAMESPACE)) addNameSpace(token);
            else if (token.getType().equals(Token.Type.CLASS_DECLARATION)) addClass(token);
            else if (token.getType().equals(Token.Type.METHOD_DECLARATION)) addMethod(token);
            else if (token.getType().equals(Token.Type.EMPTY_DECLARATION)) addDeclaration(token);
            else if (token.getType().equals(Token.Type.FULL_DECLARATION)) addDeclaration(token);

        if (ns.getType().equals(Token.Type.ROOT))
            setGlobal();
    }

    public static void check(Token methodCall)
    {
        if(methodCall.getTokens().get(0).toString().equals("encrypt"))
            methodCall.setCost(new RiverCoin("0.0015").toBigInteger());

        if(methodCall.getTokens().get(0).toString().equals("sha256"))
            methodCall.setCost(new RiverCoin("0.001").toBigInteger());

        if(methodCall.getTokens().get(0).toString().equals("sha3"))
            methodCall.setCost(new RiverCoin("0.001").toBigInteger());

        if(methodCall.getTokens().get(0).toString().equals("keccak"))
            methodCall.setCost(new RiverCoin("0.001").toBigInteger());

        if(methodCall.getTokens().get(0).toString().equals("sha512"))
            methodCall.setCost(new RiverCoin("0.005").toBigInteger());

        if(methodCall.getTokens().get(0).toString().equals("sha3"))
            methodCall.setCost(new RiverCoin("0.001").toBigInteger());

        if(methodCall.getTokens().get(0).toString().equals("keccak"))
            methodCall.setCost(new RiverCoin("0.001").toBigInteger());
    }

    public void setGlobal()
    {
        setGlobal(this);

        setField("int", new Integer(0));
        setField("string", new StringObject("null"));
        setField("String", new StringObject("null"));
        setField("uint", new Integer(0));
        setField("uint256", new uint256("0"));
        setField("float", new Float(0));
        setField("double", new Float(0));
        setField("float256", new Float(0));

        setField("set", new Container(){
            @Override
            public Container call(Container self, Container... args)
            {
                return new set();
            }

            @Override
            public void write(DataOutputStream stream)
            {

            }

            @Override
            public void read(DataInputStream stream)
            {

            }
        });
        setField("mapped_set", new Container(){
            @Override
            public Container call(Container self, Container... args)
            {
                return new mapped_set();
            }

            @Override
            public void write(DataOutputStream stream)
            {

            }

            @Override
            public void read(DataInputStream stream)
            {

            }
        });
    }

    public byte[] getStateChange(HashAlgorithm algorithm) throws Exception
    {
        if(getGlobal() != this) throw new Exception("Namespace '" + name + "' is not root");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);

        objectOutputStream.writeObject(this);

        objectOutputStream.flush();
        objectOutputStream.close();

        return algorithm.encode(stream.toByteArray());
    }

    @Override
    public void write(DataOutputStream stream)
    {

    }

    @Override
    public void read(DataInputStream stream)
    {

    }
}
