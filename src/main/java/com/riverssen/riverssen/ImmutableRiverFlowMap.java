///*
// * The MIT License (MIT)
// *
// * Copyright (c) 2018 Riverssen
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// */
//
//package com.riverssen.riverssen;
//
//import com.riverssen.core.headers.ContextI;
//import com.riverssen.core.transactions.TransactionOutput;
//import com.riverssen.core.utils.Tuple;
//
//import java.util.*;
//
//public class ImmutableRiverFlowMap implements UTXOMap
//{
//    private     RiverFlowMap                            data;
//    protected   Set<Tuple<String, TransactionOutput>>   add;
//    protected   Set<String>                             remove;
//    protected   Set<Tuple<String, TransactionOutput>>   remove_values;
//
//    public ImmutableRiverFlowMap(RiverFlowMap data)
//    {
//        this.data   = data;
//        this.add    = new LinkedHashSet<>();
//        this.remove = new LinkedHashSet<>();
//        this.remove_values = new LinkedHashSet<>();
//    }
//
//    @Override
//    public void add(String publicAddress, TransactionOutput utxo) {
//        add.add(new Tuple<>(publicAddress, utxo));
//    }
//
//    @Override
//    public void addAll(String publicAddress, List<TransactionOutput> utxos) {
//        for(TransactionOutput utxo : utxos)
//            add(publicAddress, utxo);
//    }
//
//    @Override
//    public void addAll(List<TransactionOutput> utxos) {
//        for(TransactionOutput output : utxos)
//            add(output.getOwner().toString(), output);
//    }
//
//    @Override
//    public void remove(String publicAddress) {
//        remove.add(publicAddress);
//    }
//
//    @Override
//    public void remove(String publicAddress, TransactionOutput utxo) {
//        remove_values.add(new Tuple<>(publicAddress, utxo));
//    }
//
//    @Override
//    public UTXOMap getBranch() {
//        return new ImmutableRiverFlowMap(data);
//    }
//
//    @Override
//    public byte[] getStamp() {
//        return data.getStamp();
//    }
//
//    @Override
//    public void addAll(UTXOMap map) {
//    }
//
//    @Override
//    public void serialize(ContextI context) {
//        data.serialize(context);
//    }
//
//    @Override
//    @Constant
//    public Set<TransactionOutput> get(String address)
//    {
//        return new LinkedHashSet<>(data.get(address));
//    }
//}