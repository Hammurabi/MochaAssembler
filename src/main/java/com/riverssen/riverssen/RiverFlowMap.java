/**
 * Copyright 2018 Ragnarr Ivarssen
 * This Software Is Free For Use And Must
 *
 * Not Be Sold And Or Distributed Without The Written Permission Of
 * (Ragnarr Ivarssen Riverssen@gmail.com).
 *
 * The Software's Code Must Not Be Made Public, The Software Must Not Be Decompiled, Reverse Engineered, Or Unobfuscated In Any Way
 * Without The Written Permission Of (Ragnarr Ivarssen Riverssen@gmail.com).
 *
 * The Creator (Ragnarr Ivarssen Riverssen@Gmail.com) Does Not Provide Any Warranties
 * To The Quality Of The Software And It Is Provided "As Is".
 */

package com.riverssen.riverssen;

import com.riverssen.core.headers.ContextI;
import com.riverssen.core.transactions.TransactionOutput;

import java.io.*;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 *
 * This is a TEMP utxo map,
 * Every block holds a new instance, which can be changed, if a block is serialized then all content is dumped to disk.
 *
 */
public class RiverFlowMap implements UTXOMap
{
    private HashMap<String, Set<TransactionOutput>> data;
    private ContextI                                context;
    private long                                    block;

    public RiverFlowMap(long block, ContextI context)
    {
        this.data = new HashMap<>();
        this.context = context;
    }

    @Override
    public void add(String publicAddress, TransactionOutput utxo) {
        get(publicAddress).add(utxo);
    }

    @Override
    public void addAll(String publicAddress, List<TransactionOutput> utxos) {
        Set<TransactionOutput> set = get(publicAddress);

        for(TransactionOutput utxo : utxos)
            set.add(utxo);
    }

    @Override
    public void addAll(List<TransactionOutput> utxos) {
        for(TransactionOutput output : utxos)
            add(output.getOwner().toString(), output);
    }

    @Override
    public void remove(String publicAddress) {
        data.remove(publicAddress);
    }

    @Override
    public void remove(String publicAddress, TransactionOutput utxo) {
        get(publicAddress).remove(utxo);
    }

    @Override
    public UTXOMap getBranch() {
        return this;
    }

    private Element createMerkleTree(String address)
    {
        PriorityQueue<Element> hashes = new PriorityQueue<>();

        for(TransactionOutput array : data.get(address)) hashes.add(new Element(array));

        while(hashes.size() > 1)
            hashes.add(new Element(hashes.poll(), hashes.poll()));

        return hashes.poll();
    }

    @Override
    public byte[] getStamp() {
        PriorityQueue<Element> hashes = new PriorityQueue<>();

        for(String address : data.keySet())
            hashes.add(createMerkleTree(address));

        while(hashes.size() > 1)
            hashes.add(new Element(hashes.poll(), hashes.poll()));

        return new byte[0];
    }

    @Override
    public void addAll(UTXOMap map) {
//        if(map instanceof RiverFlowMap)
//            data.putAll(((RiverFlowMap) map).data);
//        else {
//            ImmutableRiverFlowMap map_ = (ImmutableRiverFlowMap) map;
//
//            for(String remove : map_.remove)
//                remove(remove);
//
//            for(Tuple<String, TransactionOutput> remove : map_.remove_values)
//                remove(remove.getI(), remove.getJ());
//
//            for(Tuple<String, TransactionOutput> add : map_.add)
//                add(add.getI(), add.getJ());
//        }
    }

    @Override
    public void serialize(ContextI context) throws IOException {
        File file = new File(context.getConfig().getBlockChainTransactionDirectory() + File.separator + "ledger" + File.separator);

        file.mkdir();

        for(String address : data.keySet())
        {
            Set<TransactionOutput> set = get(address);
            File balance_ = new File(file.toString() + File.separator + address);
                DataOutputStream stream = new DataOutputStream(new FileOutputStream(balance_));
                stream.writeInt(set.size());
                for(TransactionOutput output : set)
                    output.export(stream);
                stream.flush();
                stream.close();
        }
    }

    @Override
    public byte[] hash() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream      dataOutputStream = new DataOutputStream(stream);

        for(String address : data.keySet())
        {
            Set<TransactionOutput> set = get(address);
            dataOutputStream.writeInt(set.size());
            for(TransactionOutput output : set)
                output.export(dataOutputStream);
            stream.flush();
            stream.close();
        }
        return new byte[0];
    }

    private boolean deserialize(String name)
    {
        File address = new File(context.getConfig().getBlockChainTransactionDirectory() + File.separator + "ledger" + File.separator + name);

        if(!address.exists()) return false;

        try{
            Set<TransactionOutput> set = new LinkedHashSet<>();
            DataInputStream stream = new DataInputStream(new FileInputStream(address));
            int size = stream.readInt();

            for(int i = 0; i < size; i ++)
                set.add(new TransactionOutput(stream));

            stream.close();

            data.put(name, set);

            return true;
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Set<TransactionOutput> get(String address)
    {
        if(data.containsKey(address)) return data.get(address);

        if(deserialize(address)) return data.get(address);

        data.put(address, new LinkedHashSet<>());

        return data.get(address);
    }

    @Override
    public void removeAll(List<TransactionOutput> utxos)
    {
        for(TransactionOutput transactionOutput : utxos)
            remove(transactionOutput.getOwner().toString(), transactionOutput);
    }

    @Override
    public long block() {
        return block;
    }
}