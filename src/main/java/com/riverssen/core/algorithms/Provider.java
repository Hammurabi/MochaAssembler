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

package com.riverssen.core.algorithms;

import com.riverssen.core.headers.HashAlgorithm;
import com.riverssen.core.utils.Base58;
import com.riverssen.core.utils.HashUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Provider
{
    private final List<HashAlgorithm> algorithms;
    private final Combined            combined;

    public Provider()
    {
        algorithms = new ArrayList<>();
        algorithms.add(new Sha4());
        algorithms.add(new Sha3());
        algorithms.add(new Sha256());
        algorithms.add(new Keccak());
        algorithms.add(new RipeMD256());
        algorithms.add(new Gost());

        combined = new Combined();
    }

    private class Combined implements HashAlgorithm {

        private HashAlgorithm algorithms_[];

        public Combined()
        {
            set(algorithms.get(0), algorithms.get(1), algorithms.get(2), algorithms.get(3), algorithms.get(4));
        }

        public void set(HashAlgorithm ... algorithms)
        {
            this.algorithms_ = algorithms;
        }

        @Override
        public byte[] encode(byte[] data)
        {
            byte[] hash = algorithms_[0].encode(data);

            for(int i = 1; i < algorithms_.length; i ++)
                hash = algorithms_[i].encode(hash);
            return hash;
        }

        @Override
        public String encode16(byte[] data)
        {
            return HashUtil.hashToStringBase16(encode(data));
        }

        @Override
        public String encode32(byte[] data)
        {
            return HashUtil.base36Encode(encode(data));
        }

        @Override
        public String encode58(byte[] data)
        {
            return Base58.encode(encode(data));
        }

        @Override
        public String encode64(byte[] data)
        {
            return HashUtil.base64StringEncode(encode(data));
        }

        @Override
        public String toString()
        {
            String text = "";

            for(int i = 0; i < algorithms_.length; i++)
                text += algorithms_[i].getClass().getSimpleName() + ", ";

            text = text.substring(0, text.length() - 2);

            return text;
        }
    }

    public HashAlgorithm getRandomFromHash(byte hash[])
    {
        Random r = new Random(new BigInteger(hash).longValue());
        int algorithm0 = r.nextInt(algorithms.size());
        int algorithm1 = r.nextInt(algorithms.size());
        int algorithm2 = r.nextInt(algorithms.size());
        int algorithm3 = r.nextInt(algorithms.size());
        int algorithm4 = r.nextInt(algorithms.size());
        int algorithm5 = r.nextInt(algorithms.size());

        combined.set(algorithms.get(algorithm0), algorithms.get(algorithm1), algorithms.get(algorithm2), algorithms.get(algorithm3), algorithms.get(algorithm4), algorithms.get(algorithm5));

        return combined;
    }
}
