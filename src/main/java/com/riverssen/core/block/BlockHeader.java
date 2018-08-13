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

package com.riverssen.core.block;

import com.riverssen.core.RiverCoin;
import com.riverssen.core.headers.ContextI;
import com.riverssen.core.headers.Exportable;
import com.riverssen.core.headers.JSONFormattable;
import com.riverssen.core.security.PublicAddress;
import com.riverssen.core.utils.*;
import com.riverssen.core.headers.Encodeable;

import java.io.*;
import java.math.BigInteger;
import java.util.zip.InflaterInputStream;

public class BlockHeader implements Encodeable, Exportable
{
    public static long SIZE = 8 + 32 + 32 + 32 + 32 + 8 + 32 + PublicAddress.SIZE + 8 + 8 + 13;
    /** 2 byte version information **/
    private volatile long version;
    /** 32 byte hash of **/
    private volatile byte hash[] = new byte[32];
    /** 32 byte hash of the block **/
    private volatile byte parentHash[] = new byte[32];
    /** 32 byte hash of the merkle root **/
    private volatile byte merkleRoot[] = new byte[32];
    /** 32 byte hash of the UTXOChainmerkle root **/
    private volatile byte riverMerkleRoot[] = new byte[32];
    /** 8 byte integer of the blocks timestamp **/
    private volatile long timeStamp;
    /** 32 byte integer of the difficulty **/
    private volatile byte difficulty[] = new byte[32];
    /** 20 byte hash of the miners public address **/
    private volatile byte minerAddress[] = new byte[PublicAddress.SIZE];
    /** 8 byte integer solution **/
    private volatile long nonce;
    /** 8 byte integer referencing the block id **/
    private volatile long blockID;
    private volatile byte reward[] = new byte[RiverCoin.SIZE];

    public BlockHeader(String hash, String parentHash, String merkleRoot, MerkleTree tree, long timeStamp, BigInteger difficulty, PublicAddress minerAddress, long nonce)
    {
        this(null, null, null, null, null, null, null, null);
    }

    public BlockHeader(byte[] hash, byte[] parentHash, byte[] merkleRoot, byte[] merkleTree, byte[] timeStamp, byte[] difficulty, byte[] minerAddress, byte[] nonce)
    {
    }

    public BlockHeader()
    {
    }

    private void read(DataInputStream stream) throws IOException {
        version = stream.readLong();
        stream.read(hash);
        stream.read(parentHash);
        stream.read(merkleRoot);
        stream.read(riverMerkleRoot);
        timeStamp = stream.readLong();
        stream.read(difficulty);
        stream.read(minerAddress);
        nonce = stream.readLong();
        blockID = stream.readLong();
        stream.read(reward);
    }

    @Override
    public void export(DataOutputStream dost) throws IOException
    {
        dost.writeLong(version);
        dost.write(hash);
        dost.write(parentHash);
        dost.write(merkleRoot);
        dost.write(riverMerkleRoot);
        dost.writeLong(timeStamp);
        dost.write(difficulty);
        dost.write(minerAddress);
        dost.writeLong(nonce);
        dost.writeLong(blockID);
        dost.write(reward);
    }

    public BlockHeader(long block, ContextI context)
    {
        if(block < 0) return;

        File file = new File(context.getConfig().getBlockChainDirectory() + File.separator + "block["+block+"]");

        try
        {
            DataInputStream stream = new DataInputStream(new InflaterInputStream(new FileInputStream(file)));

            read(stream);

            stream.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public BlockHeader(DataInputStream stream)
    {
        try
        {
            read(stream);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public byte[] getHash()
    {
        return hash;
    }

    public byte[] getParentHash()
    {
        return parentHash;
    }

    public byte[] getMerkleRoot()
    {
        return merkleRoot;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public byte[] getDifficulty()
    {
        return difficulty;
    }

    public byte[] getMinerAddress()
    {
        return minerAddress;
    }

    public long getNonce()
    {
        return nonce;
    }

    public String getHashAsString()
    {
        return ByteUtil.decodeHash(this.hash);
    }

    public long getBlockID()
    {
        return blockID;
    }

    public String getParentHashAsString()
    {
        return ByteUtil.decodeHash(getParentHash());
    }

    public String getMerkleRootAsString()
    {
        return ByteUtil.decodeHash(getMerkleRoot());
    }

    public long getTimeStampAsLong()
    {
        return getTimeStamp();
    }

    public BigInteger getDifficultyAsInt()
    {
        return new BigInteger(getDifficulty());
    }

    public long getNonceAsInt()
    {
        return getNonce();
    }

    public PublicAddress getMinerAddressAsPublicAddress()
    {
        return new PublicAddress(getMinerAddress());
    }

    public void setHash(byte[] hash)
    {
        for(int i = 0; i < 32; i ++) this.hash[i] = hash[i];
    }

    public void setParentHash(byte[] hash)
    {
        for(int i = 0; i < 32; i ++) this.parentHash[i] = hash[i];
    }

    public void setMerkleRoot(byte root[])
    {
        for(int i = 0; i < 32; i ++) this.merkleRoot[i] = root[i];
    }

    public void setTimeStamp(long serializable)
    {
        this.timeStamp = serializable;
    }

    public void setMiner(PublicAddress address)
    {
        this.minerAddress = address.getBytes();
    }

    @Override
    public byte[] getBytes()
    {
        return null;
    }

    private byte[] difficultyBytes(BigInteger integer)
    {
        byte bytes[] = new byte[32];

        byte nn[]    = integer.toByteArray();

        int toPad    = 32 - nn.length;

        for(int i = 0; i < toPad; i ++) bytes[i] = 0;

        for(int i = 0; i < nn.length; i ++) bytes[i + toPad] = nn[i];

        return bytes;
    }

    public void setDifficulty(BigInteger difficulty)
    {
//        System.arraycopy(difficulty.toByteArray(), 0, this.difficulty, 32 - difficulty.toByteArray().length, difficulty.toByteArray().length);

        System.arraycopy(difficultyBytes(difficulty), 0, this.difficulty, 0, 32);
    }

    public void setNonce(long nonce)
    {
//        System.arraycopy(this.nonce, 0, ByteUtil.encode(nonce), 0, 8);
        this.nonce = nonce;
    }

    public void setMinerAddress(byte[] minerAddress)
    {
        System.arraycopy(minerAddress, 0, this.minerAddress, 0, this.minerAddress.length);
    }

    public void set(BlockHeader header)
    {
    }

    public void setBlockID(long blockID)
    {
        this.blockID = blockID;
    }

    public FullBlock continueChain(ContextI context)
    {
        return new FullBlock(getBlockID(), this, context);
    }

    public static FullBlock FullBlock(long block, ContextI context)
    {
        File file = new File(context.getConfig().getBlockChainDirectory() + File.separator + "block["+block+"]");
        DataInputStream stream = null;
        try
        {
            stream = new DataInputStream(new InflaterInputStream(new FileInputStream(file)));
            BlockHeader header = new BlockHeader(stream);
            BlockHeader parent = new BlockHeader(header.getBlockID(), context);
            FullBlock fBlock = new FullBlock(header, new BlockData(stream, context), parent, context);

            stream.close();

            return fBlock;
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String toString()
    {
        return new JSONFormattable.JSON("header")
                .add("version", version + "")
                .add("hash", HashUtil.hashToStringBase16(hash))
                .add("parent", HashUtil.hashToStringBase16(parentHash))
                .add("merkleRoot", HashUtil.hashToStringBase16(merkleRoot))
                .add("riverRoot", Base58.encode(riverMerkleRoot))
                .add("timestamp", timeStamp + "")
                .add("difficulty", new BigInteger(difficulty).toString())
                .add("miner", Base58.encode(minerAddress))
                .add("nonce", nonce + "")
                .add("block", blockID + "")
                .add("reward", new RiverCoin(new BigInteger(reward)).toRiverCoinString()).toString();
    }

//    @Override
//    public byte[] header()
//    {
//        return new byte[0];
//    }
//
//    @Override
//    public byte[] content()
//    {
//        return new byte[0];
//    }

    @Override
    public void export(SmartDataTransferer smdt)
    {
    }

    public void setRiverMerkleRoot(byte[] encode)
    {
        this.riverMerkleRoot = encode;
    }

    public void setVersion(long versionBytes)
    {
        this.version = versionBytes;
    }

    public long getVersion()
    {
        return version;
    }

    public byte[] getrvcRoot()
    {
        return riverMerkleRoot;
    }

    public void setReward(RiverCoin riverCoin)
    {
        this.reward = riverCoin.getBytes();
    }

    public byte[] getReward()
    {
        return reward;
    }
}