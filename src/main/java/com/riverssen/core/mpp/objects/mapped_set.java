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

package com.riverssen.core.mpp.objects;

import com.riverssen.core.mpp.compiler.Container;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class mapped_set extends Container implements Serializable
{
    HashMap<Container, Container> value;

    public mapped_set()
    {
        this.value = new HashMap<>();

        setField("add", new Container(){
            @Override
            public Container call(Container self, Container... args)
            {
                if(!value.containsKey(args[0]))
                    value.put(args[0], new set());

                value.get(args[0]).addition(args[1]);

                return Boolean.TRUE;
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

        setField("put", new Container(){
            @Override
            public Container call(Container self, Container... args)
            {
                if(!value.containsKey(args[0]))
                    value.put(args[0], new set());

                value.get(args[0]).addition(args[1]);

                return Boolean.TRUE;
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

        setField("remove", new Container(){
            @Override
            public Container call(Container self, Container... args)
            {
                value.remove(args[0]);
                return Boolean.TRUE;
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

        setField("get", new Container(){
            @Override
            public Container call(Container self, Container... args)
            {
                return value.get(args[0]);
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

        setField("contains", new Container(){
            @Override
            public Container call(Container self, Container... args)
            {
                for(Container container : args)
                    if(!value.containsKey(container)) return Boolean.FALSE;
                return Boolean.TRUE;
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

        setField("clear", new Container(){
            @Override
            public Container call(Container self, Container... args)
            {
                value.clear();
                return Boolean.TRUE;
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

    @Override
    public Object asJavaObject()
    {
        return value;
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
        return value.toString();
    }
}
