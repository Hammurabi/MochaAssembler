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

package com.riverssen.core.headers;

import com.riverssen.core.BlockChain;
import com.riverssen.core.BlockPool;
import com.riverssen.core.TransactionPool;
import com.riverssen.core.networking.NetworkManager;
import com.riverssen.core.security.PublicAddress;
import com.riverssen.core.security.Wallet;
import com.riverssen.core.system.Config;
import com.riverssen.core.system.FileSpec;
import com.riverssen.riverssen.UTXOMap;

import java.io.DataInputStream;
import java.io.File;
import java.math.BigInteger;
import java.util.concurrent.ExecutorService;

public interface ContextI
{
    public long getTime();

    public long getVersionBytes();

    public boolean shouldMine();

    public ExecutorService getExecutorService();

    public NetworkManager getNetworkManager();

    public BlockPool getBlockPool();

    public TransactionPool getTransactionPool();

    public UTXOMap getUtxoManager();

    public PublicAddress getMiner();

    public Wallet getWallet();

    public Config getConfig();

    public void run();

    public HashAlgorithm getHashAlgorithm(byte blockHash[]);

    public BigInteger getDifficulty();

    public boolean isRunning();

    public BlockChain getBlockChain();

    boolean actAsRelay();

    void shutDown();

    default FileSpec getLedger()
    {
        return FileSpec.fromFile(new File(getConfig().getBlockChainTransactionDirectory()));
    }
}