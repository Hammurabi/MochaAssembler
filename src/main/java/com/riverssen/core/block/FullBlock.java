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

import com.riverssen.core.miningalgorithm.Riv3rH4sh;
import com.riverssen.core.system.Logger;
import com.riverssen.core.RiverCoin;
import com.riverssen.core.headers.*;
import com.riverssen.core.security.PublicAddress;
import com.riverssen.core.system.Config;
import com.riverssen.core.system.LatestBlockInfo;
import com.riverssen.core.transactions.RewardTransaction;
import com.riverssen.core.utils.*;
import com.riverssen.riverssen.RiverFlowMap;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.zip.DeflaterOutputStream;

public class FullBlock implements Encodeable, JSONFormattable, Exportable
{
    public static int err_not_valid = 1;
    public static int err_mrkl = 2;
    public static int err_transactions = 3;
    public static int err_timestampa = 4;
    public static int err_timestampb = 5;
    public static int err_timestampc = 6;

    /** parent header file **/
    private volatile BlockHeader parent;
    /** this blocks hash **/
    private volatile byte[]      hash;
    /** this blocks header **/
    private volatile BlockHeader header;
    /** this blocks body **/
    private volatile BlockData   body;
    /** the context of the program **/
    private volatile ContextI    context;

    private volatile RiverFlowMap map;

    public FullBlock(BlockHeader header, BlockData data, BlockHeader parent, ContextI context)
    {
        this.header             = header;
        this.body               = data;
        this.parent             = parent;
        this.map                = new RiverFlowMap(getBlockID(), context);
        this.context            = context;
        this.validateBody();
    }

    public FullBlock(long lastBlock, BlockHeader parent, ContextI context)
    {
        this.header         = new BlockHeader();
        this.body           = new BlockData();
        this.parent         = parent;
        this.header.setBlockID(lastBlock + 1);
        this.map            = new RiverFlowMap(getBlockID(), context);
        this.context        = context;
    }

    public FullBlock(DataInputStream in, ContextI context)
    {
        this.header             = new BlockHeader(in);
        this.body               = new BlockData(in, context);
        if(header.getBlockID() > 0) this.parent = new BlockHeader(header.getBlockID() - 1, context);
        this.map                = new RiverFlowMap(getBlockID(), context);
        this.context            = context;
        this.validateBody();
    }

    public String getHashAsString()
    {
        return header.getHashAsString();
    }

    public long getBlockID()
    {
        return header.getBlockID();
    }

    public void validateBody()
    {
        this.body.validate(map, context);
    }

    public int validate(ContextI context)
    {
        return validate(getBlockID() > 0 ? new BlockHeader(getBlockID() - 1, context) : null, context);
    }

    public void undoUTXOChanges(ContextI context)
    {
        body.revertOutputs(map, context);
    }

    public int validate(BlockHeader parent, ContextI context)
    {
        byte pHash[] = null;

        if (parent == null)
            pHash = new byte[32];
        else pHash = parent.getHash();

        if(parent == null && getBlockID() > 0) return err_not_valid;

//        getBody().getMerkleTree().buildTree();
//
        /** validate merkle root **/
//        if (!ByteUtil.equals(body.getMerkleTree().encode(ByteUtil.defaultEncoder()), header.getMerkleRoot())) return err_mrkl;

        /** validate transactions **/
        if (!body.transactionsValid()) return err_transactions;

        /** verify timestamp **/
//        if (body.getTimeStamp() != header.getTimeStampAsLong()) return err_timestamp;
        long timeDifference = 0;

        if(parent != null)
            timeDifference = getHeader().getTimeStampAsLong() - parent.getTimeStampAsLong();

        if(timeDifference >= (context.getConfig().getAverageBlockTime() * 50)) return err_timestampa;

        if(timeDifference < (context.getConfig().getAverageBlockTime() * -50)) return err_timestampb;

        /** verify block started mining at least lastblock_time + blocktime/5 after **/
//        if (body.getTimeStamp() <= (header.getTimeStampAsLong() + (context.getConfig().getAverageBlockTime() / 5))) return err_timestampc;

                getBody().getMerkleTree().buildTree();

        /** verify nonce **/
        HashAlgorithm algorithm = context.getHashAlgorithm(pHash);
        ByteBuffer data = getBodyAsByteBuffer();
        data.putLong(data.capacity() - 8, header.getNonceAsInt());
        byte hash[] = algorithm.encode(data.array());

        System.out.println(Base58.encode(header.getMinerAddress()));

        if(!ByteUtil.equals(this.header.getHash(), hash)) return 7;

        if(new BigInteger(hash).compareTo(header.getDifficultyAsInt()) >= 0) return 8;

        if(!PublicAddress.isPublicAddressValid(Base58.encode(header.getMinerAddress()))) return 9;

        return 0;
    }

    public void add(TransactionI token, ContextI context)
    {
        body.add(token, map, context);
    }

    /**
     * mine the block
     **/
    public void mine(ContextI context) throws Exception {
        byte parentHash[] = this.parent != null ? this.parent.getHash() : new byte[32];

        HashAlgorithm algorithm     = context.getHashAlgorithm(parentHash);
        BigInteger    difficulty    = context.getDifficulty();
        PublicAddress miner         = context.getMiner();

        String difficultyHash = HashUtil.hashToStringBase16(difficulty.toByteArray());
        while (difficultyHash.length() < 64) difficultyHash = "0" + difficultyHash;

        Logger.alert("--------------------------------");
        Logger.alert("[" + TimeUtil.getPretty("H:M:S") + "][" + header.getBlockID() + "]: new job {"+ algorithm.getClass().getSimpleName() + "{" + algorithm.toString() + "}:" + (difficultyHash) + "}");

        long time_stamp = System.currentTimeMillis();
        body.getMerkleTree().buildTree();

        //System.out.println(new RewardTransaction(miner).toJSON());

        /** add the reward BEFORE mining the block **/
        body.addNoValidation(new RewardTransaction(miner, algorithm.encode(body.getBytes())), map, context);
        /** rebuild tree to include reward **/
        body.getMerkleTree().buildTree();

        header.setVersion(context.getVersionBytes());
        header.setParentHash(parentHash);
        header.setMerkleRoot(body.getMerkleTree().encode(ByteUtil.defaultEncoder()));
        header.setRiverMerkleRoot(body.getMerkleTree().encode(ByteUtil.defaultEncoder()));
        header.setTimeStamp(time_stamp);
        header.setDifficulty(difficulty);
        header.setMiner(context.getMiner());
        header.setReward(new RiverCoin(Config.getReward(getBlockID())));

        ByteBuffer data = getBodyAsByteBuffer();

//        long nonce = 0;
//        this.hash = algorithm.encode(data.array());

//        while (new BigInteger(hash).compareTo(difficulty) >= 0) { data.putLong(data.capacity() - 8, ++nonce); this.hash = algorithm.encode(data.array()); }

        Riv3rH4sh mining_algorithm = new Riv3rH4sh(getBlockID(), context);

        Tuple<byte[], Long> mine = mining_algorithm.mine(data.array(), difficulty, context);

//        Riv3rH4sh miner1 = new Riv3rH4sh(context);
//
//        System.out.println(ByteUtil.equals(miner1.verify(data.array(), mine.getJ(), context), mine.getI()));

        header.setHash(mine.getI());
        header.setNonce(mine.getJ());

        this.hash = mine.getI();

        double time = (System.currentTimeMillis() - time_stamp) / 1000.0;

        BigDecimal hashRate = new BigDecimal(mine.getJ()).divide(new BigDecimal(time).divide(new BigDecimal(1000.0), 10, RoundingMode.HALF_UP), 10, RoundingMode.HALF_UP);
        Logger.alert("[" + TimeUtil.getPretty("H:M:S") + "][" + header.getBlockID() + "]: hashing took '" + time + "s' '" + HashUtil.hashToStringBase16(hash) + "'\ntotal transactions: " + body.getMerkleTree().flatten().size());
        Logger.alert("[" + TimeUtil.getPretty("H:M:S") + "][" + header.getBlockID() + "]: hashrate of '"+hashRate.toPlainString()+"h/s'}");
    }

    public BlockHeader getHeader()
    {
        return header;
    }

    @Override
    public byte[] getBytes()
    {
        return body.getBytes();
    }

    public BlockData getBody()
    {
        return body;
    }

    private ByteBuffer getBodyAsByteBuffer()
    {
        byte bodydata[] = getBytes();
//        8 + 32 + 32 + 8 + PublicAddress.SIZE + RiverCoin.SIZE + 8 +
        ByteBuffer data = ByteBuffer.allocate(32 + 32 + 8 + 8 + PublicAddress.SIZE + RiverCoin.SIZE + bodydata.length + 8);

//        data.putLong(header.getVersion());
        data.put(header.getParentHash());
        data.put(header.getMerkleRoot());
        data.putLong(header.getTimeStamp());
        data.put(header.getMinerAddress());
        data.put(header.getReward());
        data.putLong(header.getBlockID());
//        data.put(header.getrvcRoot());
        data.put(bodydata);
        data.putLong(0);
        data.flip();
        return data;
    }

    public void serialize(ContextI context)
    {
        File file = new File(context.getConfig().getBlockChainDirectory() + File.separator + "block["+getBlockID()+"]");

        try
        {
            DataOutputStream stream = new DataOutputStream(new DeflaterOutputStream(new FileOutputStream(file)));

            export(stream);

            stream.flush();
            stream.close();

            LatestBlockInfo info = new LatestBlockInfo(context.getConfig());
            info.read();

            info.write(getBlockID(), info.getLastBlockCheck(), info.getLastBlockCheckTimestamp(), info.getDifficulty(), info.getTotalHashes());

            map.serialize(context);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public FullBlock getParent(ContextI context)
    {
        return BlockHeader.FullBlock(getBlockID()-1, context);
    }

    @Override
    public void export(SmartDataTransferer smdt) {
    }

    @Override
    public void export(DataOutputStream dost) throws IOException {
        header.export(dost);
        body.export(dost);
    }

    @Override
    public String toJSON() {
        return new JSON().add(header.toString())
                .add(new JSON("outputs", true).add(outputsAsJSON()).toString())
                .add(new JSON("body").add(body.getMerkleTree().toString()).toString()).toString();
    }

    private String outputsAsJSON() {
        String data = "";

//        for(TransactionOutput output : getBody().getOutputs())
//            data += output.toString() + ", ";

        if (data.length() == 0) return "";

        return data.substring(0, data.length() - 2);
    }

    public void free(ContextI context)
    {
        context.getTransactionPool().putBack(getBody().getMerkleTree().flatten());
    }
}