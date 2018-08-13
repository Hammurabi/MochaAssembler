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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class Integer extends Container implements Serializable
{
    long value;

    public Integer(long value)
    {
        this.value = value;
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
    protected Container newInstance(Object... args)
    {
        return new Integer(Long.parseLong(args[0].toString().replaceAll("\\_", "")));
    }

    @Override
    public Container addition(Container b)
    {
        if (b instanceof Integer)
            return new Integer(this.value + ((Integer) b).value);
        else if(b instanceof Float)
            return new Float(this.value + ((Float) b).value);
        else if(b instanceof uint256)
            return new uint256(new BigInteger("" + value).add(((uint256) b).value));
        else if(b instanceof Number)
            return new Number(new BigDecimal(this.value).add(((Number) b).value));
        else return EMPTY;
    }

    @Override
    public Container submission(Container b)
    {
        if (b instanceof Integer)
            return new Integer(this.value - ((Integer) b).value);
        else if(b instanceof Float)
            return new Float(this.value - ((Float) b).value);
        else if(b instanceof uint256)
            return new uint256(new BigInteger("" + value).subtract(((uint256) b).value));
        else if(b instanceof Number)
            return new Number(new BigDecimal(this.value).subtract(((Number) b).value));
        else return EMPTY;
    }

    @Override
    public Container multiplication(Container b)
    {
        if (b instanceof Integer)
            return new Integer(this.value * ((Integer) b).value);
        else if(b instanceof Float)
            return new Float(this.value * ((Float) b).value);
        else if(b instanceof uint256)
            return new uint256(new BigInteger("" + value).multiply(((uint256) b).value));
        else if(b instanceof Number)
            return new Number(new BigDecimal(this.value).multiply(((Number) b).value));
        else return EMPTY;
    }

    @Override
    public Container subdivision(Container b)
    {
        if (b instanceof Integer)
            return new Integer(this.value / ((Integer) b).value);
        else if(b instanceof Float)
            return new Float(this.value / ((Float) b).value);
        else if(b instanceof uint256)
            return new uint256(new BigInteger("" + value).divide(((uint256) b).value));
        else if(b instanceof Number)
            return new Number(new BigDecimal(this.value).divide(((Number) b).value, 200, RoundingMode.HALF_DOWN));
        else return EMPTY;
    }

    @Override
    public Container modulo(Container b)
    {
        if (b instanceof Integer)
            return new Integer(this.value % ((Integer) b).value);
        else if(b instanceof Float)
            return new Float(this.value % ((Float) b).value);
        else if(b instanceof uint256)
            return new uint256(new BigInteger("" + value).mod(((uint256) b).value));
        else if(b instanceof Number)
            return new Number(new BigDecimal(this.value).divide(((Number) b).value));
        else return EMPTY;
    }

    @Override
    public String toString()
    {
        return value + "";
    }
}
