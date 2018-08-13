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

package com.riverssen.core;

import com.riverssen.core.headers.Encodeable;
import com.riverssen.core.headers.Exportable;
import com.riverssen.core.headers.JSONFormattable;
import com.riverssen.core.utils.SmartDataTransferer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class RiverCoin implements Encodeable, JSONFormattable, Exportable
{
    public  static final long       NANO_COIN_TO_RVC        = 100_000_000_000L;
    public static final BigInteger  MAX_RIVERCOINS          = new BigInteger("184_467_440__737_095_516_150".replace("_", ""));
    public static final int         SIZE                    = MAX_RIVERCOINS.toByteArray().length;

    private BigInteger nanocoin;

    /**
     * @param nanocoin Be careful when passing 1, 1 = 1/100.000.000.000 of a rivercoin.
     */
    @Deprecated
    public RiverCoin(BigInteger nanocoin)
    {
        this.nanocoin = nanocoin;
    }

    public RiverCoin(String rvc)
    {
        this.nanocoin = new BigInteger(new BigDecimal(rvc).multiply(new BigDecimal(NANO_COIN_TO_RVC + "")).toPlainString().replaceAll("\\..*", ""));
    }

    public RiverCoin(byte[] bytes)
    {
        this.nanocoin = new BigInteger(bytes);
    }

    public RiverCoin(BigDecimal decimal)
    {
        this(decimal.toPlainString());
    }

    @Override
    public String toString()
    {
        return nanocoin.toString();
    }

    public String toRiverCoinString()
    {
        return new BigDecimal(nanocoin).divide(new BigDecimal(NANO_COIN_TO_RVC + "")).toPlainString();
    }

    public BigInteger toBigInteger()
    {
        return nanocoin;
    }

    @Override
    public byte[] getBytes()
    {
        byte bytes[] = new byte[SIZE];

        byte nn[]    = nanocoin.toByteArray();

        int toPad    = SIZE - nn.length;

        for(int i = 0; i < toPad; i ++) bytes[i] = 0;

        for(int i = 0; i < nn.length; i ++) bytes[i + toPad] = nn[i];

        return bytes;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof RiverCoin) return nanocoin.compareTo(((RiverCoin) obj).nanocoin) == 0;
        else if(obj instanceof BigInteger) return nanocoin.compareTo(((BigInteger) obj)) == 0;

        return false;
    }

    public RiverCoin add(RiverCoin other)
    {
        return new RiverCoin(nanocoin.add(other.nanocoin));
    }

    public RiverCoin sub(RiverCoin other)
    {
        return new RiverCoin(nanocoin.subtract(other.nanocoin));
    }

    public RiverCoin mul(RiverCoin other)
    {
        return new RiverCoin(nanocoin.multiply(other.nanocoin));
    }

    public RiverCoin div(RiverCoin other)
    {
        return new RiverCoin(nanocoin.divide(other.nanocoin));
    }

    public static RiverCoin fromStream(DataInputStream stream) throws Exception
    {
        byte array[] = new byte[SIZE];

        stream.read(array);

        return new RiverCoin(array);
    }

    @Override
    public String toJSON() {
        return "{"+toRiverCoinString()+"}";
    }

//    @Override
//    public byte[] header() {
//        return HEADER_BYTES;
//    }
//
//    @Override
//    public byte[] content() {
//        return nanocoin.toByteArray();
//    }

    @Override
    public void export(SmartDataTransferer smdt) {

    }

    @Override
    public void export(DataOutputStream dost) {
        try {
            dost.write(getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}