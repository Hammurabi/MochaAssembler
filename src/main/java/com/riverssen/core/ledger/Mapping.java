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

package com.riverssen.core.ledger;

import com.riverssen.core.RiverCoin;
import com.riverssen.core.security.PublicAddress;
import com.riverssen.core.utils.ByteUtil;

import java.io.*;
import java.math.BigInteger;
import java.util.zip.InflaterInputStream;

public class Mapping
{
    private Balance[] data = new Balance[Balance.MAX_BALANCES_IN_FILE.intValue()];

    public Mapping(File file) throws IOException {
        DataInputStream stream = new DataInputStream(new InflaterInputStream(new FileInputStream(file)));

        int size = stream.readInt();

        for(int i = 0; i < size; i ++)
        {
            byte address[] = ByteUtil.read(stream, PublicAddress.SIZE);
            byte balance[] = ByteUtil.read(stream, RiverCoin.SIZE);

            data[new BigInteger(address).mod(Balance.MAX_BALANCES_IN_FILE).intValue()] = new Balance(new PublicAddress(address), new BigInteger(balance));
        }
    }

    public void put(PublicAddress address, Balance balance)
    {

    }

    public void get(PublicAddress address, Balance balance) {
    }
}
