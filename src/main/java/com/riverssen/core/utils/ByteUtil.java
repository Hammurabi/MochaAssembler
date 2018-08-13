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

import com.riverssen.core.algorithms.Sha3;
import com.riverssen.core.headers.Exportable;
import com.riverssen.core.headers.HashAlgorithm;
import com.riverssen.core.headers.Serialisable;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class ByteUtil
{
    private static HashAlgorithm dEncoder = new Sha3();

    public static HashAlgorithm defaultEncoder()
    {
        return dEncoder;
    }

    public static byte[] encode(long serializable)
    {
        byte array[] = new byte[]{
                (byte) (serializable >> 56),
                (byte) (serializable >> 48),
                (byte) (serializable >> 40),
                (byte) (serializable >> 32),
                (byte) (serializable >> 24),
                (byte) (serializable >> 16),
                (byte) (serializable >> 8),
                (byte) serializable
        };

        return array;
    }

    public static byte[] encode(Serialisable serialisable)
    {
        return null;
    }

    public static void decode(Serialisable serialisable, byte data[])
    {
    }

    public static byte[] encodeHash(String hash)
    {
        return new BigInteger(hash).toByteArray();
    }

    public static String decodeHash(byte[] hash)
    {
        return HashUtil.hashToStringBase16(hash);
    }

    public static long decode(byte data[])
    {
        return ByteBuffer.wrap(data).getLong();
    }

    public static byte[] concatenate(byte[]...arrays)
    {
        int size = 0;
        for(byte[] array : arrays)
            size += array.length;

        byte concatenated[] = new byte[size];

        int index = 0;

        for(byte[] array : arrays)
        {
            System.arraycopy(array, 0, concatenated, index, array.length);

            index += array.length;
        }

        return concatenated;
    }

    public static String[] concatenate(String[]...arrays)
    {
        int size = 0;
        for(String[] array : arrays)
            size += array.length;

        String concatenated[] = new String[size];

        int index = 0;

        for(String[] array : arrays)
        {
            System.arraycopy(array, 0, concatenated, index, array.length);

            index += array.length;
        }

        return concatenated;
    }

    public static byte[] encodei(int serializable)
    {
        byte array[] = new byte[]{
                (byte) (serializable >> 24),
                (byte) (serializable >> 16),
                (byte) (serializable >> 8),
                (byte) serializable
        };

        return array;
    }

    public static int decodei(byte data[])
    {
        return ByteBuffer.wrap(data).getInt();
    }

    public static byte[] read(DataInputStream stream, int amt)
    {
        byte b[] = new byte[amt];

        try
        {
            stream.read(b);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return b;
    }

    public static byte[] readvariable(DataInputStream stream)
    {
        try
        {
            int length = stream.readShort();

            byte b[]   = new byte[length];

            stream.read(b);

            return b;
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return new byte[1];
    }

    public static void writeObject(ByteArrayOutputStream stream, Object object) throws Exception
    {
        ObjectOutputStream stream1 = new ObjectOutputStream(stream);

        stream1.writeObject(object);

        stream1.flush();
        stream1.close();
    }

    public static byte[] getBytes(Exportable exportable)
    {
        try{
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream s = new DataOutputStream(stream);

            exportable.export(s);

            s.flush();
            s.close();

            return stream.toByteArray();
        } catch (Exception e)
        {
            return new byte[0];
        }
    }

    public static byte[] trim(byte[] bytes, int i, int i1) {
        byte new_bytes[]    = new byte[i1 - i];

        int free            = 0;

        for(int index = i; index < i1; index ++)
            new_bytes[free ++] = bytes[index];
        return new_bytes;
    }

    public static String list(byte[] checksum) {
        StringBuilder builder = new StringBuilder("[");

        for(byte b : checksum)
            builder.append(b).append(", ");

        return builder.toString().substring(0, builder.length() - 2) + "]";
    }

    public static boolean equals(byte[] trim, byte[] trim1) {
        if(trim.length != trim1.length) return false;

        for(int i = 0; i < trim1.length; i ++)
            if(trim[i] != trim1[i]) return false;

        return true;
    }

    public static byte[] xor(byte[] hash_, byte[] prng) {
        byte bytes[] = new byte[hash_.length];

        for(int i = 0; i < hash_.length; i ++)
            bytes[i] = (byte) (hash_[i] ^ prng[i]);
        return bytes;
    }
}