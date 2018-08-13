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

public class LoopContainer extends Container implements Serializable
{
    Container value;
//    Container self;

    public LoopContainer(Container value)
    {
        this.value = value;
//        this.self  = self;
    }

    @Override
    public Container get(String name)
    {
        if (super.get(name) != null) return super.get(name);
        else if (value.get(name) != null) return value.get(name);
        else return value.get(name);
//        else return self.get(name);
    }

    @Override
    public void setField(String name, Container value)
    {
        if (value.get(name) != null) value.setField(name, value);
//        else if (self.get(name) != null) self.setField(name, value);
        else super.setField(name, value);
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
}
