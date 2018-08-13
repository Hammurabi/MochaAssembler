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

package com.riverssen.core.utils;

import java.io.*;
import java.math.BigInteger;

public class Serializer implements AutoCloseable
{
    private OutputStream          baos;
    private DataOutputStream      dos;

    public Serializer(OutputStream stream)
    {
        this.baos = stream;
        this.dos  = new DataOutputStream(baos);
    }

    public Serializer()
    {
        this(new ByteArrayOutputStream());
    }

    public void flush() throws IOException
    {
        dos.flush();
    }

    public void write(byte data[]) throws IOException
    {
        dos.write(data);
    }

    public void write(int b) throws IOException
    {
        dos.write(b);
    }

    public void writeInt(int i) throws IOException
    {
        dos.writeInt(i);
    }

    public void writeLong(long i) throws IOException
    {
        dos.writeLong(i);
    }

    public void writeBigInteger(BigInteger i) throws IOException
    {
        dos.writeInt(i.toByteArray().length);
        dos.write(i.toByteArray());
    }

    public void writeFloat(float i) throws IOException
    {
        dos.writeFloat(i);
    }

    public void writeDouble(double i) throws IOException
    {
        dos.writeDouble(i);
    }

    public void writeUTF(String utf) throws IOException
    {
        dos.writeUTF(utf);
    }

    @Override
    public void close() throws IOException
    {
        dos.close();
    }

    public DataOutputStream asDataOutputStream()
    {
        return dos;
    }

    public byte[] getBytes()
    {
        return ((ByteArrayOutputStream)baos).toByteArray();
    }
}
