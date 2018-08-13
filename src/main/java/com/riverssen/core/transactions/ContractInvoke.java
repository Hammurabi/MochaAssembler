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
import com.riverssen.core.headers.ContextI;
import com.riverssen.core.headers.Encodeable;
import com.riverssen.core.headers.TransactionI;
import com.riverssen.core.security.CompressedAddress;
import com.riverssen.core.security.ContractAddress;
import com.riverssen.core.security.PrivKey;
import com.riverssen.core.security.PublicAddress;
import com.riverssen.core.system.Config;
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

public class ContractInvoke implements TransactionI, Encodeable
{
    /** 64 byte compressed ecdsa public key **/
    private CompressedAddress                   sender;
    /** 20 byte contract public address that starts with 0x20 **/
    private ContractAddress                     method;
    /** A 64 byte ip address of the method (for value returns) **/
    private byte                                receiveIPAddress[];
    /** list containing the type and amount of txids to be transferred **/
    private TXIList                             txids;
    /** amount of txids to be transferred **/
    private RiverCoin                           amount;
    /** 256 byte argument data **/
    private byte                                data[];
    /** var size byte signature **/
    private byte                                signature[];
    /** 8 byte 'honest' typestamp **/
    private byte                                timestamp[];
    /** The contract creator **/
    private PublicAddress                       contractCreator;

    public ContractInvoke(DataInputStream stream) throws IOException, Exception
    {
        sender      = new CompressedAddress(stream);
        method      = new ContractAddress(ByteUtil.read(stream, 20));
        txids       = new TXIList(stream);
        amount      = RiverCoin.fromStream(stream);
        data        = ByteUtil.read(stream, 256);
        signature   = ByteUtil.read(stream, stream.read());
        timestamp   = ByteUtil.read(stream, 8);
    }

    public ContractInvoke(CompressedAddress        sender,
                          ContractAddress             receiver,
                          TXIList                   goods,
                          RiverCoin                 amount,
                          String                    comment,
                          long                      timestamp)
    {
        this.sender     = sender;
        this.method     = receiver;
        this.txids      = goods;
        this.amount     = amount;

        /** trim excess comment bytes **/
        if(comment.length() > 256) comment = comment.substring(0, 256);
        this.data       = comment.getBytes();
        this.timestamp  = ByteUtil.encode(timestamp);
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

        for(TransactionInput input : txids) if(!input.getUTXO().getOwner().equals(sender)) return false;

        /** check amount is more than or equal to the minimum transaction amount **/
        if(amount.toBigInteger().compareTo(new BigInteger(Config.getMinimumTransactionAmount())) < 0) return false;

        /** check utxo amount is more than transaction amount **/
        if (amount.toBigInteger().compareTo(getInputAmount()) > 0) return false;
        /** check utxo amount is contains a transaction fee **/
        if (amount.toBigInteger().add(getFee()).compareTo(getInputAmount()) >= 0) return false;


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
        return null;
    }

    @Override
    public TXIList getTXIDs() {
        return txids;
    }

    @Override
    public List<TransactionOutput> getOutputs(PublicAddress miner, ContextI context) {
        return null;
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
        List<TransactionOutput> utxos = new ArrayList<>();

//        utxos.add(new TransactionOutput(method, amount, encode(ByteUtil.defaultEncoder())));
        RiverCoin leftOver = new RiverCoin(getInputAmount().subtract(amount.toBigInteger()));
        /** if miner doesn't want fees, then all the leftover amount is returned to the sender as a new unspent output **/
        if(miner == null) utxos.add(new TransactionOutput(sender.toPublicKey().getAddress(), leftOver, encode(ByteUtil.defaultEncoder())));
        else{
            RiverCoin fee = new RiverCoin(getFee());
            leftOver = new RiverCoin(leftOver.toBigInteger().subtract(fee.toBigInteger()));

            if(leftOver.toBigInteger().compareTo(BigInteger.ZERO) < 0) return;//("leftover must be bigger than zero");
            if(fee.toBigInteger().compareTo(     BigInteger.ZERO) < 0) return;//("leftover must be bigger than zero");
            if(fee.toBigInteger().compareTo(     BigInteger.ZERO) > 0) utxos.add(new TransactionOutput(miner, fee, encode(ByteUtil.defaultEncoder())));
            if(leftOver.toBigInteger().compareTo(BigInteger.ZERO) > 0) utxos.add(new TransactionOutput(sender.toPublicKey().getAddress(), leftOver, encode(ByteUtil.defaultEncoder())));
        }

        for(TransactionInput input : txids)
            context.getUtxoManager().add(input.getUTXO().getOwner().toString(), input.getUTXO());
        for(TransactionOutput output : utxos)
            context.getUtxoManager().remove(output.getOwner().toString(), output);
    }

    public BigInteger getFee()
    {
        return new RiverCoin(Config.getMiningFee()).toBigInteger();
    }

    public List<TransactionOutput> generateOutputs(PublicAddress miner, UTXOMap map, ContextI context)
    {
        List<TransactionOutput> utxos = new ArrayList<>();

//        utxos.add(new TransactionOutput(method, amount, encode(ByteUtil.defaultEncoder())));
        RiverCoin leftOver = new RiverCoin(getInputAmount().subtract(amount.toBigInteger()));
        /** if miner doesn't want fees, then all the leftover amount is returned to the sender as a new unspent output **/
        if(miner == null) utxos.add(new TransactionOutput(sender.toPublicKey().getAddress(), leftOver, encode(ByteUtil.defaultEncoder())));
        else{
            RiverCoin fee = new RiverCoin(getFee());
            leftOver = new RiverCoin(leftOver.toBigInteger().subtract(fee.toBigInteger()));

            if(leftOver.toBigInteger().compareTo(BigInteger.ZERO) < 0) return null;//("leftover must be bigger than zero");
            if(fee.toBigInteger().compareTo(     BigInteger.ZERO) < 0) return null;//("leftover must be bigger than zero");
            if(fee.toBigInteger().compareTo(     BigInteger.ZERO) > 0) utxos.add(new TransactionOutput(miner, fee, encode(ByteUtil.defaultEncoder())));
            if(leftOver.toBigInteger().compareTo(BigInteger.ZERO) > 0) utxos.add(new TransactionOutput(sender.toPublicKey().getAddress(), leftOver, encode(ByteUtil.defaultEncoder())));
        }

        for(TransactionInput input : txids)
            context.getUtxoManager().remove(input.getUTXO().getOwner().toString(), input.getUTXO());
        for(TransactionOutput output : utxos)
            context.getUtxoManager().add(output.getOwner().toString(), output);

        return utxos;
    }

    @Override
    public RiverCoin cost() {
        return new RiverCoin("0");
    }

    public byte[] generateSignatureData()
    {
        return generateSignatureData(sender, method, amount, txids, data, timestamp);
    }

    public static byte[] generateSignatureData(CompressedAddress sender, ContractAddress receiver, RiverCoin amount, TXIList txilist, byte comment[], byte timestamp[])
    {
        return ByteUtil.concatenate(sender.getBytes(), receiver.getBytes(), amount.getBytes(), txilist.getBytes(), comment, timestamp);
    }

    @Override
    public byte[] getBytes() {
        return ByteUtil.concatenate(sender.getBytes(),
                method.getBytes(),
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
            dost.write(method.getBytes());
            txids.export(dost);
            amount.export(dost);
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
        return new JSON().add("sender", getSender().toString()).add("method", getReceiver().toString()).toString();
    }
}
