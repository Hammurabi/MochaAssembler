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
import com.riverssen.core.headers.ContextI;
import com.riverssen.core.security.PublicAddress;
import com.riverssen.core.utils.Base58;
import com.riverssen.riverssen.Constant;

import java.io.File;
import java.math.BigInteger;

public class Balance
{
    public static final int         MAX_SIZE = PublicAddress.SIZE + RiverCoin.SIZE;
    public static final int         MAX_FILE_SIZE = 16_000;
    public static final BigInteger  MAX_BALANCES_IN_FILE = new BigInteger((MAX_FILE_SIZE / MAX_SIZE) + "");

    private PublicAddress   address;
    private RiverCoin       balance;

    public Balance(PublicAddress address, BigInteger balance)
    {
        this.address    = address;
        this.balance    = new RiverCoin(balance);
    }

    public Balance(@Constant ContextI contextI, @Constant PublicAddress address)
    {
        File file = new File(contextI.getConfig().getBlockChainTransactionDirectory() + File.separator + Base58.encode(getPosition(address).toByteArray()));

//        new Mapping(file).get(address, this);

        this.address = address;
    }

    protected void setBalance(BigInteger balance)
    {
        this.balance = new RiverCoin(balance);
    }

    public RiverCoin getBalance()
    {
        return balance;
    }

    private BigInteger getPosition(PublicAddress address)
    {
        return new BigInteger(address.getBytes()).divide(MAX_BALANCES_IN_FILE);
    }
}
