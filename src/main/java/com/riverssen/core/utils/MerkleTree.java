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

package com.riverssen.core.utils;

import com.riverssen.core.algorithms.Sha3;
import com.riverssen.core.headers.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class MerkleTree implements Serialisable, Encodeable
{
    /** the root element of the merkle tree **/
    private TreeElement root;
    /** a flat representation of the merkle tree in a list of tokens **/
    private List<TransactionI> list;
    /** a flat representation of the merkle tree in a list of hash strings **/
    private List<String> hashList;
    /** the size of the tree **/
    private int size;

    public MerkleTree(List<TransactionI> tokenList)
    {
        load(tokenList);
    }

    @Override
    public void serialize(DataOutputStream stream) throws Exception
    {
        stream.writeInt(list.size());

        for (TransactionI token : list)
            token.export(stream);
    }

    @Override
    public void deserialize(DataInputStream stream, String version) throws Exception
    {
        int i = stream.readInt();

        ArrayList<TransactionI> list = new ArrayList<>();

        for (int j = 0; j < i; j++)
            list.add(TransactionI.read(stream));

        load(list);
    }

    /** return a flat representation of the tree elements in a list of tokens **/
    public List<TransactionI> flatten()
    {
        return list;
    }

    /** return a flat representation of the tree elements in a list of hashes **/
    public List<String> flattenHashes()
    {
        return this.hashList;
    }

    public String toJSON()
    {
        String json = "{";

        for(String hash : hashList) json += hash + ",";

        json = json.substring(0, json.length() - 2) + "}";

        return json;
    }

    private void load(List<TransactionI> list)
    {
        this.list = Collections.synchronizedList(new ArrayList<>());
        this.list.addAll(list);

        PriorityQueue<TreeElement> elements = new PriorityQueue<>();

        for (TransactionI token : list) elements.add(new TreeElement(token));

        /** if list size is an odd number, we add the last element twice to create an even number list **/
        if(list.size() % 2 == 1) elements.add(new TreeElement(list.get(list.size() - 1)));

        int i = 0;

        while (elements.size() > 1)
            elements.add(new TreeElement(elements.poll(), elements.poll(), i++));

        this.root = elements.poll();
        this.size = list.size();
    }

    public MerkleTree()
    {
        list = new ArrayList<>();
        hashList = new ArrayList<>();
    }

//    public void loadFromHeader(List<String> list)
//    {
//        this.list = Collections.synchronizedList(new ArrayList<>());
//        this.hashList = Collections.synchronizedList(new ArrayList<>());
//        this.hashList.addAll(list);
//
//        PriorityQueue<TreeElement> elements = new PriorityQueue<>();
//
//        for (String token : list) elements.add(new TreeElement(token));
//
//        int i = 0;
//
//        while (elements.size() > 1)
//            elements.add(new TreeElement(elements.poll(), elements.poll(), i++));
//
//        this.root = elements.poll();
//        this.size = list.size();
//    }

    public void buildTree()
    {
        PriorityQueue<TreeElement> elements = new PriorityQueue<>();

        for (TransactionI token : list) elements.add(new TreeElement(token));

        /** if list size is an odd number, we add the last element twice to create an even number list **/
        if(list.size() % 2 == 1) elements.add(new TreeElement(list.get(list.size() - 1)));

        int i = 0;

        while (elements.size() > 1)
            elements.add(new TreeElement(elements.poll(), elements.poll(), i++));

        this.root = elements.poll();
        this.size = list.size();
    }

    public String hash()
    {
        return root.hash();
    }

    @Override
    public byte[] encode(HashAlgorithm algorithm)
    {
        return root.encode(algorithm);
    }

    @Override
    public byte[] getBytes()
    {
        return new byte[0];
    }

    public void add(TransactionI token)
    {
        list.add(token);

        load(new ArrayList<>(list));
    }

    private class TreeElement implements Comparable<TreeElement>, Serialisable, Encodeable
    {
        private TransactionI token;
        private String hash;
        private TreeElement left;
        private TreeElement right;
        private int priority;

        TreeElement(TransactionI token)
        {
            this.token = token;
            this.hash  = token.encode16(new Sha3());
        }

        public TreeElement(TreeElement left, TreeElement right, int i)
        {
            this.left = left;
            this.right = right;
            this.priority = i;
        }

        public TreeElement(String token)
        {
            this.hash = token;
        }

        public boolean isLeaf()
        {
            return this.left == null && this.right == null;
        }

        @Override
        public int compareTo(TreeElement o)
        {
            return o.isLeaf() ? 1 : 0;
        }

        public String hash()
        {
            if (isLeaf())
                return hash;

            return HashUtil.hashToStringBase16(HashUtil.applySha256((left.hash() + right.hash()).getBytes()));
        }

        public void add(List<TreeElement> list)
        {
            if (isLeaf())
                list.add(this);

            else
            {
                left.add(list);
                right.add(list);
            }
        }

        /** this shouldn't be used, instead a flat represenation of the tree should suffice **/
        @Override
        public void serialize(DataOutputStream stream) throws Exception
        {
        }

        @Override
        public void deserialize(DataInputStream stream, String version) throws Exception
        {
        }

        @Override
        public byte[] encode(HashAlgorithm algorithm)
        {
            if(isLeaf()) return token.encode(algorithm);
            return algorithm.encode(ByteUtil.concatenate(left.encode(algorithm), right.encode(algorithm)));
        }

        @Override
        public byte[] getBytes()
        {
            if(isLeaf())
                return token.getBytes();

            return ByteUtil.concatenate(left.getBytes(), right.getBytes());
        }
    }

    @Override
    public String toString()
    {
        JSONFormattable.JSON json = new JSONFormattable.JSON("transactions", true);

        for(TransactionI transaction : list)
            json.add(transaction.toJSON());

        return json.toString();
    }
}