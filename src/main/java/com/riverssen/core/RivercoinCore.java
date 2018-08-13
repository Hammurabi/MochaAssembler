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

import com.riverssen.core.exceptions.FeatureUnavailableException;
import com.riverssen.core.miningalgorithm.Riv3rH4sh;
import com.riverssen.core.system.*;
import com.riverssen.core.headers.ContextI;
import com.riverssen.core.utils.ByteUtil;
import com.riverssen.core.utils.FileUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.Security;
import java.util.Enumeration;
import java.util.Random;

public class RivercoinCore
{
    /** main method takes two arguments String(type[CLIENT/MINER)) String(PathToConfigDirectory) **/
    public static void main(String args[]) throws Exception
    {
        /**
         * Add the bouncy castle provider
         */
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        if(args != null && args.length > 1)
        new RivercoinCore(args[0], args[1], args);
        else throw new RuntimeException("Please specify a rivercoin.config file.");
    }

    public static final long actual_version = ByteUtil.decode(new byte[]{'a', 0, 0, 0, 0, 0, 0, (byte)228});

    private RivercoinCore(String type, String file, String ...args) throws Exception
    {
        /** This Code Starts The Rivercoin Client **/
        /** create a context **/
        ContextI context = null;
        Config   config  = new Config(new File(file + File.separator));

        /** Generate directories if they don't exist **/
        FileUtils.createDirectoryIfDoesntExist(config.getBlockChainDirectory());
        FileUtils.createDirectoryIfDoesntExist(config.getBlockChainTransactionDirectory());
        FileUtils.createDirectoryIfDoesntExist(config.getBlockChainWalletDirectory());
        FileUtils.createDirectoryIfDoesntExist(config.getVSSDirectory());
        Logger.alert("----------------------------------------------------------------");
        Logger.alert("--------------------Welcome To Rivercoin Core-------------------");
        Logger.alert("----------------------------------------------------------------");
        Logger.alert("----------usable cpu threads: " + config.getMaxMiningThreads());

        switch (type)
        {
                //** A Node Will Collect And Relay Information But Won't Get Into Mining **/
            case "node":
                context = new NodeContext(config);
                throw new FeatureUnavailableException("NodeContext");
                //** Miners Act As Nodes But They Attempt To Mine To Get A Reward**/
            case "miner":
                context = new MiningContext(config);
                break;
            case "client":
                context = new ClientContext(config);
                break;
        }

        if(args != null && args.length > 2 && args[2].equals("-benchmark"))
        {
            Logger.alert("BENCHMARKING...");

            byte bench_mark_hash[] = new byte[4096];

            Random random = new Random();
            random.nextBytes(bench_mark_hash);


            Riv3rH4sh h4sh = new Riv3rH4sh(0, context);


            long benchmark_start_time = System.currentTimeMillis();


            for(int i = 0; i < 100; i ++)
                h4sh.verify_v3(bench_mark_hash, i);

            BigDecimal benchmark_time = new BigDecimal(System.currentTimeMillis() - benchmark_start_time).divide(new BigDecimal(1000.0), 10, RoundingMode.HALF_UP);
            Logger.alert("Finishded benchmarking.");
            Logger.alert("Benchmarking took '" + benchmark_time + "' seconds (1000 hashes).");

            Logger.alert("Benchmark result: " + new BigDecimal(1_000).divide(benchmark_time, 10, RoundingMode.HALF_UP).toPlainString() + "hashes/second");
        }

        System.out.println("\n\n\n\n\n\n\n\n");

//        Riv3rH4sh miner = new Riv3rH4sh(context);
//
//        System.out.println(HashUtil.hashToStringBase16(miner.verify_v3("hello world my name jeff 1 ds sd fsd f ds f dsf ds f ds f dsf ds f sd".getBytes(), 120)));
//
//        System.exit(0);

//        Riv3rH4sh miner = new Riv3rH4sh(context);

//        RiverHash     hash  = new RiverHash();
//        byte test[]         = new byte[1000000];
//        byte hfds[]         = new byte[1000000];

//        new Random(System.currentTimeMillis()).nextBytes(test);

//        long now = System.currentTimeMillis();

//        for(int i = 0; i < 24_686; i ++)
//            hfds = hash.encode(test);

//        BigInteger target = new BigDecimal(Config.getMinimumDifficulty()).divide(new BigDecimal(24_686), 200, BigDecimal.ROUND_HALF_UP).toBigInteger();
//        String difficultyHash = HashUtil.hashToStringBase16(target.toByteArray());
//        while (difficultyHash.length() < 64) difficultyHash = "0" + difficultyHash;
//        System.out.println(difficultyHash);
//
//        System.out.println(System.currentTimeMillis() - now);
//
//        System.exit(0);

        //Difficulty = At / Th
        //difficulty = 120_000 / 120

        Thread.sleep(1000);

        context.run();
    }
}