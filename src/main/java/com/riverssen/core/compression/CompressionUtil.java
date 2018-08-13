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

package com.riverssen.core.compression;

import com.riverssen.core.algorithms.Sha3;
import com.riverssen.core.utils.Base58;

import java.io.*;
import java.security.Security;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class CompressionUtil
{
    /** base58 compression yeilds a 0.75 efficiency IE: compress(base58(bytes(32))) = 33 bytes so an efficiency of 1.03125**/
    /** while base16 yeilds 0.5 IE: compress(base16(bytes(32)) = 32 bytes so an efficiency of 1**/
    /** base58 on a 32 byte array yeilds 44 bytes, instead of compressing down to 33 we could instead use the raw bytes **/
    /** and base16 on a 32 byte array yeilds 64 bytes **/
    private static final byte   base58CommonBytes[]           = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".getBytes();
    private static final float  base58CommonBytesFrequency[]  = new float   [58];
    private static final String base58Bits[]                  = {
            "000000"
    };

    public byte[] base58Compress(@Base58Hash byte hash[])
    {
        return hash;
    }

    public static void main(String...args)
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        System.out.println(Base58.encode(new Sha3().encode(new byte[3342])).length());
    }

    public static void compressData(byte data[], DataOutputStream outputStream) throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream  deflaterOutputStream  = new DeflaterOutputStream(byteArrayOutputStream);

        deflaterOutputStream.write(data);

        deflaterOutputStream.flush();
        deflaterOutputStream.close();

        outputStream.writeInt(byteArrayOutputStream.size());
        outputStream.writeInt(data.length);
        outputStream.write(byteArrayOutputStream.toByteArray());
    }

    public static byte[] decompressData(DataInputStream inputStream) throws IOException
    {
        int compressedsize  = inputStream.readInt();
        int originalsize    = inputStream.readInt();

        byte data[] = new byte[compressedsize];

        inputStream.read(data);

        InflaterInputStream inflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(data));

        byte originalData[] = new byte[originalsize];

        inflaterInputStream.read(originalData);

        return originalData;
    }
}