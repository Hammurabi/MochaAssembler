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

package com.riverssen.core;

import com.riverssen.core.block.BlockDownloadService;
import com.riverssen.core.block.BlockHeader;
import com.riverssen.core.block.FullBlock;
import com.riverssen.core.headers.ContextI;
import com.riverssen.core.networking.Client;
import com.riverssen.core.networking.messages.RequestBlockMessage;
import com.riverssen.core.system.LatestBlockInfo;
import com.riverssen.core.system.Logger;
import com.riverssen.core.utils.ByteUtil;
import com.riverssen.core.utils.FileUtils;
import com.riverssen.core.utils.Handler;
import com.riverssen.riverssen.Constant;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class BlockChain implements Runnable
{
    private volatile Set<BlockDownloadService>                  orphanedBlocks;
//    private Map<Client, Set<FullBlock>>     downloadedBlocks;
    private volatile Set<BlockDownloadService>              downloadedBlocks;
    private volatile Handler<FullBlock>              block;
    private volatile ContextI                        context;
    private volatile long                            lastvalidated;
    private volatile ReentrantLock                   lock;
    private volatile boolean                         mlock;
    private volatile AtomicLong                      current;

    public BlockChain(ContextI context)
    {
        this.context        = context;
        this.orphanedBlocks = Collections.synchronizedSet(new LinkedHashSet<>());
        this.downloadedBlocks= Collections.synchronizedSet(new LinkedHashSet<>());
        this.lock           = new ReentrantLock();
        this.block          = new Handler<>(null);
        this.current        = new AtomicLong(0);
    }

    public synchronized void FetchTransactions()
    {
    }

    public synchronized void ValidateTransactions()
    {
    }

    public synchronized void RemoveDoubleSpends()
    {
    }

    public synchronized void LoadBlockChain()
    {
    }

    public synchronized BlockHeader lastBlockHeader()
    {
        lock.lock();
        try{
            if(currentBlock() == 0) return null;
            return new BlockHeader(currentBlock() - 1, context);
        } finally {
            lock.unlock();
        }
    }

    public void download(BlockDownloadService block)
    {
//        lock.lock();
//        try{
            this.downloadedBlocks.add(block);
//        } finally {
//            lock.unlock();
//        }
    }

    public synchronized void FetchBlockChainFromPeers()
    {
        Logger.alert("attempting to download block(s) from peers");
        @Constant Set<Client> communicators = new LinkedHashSet<>(context.getNetworkManager().getCommunicators());

//        context.getNetworkManager().downloadLongestChain();
        Logger.alert("listing...");

        List<Client> nodes = new ArrayList<>(communicators);
        Logger.alert("arranging...");

        //Descending Ordered List
        nodes.sort((a, b)->{
            if(a.getChainSize() == b.getChainSize()) return 0;
            else if(a.getChainSize() > b.getChainSize()) return -1;
            else return 1;
        });

        Logger.alert("rearranging...");

        if(nodes.size() == 0) {
            Logger.alert("no nodes found.");
            return;
        }

        long startingPoint = currentBlock();

        Logger.alert("client '" + nodes.get(0).getChainSize() + "' block(s) behind");

        client_iterator :
            for(Client node : nodes) {
                Logger.alert("attempting: " + node.toString());

                if(node.isBlocked()) continue;
                String lock = ByteUtil.defaultEncoder().encode58((System.currentTimeMillis() + " BlockChainLock: " + node).getBytes());

                //Wait for node to unlock.
                while (!node.lock(lock)) {}

                for (long i = context.getBlockChain().currentBlock(); i < node.getChainSize(); i++)
                    if (i >= 0) node.sendMessage(new RequestBlockMessage(i));

                long required = node.getChainSize() - Math.max(currentBlock(), 0);
                Logger.alert("required: " + required);

                long lastChange = System.currentTimeMillis();
                long lastChainS = startingPoint;

                while (currentBlock() < currentBlock()+required)
                {
                    if(lastChainS != currentBlock())
                    {
                        lastChange = System.currentTimeMillis();
                        lastChainS = currentBlock();
                    }

                    if (System.currentTimeMillis() - lastChange > ((3.75 * 60_000L) * required))
                        Logger.err("a network error might have occurred, no updates to the network.");

                    if (System.currentTimeMillis() - lastChange > ((7.5 * 60_000L) * required))
                    {
                        Logger.err("a network error might have occurred, '" + downloadedBlocks.size() + "' downloaded out of '" + required + "'.");
                        Logger.alert("forking...");

                        node.block();
                        downloadedBlocks.clear();
                        break client_iterator;
                    }

                    if(downloadedBlocks.size() > 0)
                    {
                        Logger.alert("acquired: " + currentBlock() + "/" + (currentBlock()+required) + " remaining: " + downloadedBlocks.size());
                        FullBlock next  = null;

                        Set<BlockDownloadService> downloadServices = new LinkedHashSet<>();
                        synchronized (downloadedBlocks)
                        {
                            downloadServices.addAll(downloadedBlocks);
                        }

                        for(BlockDownloadService fullBlock : downloadServices)
                            if(fullBlock.getBlockID() == (currentBlock() + 1))
                            {
                                next = new FullBlock(fullBlock.decompressedInputStream(), context);
                                break;
                            }

                            if(next != null)
                            {
                                int reason = 0;
                                if((reason = next.validate(context)) != 0)
                                {
                                    Logger.err(next + " rejected '"+reason+"'.");
                                    node.block();

                                    for(long i = startingPoint; i < currentBlock(); i ++)
                                        FileUtils.deleteblock(i, context);

                                    break client_iterator;
                                }

                                this.block.set(next);
                                this.block.get().serialize(context);
                                downloadedBlocks.remove(next);
                                node.sendMessage(new RequestBlockMessage(currentBlock() + 1));
                            }

                        node.sendMessage(new RequestBlockMessage(currentBlock() + 1));
                    }
                }

                node.unlock(lock);
            }
    }

    public synchronized void FetchBlockChainFromDisk()
    {
        Logger.alert("attempting to load the blockchain from disk");

        LatestBlockInfo info = new LatestBlockInfo(context.getConfig());
        try
        {
            info.read();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        long latestblock = info.getLatestBlock();

        if(latestblock < 0) return;

        this.block.set(BlockHeader.FullBlock(latestblock, context));

        Logger.alert("block loaded successfully");
    }

    public synchronized void Validate()
    {
        /** check (25 minutes) time passed since last validation **/
        if(System.currentTimeMillis() - lastvalidated < 1_500_000L) return;
        Logger.alert("attempting to validate block");

        lastvalidated = System.currentTimeMillis();

//        Tuple<String, Long> forkInfo = context.getNetworkManager().getForkInfo();
//        long latestFork = forkInfo.getJ();

        /** check that our block is the longest block, if it is, then return **/
//        if(latestFork < block.getBlockID()) return;

        /** if our block is short then update it with the longest block **/
//        if(block.getBlockID() < forkInfo.getJ())
//            FetchBlockChainFromPeers();
    }

    public long currentBlock()
    {
//        lock.lock();
//        try{
//            if(block == null) return -1;
//
//            return block.get().getHeader().getBlockID();
//        } finally {
//            lock.unlock();
//        }
        if(block.get() == null) return -1;
        return block.get().getBlockID();
    }

    public void queueBlock(BlockDownloadService block)
    {
//        lock.unlock();
//        try{
            orphanedBlocks.add(block);
//        } finally {
//            lock.unlock();
//        }
    }

    public void run()
    {
//        Wallet wallet = new Wallet("test", "test");
//        context.getTransactionPool().addInternal(new Transaction(wallet.getPublicKey().getCompressed(), context.getMiner(), new TXIList(), new RiverCoin("12.0"), "bro").sign(wallet.getPrivateKey()));

        FetchBlockChainFromDisk();
//        FetchBlockChainFromPeers();
//        if(block == null)
//            block = new FullBlock(-1, new BlockHeader());

        FetchBlockChainFromPeers();

//        Validate();

//        System.out.println(block.toJSON());

        if(block.get() == null)
            block.set(new FullBlock(-1, null, context));
        else block.set(block.get().getHeader().continueChain(context));

//        System.exit(0);

        Logger.alert("finished processes.");

        Set<BlockDownloadService> delete = new LinkedHashSet<>();
        List<BlockDownloadService> blockList = new ArrayList<>();

        long lastBlockWas = 1L;

        while(context.isRunning())
        {
            current.set(block.get().getBlockID());

            for (BlockDownloadService block : orphanedBlocks)
                if(block.getBlockID() < currentBlock() - 1)
                    delete.add(block);

            orphanedBlocks.removeAll(delete);

            delete.clear();

            if(System.currentTimeMillis() - lastBlockWas >= context.getConfig().getAverageBlockTime())
            {
                orphancheck : while (orphanedBlocks.size() > 0)
                {
                    long current = currentBlock() - 1;

                    for(BlockDownloadService block : orphanedBlocks)
                        if(block.getBlockID() == current)
                            blockList.add(block);

                    orphanedBlocks.removeAll(blockList);

                    if(blockList.size() == 0) break orphancheck;

//                    blockList.sort((a, b)->{
//                        if          (a.getBlockID() == b.getBlockID()) return 0;
//                        else if     (a.getBlockID() > b.getBlockID()) return 1;
//
//                        return -1;
//                    });

                    for(BlockDownloadService download : blockList)
                    {
                        FullBlock block = new FullBlock(download.decompressedInputStream(), context);
                        if(block.validate(context) == 0)
                        {
                            /** This function should choose the biggest block in queue at the current level **/
                            /** first block downloaded **/
                            this.block.set(block);

                            this.block.get().serialize(context);
                            this.block.set(this.block.get().getHeader().continueChain(context));
                            lastBlockWas = System.currentTimeMillis();

                            break orphancheck;
                        }
                    }
                }
            }

            delete.clear();
            blockList.clear();

            if(mlock)
            {
            } else {
                while (context.getTransactionPool().available()) {
                    block.get().add(context.getTransactionPool().next(), context);
                    if (block.get().getBody().mine(context))
                        break;
                }

                if(System.currentTimeMillis() - lastBlockWas >= context.getConfig().getAverageBlockTime())
                {
                    while (orphanedBlocks.size() > 0)
                    {
                        BlockDownloadService orphan = orphanedBlocks.iterator().next();
                        FullBlock orphaned = new FullBlock(orphan.decompressedInputStream(), context);

                        orphanedBlocks.remove(orphan);

                        if(orphaned.getBlockID() == currentBlock() && orphaned.validate(context) == 0)
                        {
                            this.block.get().free(context);

                            this.block.set(orphaned);
                            this.block.get().serialize(context);
                            this.block.set(this.block.get().getHeader().continueChain(context));
                        }
                    }
                }

                if(block.get().getBody().mine(context))
                {
                    try{
                        block.get().mine(context);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    boolean continueBlock = true;

                    while (orphanedBlocks.size() > 0)
                    {
                        BlockDownloadService orphan = orphanedBlocks.iterator().next();
                        FullBlock orphaned = new FullBlock(orphan.decompressedInputStream(), context);

                        orphanedBlocks.remove(orphan);

                        if(orphaned.getBlockID() == currentBlock() && orphaned.validate(context) == 0 && orphaned.getHeader().getTimeStampAsLong() <= block.get().getHeader().getTimeStampAsLong())
                        {
                            this.block.get().free(context);

                            this.block.set(orphaned);
                            this.block.get().serialize(context);
                            this.block.set(this.block.get().getHeader().continueChain(context));
                            continueBlock = false;
                        }
                    }

                    if(continueBlock)
                    {
                        /** Send Solution To Nodes **/

                        try {
                            context.getNetworkManager().sendBlock(new BlockDownloadService(block.get()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        block.get().serialize(context);

                        block.set(block.get().getHeader().continueChain(context));
                        lastBlockWas = System.currentTimeMillis();
                    }
                }
            }
            current.set(block.get().getBlockID());
        }
    }

    public void insertBlock(FullBlock block)
    {
        if(currentBlock() + 1 == block.getBlockID())
        {
            if(this.block != null)
                this.block.get().serialize(context);
                this.block.set(block);
        }
    }
}