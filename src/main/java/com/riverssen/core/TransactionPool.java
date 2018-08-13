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

import com.riverssen.core.headers.TransactionI;
import com.riverssen.core.headers.ContextI;
import com.riverssen.core.transactions.Transaction;

import java.util.*;

public class TransactionPool
{
    private volatile Set<TransactionI>   pool;
    private volatile ContextI            context;
    private volatile long                lastTransactionTime;

    public TransactionPool(ContextI network)
    {
        pool = Collections.synchronizedSet(new LinkedHashSet<>());
        context = network;
        lastTransactionTime = System.currentTimeMillis();
    }

    public void addInternal(TransactionI token)
    {
        /** check tokens timestamp isn't older than a block **/
        if(System.currentTimeMillis() - token.getTimeStamp() > (context.getConfig().getAverageBlockTime() * 1.5)) return;

        lastTransactionTime = System.currentTimeMillis();

        pool.add(token);

        context.getNetworkManager().broadCastNewTransaction(token);
    }

    public void addRelayed(TransactionI transaction)
    {
        /** check tokens timestamp isn't older than a block **/
        if(System.currentTimeMillis() - transaction.getTimeStamp() > (context.getConfig().getAverageBlockTime() * 1.5)) return;

        lastTransactionTime = System.currentTimeMillis();

        pool.add(transaction);
    }

    public void putBack(List<TransactionI> transactions)
    {
        for (TransactionI transaction : transactions)
            addRelayed(transaction);
    }

    public boolean available()
    {
        return pool.size() > 0;
    }

    public TransactionI next()
    {
        TransactionI txi = pool.iterator().next();
        pool.remove(txi);
        return txi;
    }

    public void resetTimer()
    {
        lastTransactionTime = System.currentTimeMillis();
    }

    public boolean getLastTransactionWas(long time)
    {
        boolean bool = System.currentTimeMillis() - lastTransactionTime >= time;
        return bool;
    }
}