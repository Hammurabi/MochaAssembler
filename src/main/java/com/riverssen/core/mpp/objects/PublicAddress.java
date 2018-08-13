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

public class PublicAddress extends Container implements Serializable
{
    com.riverssen.core.security.PublicAddress value;

    public PublicAddress(com.riverssen.core.security.PublicAddress value)
    {
        this.value = value;
    }

    @Override
    public Object asJavaObject()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return "address{"+value+"}";
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof PublicAddress)
        return ((PublicAddress) obj).value.equals(value);
        else if(obj instanceof StringObject)
            return ((String)((StringObject) obj).asJavaObject()).equals(value.toString());
        else return false;
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
