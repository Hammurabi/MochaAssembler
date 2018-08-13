package com.riverssen.core.system;

import com.riverssen.core.block.FullBlock;
import com.riverssen.core.block.BlockHeader;
import com.riverssen.core.headers.ContextI;

import java.io.*;
import java.math.BigInteger;

public class LatestBlockInfo
{
    private static boolean  available = true;
    private long            lastBlockCheck;
    private long            lastBlockCheckTimestamp;
    private long            lastBlock;
    private BigInteger      difficulty;
    private BigInteger      totalHashes;
    private Config          config;

    public LatestBlockInfo(Config config)
    {
        this.config = config;
    }

    public synchronized void read() throws Exception
    {
        while(!available)
        {
        }

        File file = new File(config.getBlockChainDirectory() + File.separator + "latestblock");

        if(!file.exists())
        {
            lastBlock = -1;
            lastBlockCheck = 0;
            lastBlockCheckTimestamp = System.currentTimeMillis();
            difficulty = Config.getMinimumDifficulty();
            totalHashes = BigInteger.ONE;

            return;
        }

        available = false;

        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));

        lastBlock               = dataInputStream.readLong();
        lastBlockCheck          = dataInputStream.readLong();
        lastBlockCheckTimestamp = dataInputStream.readLong();

        byte difficultyByte[]   = new byte[dataInputStream.readInt()];
        dataInputStream.read(difficultyByte);

        difficulty              = new BigInteger(difficultyByte);
        byte totalHashesArrray[] = new byte[dataInputStream.readInt()];

        dataInputStream.read(totalHashesArrray);

        totalHashes             = new BigInteger(totalHashesArrray);

        dataInputStream.close();

        available = true;
    }

    public synchronized void write(long lastBlock, long lastBlockCheck, long lastBlockCheckTimestamp, BigInteger difficulty, BigInteger totalHashes) throws Exception
    {
        while(!available)
        {
        }

        available = false;

        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(new File(config.getBlockChainDirectory() + File.separator + "latestblock")));

        dataOutputStream.writeLong(lastBlock);
        dataOutputStream.writeLong(lastBlockCheck);
        dataOutputStream.writeLong(lastBlockCheckTimestamp);
        dataOutputStream.writeInt(difficulty.toByteArray().length);
        dataOutputStream.write(difficulty.toByteArray());
        dataOutputStream.writeInt(totalHashes.toByteArray().length);
        dataOutputStream.write(totalHashes.toByteArray());

        dataOutputStream.flush();
        dataOutputStream.close();

        available = true;
    }

    public synchronized long getLatestBlock()
    {
        return lastBlock;
    }

    public synchronized long getLastBlockCheck()
    {
        return lastBlockCheck;
    }

    public synchronized long getLastBlockCheckTimestamp()
    {
        return lastBlockCheckTimestamp;
    }

    public synchronized BigInteger getDifficulty()
    {
        return difficulty;
    }

    public BigInteger getTotalHashes()
    {
        return totalHashes;
    }

    public BlockHeader getLatestBlockHeader(ContextI context)
    {
        if(lastBlock >= 0)
            return new BlockHeader(lastBlock, context);
        return null;
    }

    public FullBlock getLatestFullBlock(ContextI context)
    {
        if(lastBlock >= 0)
            return BlockHeader.FullBlock(lastBlock, context);
        return null;
    }
}