package com.riverssen.core.system;

import com.riverssen.core.utils.HashUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Benchmark
{
    public static double runCPUBenchmark() throws InterruptedException
    {
        List<String> tokens = new ArrayList<>();
        List<String> hashes = new ArrayList<>();

        String string   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase() + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789";
        List<Byte> chr  = new ArrayList();

        for(byte b : (string.getBytes()))
            chr.add(b);

//        ExecutorService service = Executors.newFixedThreadPool(threads);

//        for(int t = 0; t < threads * 2; t ++)
//        {
//            int max = threads * 2;
//            int stt = 1000000 / max;
//
//            class r implements Runnable{
//
//                int t;
//                r(int t)
//                {
//                    this.t = t;
//                }
//
//                @Override
//                public void run()
//                {
//                    for(int i = (stt * t); i < (stt * t) + t; i ++)
//                    {
//                        Collections.shuffle(chr);
//                        String shuffled = "";
//
//                        for(Byte b : chr) shuffled += new String(new byte[] { b.byteValue() });
//
//                        tokens.add(shuffled);
//                    }
//                }
//            }
//
//            service.execute(new r(t));
//        }

//        service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

//        while(!service.isShutdown())
//        {
//        }

        for(int i = 0; i < 1000000; i ++)
        {
            Collections.shuffle(chr);
            String shuffled = "";

            for(Byte b : chr) shuffled += new String(new byte[] { b.byteValue() });

            tokens.add(shuffled);
        }

        long now = System.currentTimeMillis();

        for(String hshsting : tokens) hashes.add(HashUtil.hashToStringBase16(HashUtil.applySha256(hshsting.getBytes())));

        now      = System.currentTimeMillis() - now;

        return (1000.0 / (now / 1000.0)) / 1000.0;// / (now / (double) tokens.size() / 1000.0);
    }
}
