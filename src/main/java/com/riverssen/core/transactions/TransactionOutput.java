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

package com.riverssen.core.transactions;

import com.riverssen.core.RiverCoin;
import com.riverssen.core.headers.Encodeable;
import com.riverssen.core.headers.Exportable;
import com.riverssen.core.headers.JSONFormattable;
import com.riverssen.core.security.PublicAddress;
import com.riverssen.core.utils.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Set;

/** Instances of this class will be created during runtime using the Transaction::generateOutputs(Transaction*); **/
public class TransactionOutput/**<T extends Encodeable & JSONFormattable & Exportable>**/ implements Encodeable, JSONFormattable, Exportable
{
    public static final int SIZE = PublicAddress.SIZE + RiverCoin.SIZE + 64;
    private final PublicAddress owner;
    private final RiverCoin     value;
    private final byte          ptxid[];
    private final byte          txoid[];

    public TransactionOutput(PublicAddress receiver, RiverCoin value, byte parentTXID[])
    {
        this.owner = receiver;
        this.value = value;
        this.ptxid = parentTXID;

        /** generate a custom hash id for this particular transactionoutput **/
        this.txoid = ByteUtil.defaultEncoder().encode(ByteUtil.concatenate(receiver.getBytes(), value.getBytes(), parentTXID));
    }

    public TransactionOutput(DataInputStream stream) throws Exception {
        this(new PublicAddress(ByteUtil.read(stream, PublicAddress.SIZE)), RiverCoin.fromStream(stream), ByteUtil.read(stream, 32));
    }

    /** The parent transaction ID **/
    public byte[] getParentTXID()
    {
        return ptxid;
    }
    public byte[] getHash()
    {
        return txoid;
    }
    /** The value of the UTXO, using UTXO<Rivercoin> will return rvc balances **/
    public RiverCoin getValue()
    {
        return value;
    }
    /** The recepient of the unspent transaction output **/
    public PublicAddress getOwner()
    {
        return owner;
    }
    /** Any class must have a toString method returning a simple readable format of this header **/
    public String toString()
    {
        return toJSON();
    }

    @Override
    public byte[] getBytes()
    {
        return ByteUtil.concatenate(getOwner().getBytes(), getValue().getBytes(), getParentTXID());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TransactionOutput)
            return ((TransactionOutput) obj).owner.equals(owner) && ByteUtil.equals(((TransactionOutput) obj).ptxid, ptxid) && ByteUtil.equals(((TransactionOutput) obj).txoid, txoid) && ((TransactionOutput) obj).value.equals(value);

        return false;
    }

    @Override
    public int hashCode() {
        return HashUtil.applySha512(ByteUtil.concatenate(owner.getBytes(), ptxid, txoid, value.getBytes())).hashCode();
    }

    @Override
    public String toJSON()
    {
        return new JSONFormattable.JSON().add("txoid", Base58.encode(txoid)).add("owner", getOwner().toString()).add("value", getValue().toRiverCoinString()).add("txid", Base58.encode(getParentTXID())).toString();
    }

//    @Override
//    public byte[] header()
//    {
//        return ByteUtil.concatenate(getValue().header());
//    }
//
//    @Override
//    public byte[] content()
//    {
//        return ByteUtil.concatenate(getOwner().getBytes(), getValue().getBytes(), getParentTXID());
//    }

    @Override
    public void export(SmartDataTransferer smdt)
    {
    }

    @Override
    public void export(DataOutputStream dost) {
        try {
            owner.export(dost);
            value.export(dost);
            dost.write(ptxid);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
