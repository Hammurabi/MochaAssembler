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

package com.riverssen.core.security;

import com.riverssen.core.headers.Exportable;
import com.riverssen.core.utils.Base58;
import com.riverssen.core.headers.Encodeable;
import com.riverssen.core.utils.SmartDataTransferer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CompressedAddress implements Encodeable, Exportable
{
    private String address;

    public CompressedAddress(String address)
    {
        this.address = address;
    }

    public CompressedAddress(DataInputStream stream)
    {
        try {
            int size = stream.read();
            byte array[] = new byte[size];

            stream.read(array);
            address = Base58.encode(array);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString()
    {
        return address;
    }

    public PubKey toPublicKey()
    {
        PubKey key = new PubKey(address);

        if(key.isValid()) return key;
        return null;
    }

    @Override
    public byte[] getBytes()
    {
        return Base58.decode(address);
    }

//    @Override
//    public byte[] header() {
//        return new byte[0];
//    }
//
//    @Override
//    public byte[] content() {
//        return new byte[0];
//    }

    @Override
    public void export(SmartDataTransferer smdt) {

    }

    @Override
    public void export(DataOutputStream dost) {
        try {
            byte bytes[] = Base58.decode(address);
            dost.write(bytes.length);
            dost.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}