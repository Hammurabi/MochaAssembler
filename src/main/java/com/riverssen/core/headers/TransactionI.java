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

package com.riverssen.core.headers;

import com.riverssen.core.RiverCoin;
import com.riverssen.core.security.CompressedAddress;
import com.riverssen.core.security.PrivKey;
import com.riverssen.core.security.PublicAddress;
import com.riverssen.core.transactions.*;
import com.riverssen.riverssen.UTXOMap;

import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public interface TransactionI extends Comparable<TransactionI>, Encodeable, JSONFormattable, Exportable
{
    boolean                         valid(UTXOMap map, ContextI context);
    long                            getTimeStamp();
    CompressedAddress               getSender();
    PublicAddress                   getReceiver();
    int                             hashCode();
    TransactionI                    sign(PrivKey key);
    default int                     compareTo(TransactionI token)
    {
        return getTimeStamp() > token.getTimeStamp() ? 1 : -1;
    }
    TXIList                         getTXIDs();
    List<TransactionOutput>         getOutputs(PublicAddress miner, ContextI context);
    List<TransactionOutput>         generateOutputs(PublicAddress miner, UTXOMap map, ContextI context);
    RiverCoin                       cost();

    BigInteger                      getInputAmount();

    public static final int         TRANSACTION = 0, REWARD = 1, CONTRACT = 2, CONTRACT_INVOKE = 3;

    static TransactionI             read(DataInputStream stream)
    {
        try {
            int type = stream.read();

            if(type         == TRANSACTION)     return new Transaction(stream);
            else if(type    == REWARD)          return new RewardTransaction(stream);
            else if(type    == CONTRACT)        return new ContractDeploy(stream);
            else if(type    == CONTRACT_INVOKE) return new ContractInvoke(stream);
            else throw new Exception("corrupted transaction data");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    void revertOutputs(PublicAddress miner, UTXOMap map, ContextI context);
}