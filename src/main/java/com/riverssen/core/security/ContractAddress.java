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

import com.riverssen.core.algorithms.RipeMD160;
import com.riverssen.core.algorithms.RipeMD256;
import com.riverssen.core.algorithms.Sha256;
import com.riverssen.core.headers.Encodeable;
import com.riverssen.core.headers.Exportable;
import com.riverssen.core.system.Config;
import com.riverssen.core.transactions.TransactionOutput;
import com.riverssen.core.utils.Base58;
import com.riverssen.core.utils.ByteUtil;
import com.riverssen.core.utils.HashUtil;
import com.riverssen.core.utils.SmartDataTransferer;
import com.riverssen.riverssen.UTXOMap;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Set;

public class ContractAddress implements Encodeable, Exportable, Serializable
{
    public static final int SIZE = 25;
    private String address;

    public ContractAddress(String address)
    {
        this.address = address;
    }

    public ContractAddress(byte address[])
    {
        this.address = Base58.encode(address);
    }

    public static String  generateAddress(CompressedAddress sender, byte contract[])
    {
        byte hash[] = ByteUtil.concatenate(new byte[] {Config.MAIN_NETWORK}, new RipeMD160().encode(new Sha256().encode(new Sha256().encode(ByteUtil.concatenate(sender.getBytes(), contract)))));

        byte checksum[] = ByteUtil.trim(HashUtil.applySha256(HashUtil.applySha256(hash)), 0, 4);

        byte addr[] = ByteUtil.concatenate(hash, checksum);

        return Base58.encode(addr);
    }

    public static boolean isPublicAddressValid(String address) {
        byte bytes[]    = Base58.decode(address);

        int first       = bytes[0];

        if(first        != 0) return false;

        byte key_21[]   = ByteUtil.trim(bytes, 0, 21);

        byte a[]        = ByteUtil.trim(bytes, 21, 25);
        byte b[]        = ByteUtil.trim(HashUtil.applySha256(HashUtil.applySha256(key_21)), 0, 4);

        return ByteUtil.equals(a, b);
    }

    @Override
    public String toString()
    {
        return address;
    }

    public byte[] getBytes()
    {
        return Base58.decode(address);
    }

    public static byte[] decode(String public_address)
    {
        return Base58.decode(public_address);
    }

    public BigInteger getBalance(UTXOMap map)
    {
        Set<TransactionOutput> set = map.get(this.address);

        BigInteger balance = BigInteger.ZERO;

        for (TransactionOutput output : set)
            balance = balance.add(output.getValue().toBigInteger());

        return balance;
    }

    public boolean equals(ContractAddress address)
    {
        return address.address.equals(this.address);
    }

    @Override
    public void export(SmartDataTransferer smdt)
    {
    }

    @Override
    public void export(DataOutputStream dost) throws IOException
    {
        dost.write(Base58.decode(address));
    }
}