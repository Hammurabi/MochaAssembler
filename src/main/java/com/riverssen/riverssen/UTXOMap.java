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

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface UTXOMap {
    public void add(String publicAddress, TransactionOutput utxo);
    public void addAll(String publicAddress, List<TransactionOutput> utxos);
    public void addAll(List<TransactionOutput> utxos);

    public void remove(String publicAddress);
    public void remove(String publicAddress, TransactionOutput utxo);

    public UTXOMap getBranch();

    public byte[] getStamp();

    public void addAll(UTXOMap map);

    public void serialize(ContextI context) throws IOException;
    public byte[] hash() throws IOException;

    Set<TransactionOutput> get(String address);

    void removeAll(List<TransactionOutput> utxos);

    long block();
}