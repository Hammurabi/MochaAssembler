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

package com.riverssen.core.mpp;

import com.riverssen.core.mpp.compiler.Field;
import com.riverssen.core.mpp.compiler.GlobalSpace;
import com.riverssen.core.mpp.compiler.StackTrace;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Executable
{
    public List<Byte>          op_codes;
    public List<String>        op_strns;
    public GlobalSpace         space;

    public Executable()
    {
        op_codes = new ArrayList<>();
        op_strns = new ArrayList<>();
    }

    public void add(int code)
    {
        op_codes.add((byte) code);
    }

    public void addAll(byte codes[])
    {
        for (int code : codes)
            op_codes.add((byte) code);
    }

    public void add(byte codes[])
    {
        for (int code : codes)
            op_codes.add((byte) code);
    }

    public void add(List<Byte> codes)
    {
        op_codes.addAll(codes);
    }

    public byte[] convertInt(int integer)
    {
        ByteBuffer _int_ = ByteBuffer.allocate(4);
        _int_.putInt(integer);
        _int_.flip();

        return _int_.array();
    }

    public byte[] convertLong(long integer)
    {
        ByteBuffer _int_ = ByteBuffer.allocate(8);
        _int_.putLong(integer);
        _int_.flip();

        return _int_.array();
    }

    public byte[] convertDouble(long integer)
    {
        ByteBuffer _int_ = ByteBuffer.allocate(8);
        _int_.putDouble(integer);
        _int_.flip();

        return _int_.array();
    }

    public void push_(Field field, StackTrace trace)
    {
        if (field.getValidType() < 255)
        {
        }
        else {
            add(instructions.push);
            add(convertLong(field.size(space)));
        }
    }
}
