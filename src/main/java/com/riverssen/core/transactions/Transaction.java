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
import com.riverssen.core.headers.TransactionI;
import com.riverssen.core.security.CompressedAddress;
import com.riverssen.core.security.PrivKey;
import com.riverssen.core.security.PublicAddress;
import com.riverssen.core.system.Config;
import com.riverssen.core.headers.ContextI;
import com.riverssen.core.utils.Base58;
import com.riverssen.core.utils.ByteUtil;
import com.riverssen.core.utils.SmartDataTransferer;
import com.riverssen.riverssen.UTXOMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Transaction implements TransactionI, Encodeable
{
    /** 64 byte compressed ecdsa public key **/
    private CompressedAddress                   sender;
    /** 25 byte receiver public address **/
    private PublicAddress                       receiver;
    /** list containing the type and amount of txids to be transferred **/
    private TXIList                             txids;
    /** amount of txids to be transferred **/
    private RiverCoin                           amount;
    /** 256 byte comment in UTF format **/
    private byte                                data[];
    /** var size byte signature **/
    private byte                                signature[];
    /** 8 byte 'honest' typestamp **/
    private byte                                timestamp[];

    public Transaction(DataInputStream stream) throws IOException, Exception
    {
        sender      = new CompressedAddress(stream);
        receiver    = new PublicAddress(ByteUtil.read(stream, PublicAddress.SIZE));
        txids       = new TXIList(stream);
        amount      = RiverCoin.fromStream(stream);
        data        = ByteUtil.read(stream, stream.read());
        signature   = ByteUtil.read(stream, stream.read());
        timestamp   = ByteUtil.read(stream, 8);
    }

    public Transaction(CompressedAddress        sender,
                      PublicAddress             receiver,
                      TXIList                   goods,
                      RiverCoin                 amount,
                      String                    comment)
    {
        this.sender     = sender;
        this.receiver   = receiver;
        this.txids      = goods;
        this.amount     = amount;

        /** trim excess comment bytes **/
        if(comment.length() > 256) comment = comment.substring(0, 256);
        this.data       = comment.getBytes();
        this.timestamp  = ByteUtil.encode(System.currentTimeMillis());
    }

    /** sign transaction **/
    public TransactionI sign(PrivKey key)
    {
        byte[] bytes = generateSignatureData();

        this.signature = key.signEncoded(bytes);

        return this;
    }

    public boolean valid(UTXOMap map, ContextI context)
    {
        if (sender.toPublicKey() == null) return false;

        if (!sender.toPublicKey().isValid()) return false;

        Set<TransactionOutput> utxos = map.get(sender.toPublicKey().getAddress().toString());

        for(TransactionInput input : txids) if(!utxos.contains(input.getUTXO().getHash())) return false;

        /** check amount is more than or equal to the minimum transaction amount **/
        if(amount.toBigInteger().compareTo(new BigInteger(Config.getMinimumTransactionAmount())) < 0) return false;

        /** check utxo amount is more than transaction amount **/
        if (amount.toBigInteger().compareTo(getInputAmount()) > 0) return false;

        /** check utxo amount is contains a transaction fee **/
        if (cost().toBigInteger().compareTo(getInputAmount()) >= 0) return false;

        return sender.toPublicKey().verifySignature(generateSignatureData(), Base58.encode(signature));
    }

    @Override
    public long getTimeStamp() {
        return ByteUtil.decode(timestamp);
    }

    @Override
    public CompressedAddress getSender() {
        return sender;
    }

    @Override
    public PublicAddress getReceiver() {
        return receiver;
    }

    @Override
    public TXIList getTXIDs() {
        return txids;
    }

    @Override
    public List<TransactionOutput> getOutputs(PublicAddress miner, ContextI context) {
        List<TransactionOutput> utxos = new ArrayList<>();

        /** Add new UTXO (base transaction amount) **/
        utxos.add(new TransactionOutput(receiver, amount, encode(ByteUtil.defaultEncoder())));

        /** subtract the base transaction amount to find the leftover amount **/
        RiverCoin leftOver = new RiverCoin(getInputAmount().subtract(amount.toBigInteger()));

        /** if miner doesn't want fees, then all the leftover amount is returned to the sender as a new unspent output **/
        if(miner == null) utxos.add(new TransactionOutput(sender.toPublicKey().getAddress(), leftOver, encode(ByteUtil.defaultEncoder())));
        else {
            /** measure the cost as a fee **/
            RiverCoin fee = cost();

            /** calculate new leftover **/
            leftOver = new RiverCoin(leftOver.toBigInteger().subtract(fee.toBigInteger()));

            /** check that there is a left over **/
            if (leftOver.toBigInteger().compareTo(BigInteger.ZERO) < 0)
                return null;

            /** check that the fee is more than zero **/
            if (fee.toBigInteger().compareTo(BigInteger.ZERO) <= 0) return null;

            utxos.add(new TransactionOutput(miner, fee, encode(ByteUtil.defaultEncoder())));

            if (leftOver.toBigInteger().compareTo(BigInteger.ZERO) > 0)
                utxos.add(new TransactionOutput(sender.toPublicKey().getAddress(), leftOver, encode(ByteUtil.defaultEncoder())));
        }

        return utxos;
    }

    /** read all transaction inputs and return a rivercoin value **/
    public BigInteger getInputAmount()
    {
        BigInteger amount = BigInteger.ZERO;

        for(TransactionInput txi : txids)
            amount = amount.add(((txi.getUTXO()).getValue().toBigInteger()));

        return amount;
    }

    @Override
    public void revertOutputs(PublicAddress miner, UTXOMap map, ContextI context)
    {
        List<TransactionOutput> utxos = getOutputs(miner, context);

        for(TransactionInput input : txids)
            map.add(input.getUTXO().getOwner().toString(), input.getUTXO());
        for(TransactionOutput output : utxos)
            map.remove(output.getOwner().toString(), output);

        map.addAll(utxos);
    }

    public BigInteger getFee()
    {
        return new RiverCoin(Config.getMiningFee()).toBigInteger();
    }

    public List<TransactionOutput> generateOutputs(PublicAddress miner, UTXOMap map, ContextI context)
    {
        List<TransactionOutput> utxos = getOutputs(miner, context);

        for(TransactionInput input : txids)
            map.remove(input.getUTXO().getOwner().toString(), input.getUTXO());
        for(TransactionOutput output : utxos)
            map.add(output.getOwner().toString(), output);

        return utxos;
    }

    @Override
    public RiverCoin cost()
    {
        int sizeInBytes = data.length + txids.length();
        return new RiverCoin(Config.getCost(sizeInBytes)).add(new RiverCoin(Config.getMiningFee()));
    }

    public byte[] generateSignatureData()
    {
        return generateSignatureData(sender, receiver, amount, txids, data, timestamp);
    }

    public static byte[] generateSignatureData(CompressedAddress sender, PublicAddress receiver, RiverCoin amount, TXIList txilist, byte comment[], byte timestamp[])
    {
        return ByteUtil.concatenate(sender.getBytes(), receiver.getBytes(), amount.getBytes(), txilist.getBytes(), comment, timestamp);
    }

    @Override
    public byte[] getBytes() {
        return ByteUtil.concatenate(sender.getBytes(),
                receiver.getBytes(),
                txids.getBytes(),
                amount.getBytes(),
                data,
                timestamp);
    }

    @Override
    public void export(SmartDataTransferer smdt) {
    }

    @Override
    public void export(DataOutputStream dost)
    {
        try {
            dost.write(TRANSACTION);

            sender.export(dost);
            receiver.export(dost);
            txids.export(dost);
            amount.export(dost);
            dost.write(data.length);
            dost.write(data);
            dost.write(signature.length);
            dost.write(signature);
            dost.write(timestamp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toJSON() {
        JSON json = new JSON().add("type", "transaction").add("sender", getSender().toPublicKey().getAddress().toString()).add("receiver", getReceiver().toString()).add("input", new RiverCoin(getInputAmount().toString()).toRiverCoinString()).add("amount", amount.toRiverCoinString()).add("timestamp", getTimeStamp() + "");

        String inputs = new String("\"inputs\":[");

        for(TransactionInput input : txids)
            inputs += ("\"" + Base58.encode(input.getTransactionOutputID()) + "\"") + ", ";

        if(inputs.length() > 0)
            inputs = inputs.substring(0, inputs.length() - 2) + "]";
        else inputs = "[]";

        json.add(inputs);

        return json.toString();
    }
}
