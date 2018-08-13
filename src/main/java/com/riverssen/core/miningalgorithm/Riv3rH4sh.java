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

package com.riverssen.core.miningalgorithm;

import com.riverssen.core.algorithms.*;
import com.riverssen.core.headers.ContextI;
import com.riverssen.core.headers.HashAlgorithm;
import com.riverssen.core.mpp.objects.RSA;
import com.riverssen.core.security.AdvancedEncryptionStandard;
import com.riverssen.core.system.FileSpec;
import com.riverssen.core.utils.Base58;
import com.riverssen.core.utils.ByteUtil;
import com.riverssen.core.utils.HashUtil;
import com.riverssen.core.utils.Tuple;

import javax.crypto.Cipher;
import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class Riv3rH4sh
{
    private static final HashAlgorithm   hash = new RiverHash();
    private static final HashAlgorithm   skein= new Skein_1024_1024();
    private final long                   buffer;
    private volatile long                nonce;
    private volatile byte[]              hash_;

    public Riv3rH4sh(long blockID, ContextI context)
    {
        long bph = (60_000L * 60L) / context.getConfig().getAverageBlockTime();

        int mod = Math.max(1, (int)(blockID / (182.5 * 24 * bph)));

        //0.5 Gb buffer / 262800 blocks. (every half a year an incease of 0.5Gb) memory is needed.
        buffer = 524_288 * mod;

        nonce = -1;
    }

    public static byte[] custom_hash(byte input[])
    {
        return hash.encode(input);
    }

    private static byte[] pad(byte input[], int length)
    {
        if(input.length < length)
        {
            byte newinput[] = new byte[length];

            int i = 0;

            for(int j = 0; j < input.length; j ++)
                newinput[j] = input[i ++];

            while (i < length)
                newinput[i ++] = 0;
        }

        return ByteUtil.trim(input, 0, length);
    }

    private void mine(byte input[], ContextI context) throws Exception
    {
        nonce ++;

        verify_v3(input, nonce);
    }

    private byte[] verify(byte input[], long nonce, ContextI context) throws Exception
    {
        int nonce_ = 0;

        byte input_hash[]               = custom_hash(ByteUtil.concatenate(input, custom_hash(ByteUtil.encode(nonce))));
        AdvancedEncryptionStandard aes  = new AdvancedEncryptionStandard(input_hash);

        byte encrypted_input[]          = aes.encrypt(input);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(encrypted_input);

        while(byteArrayOutputStream.size() < buffer)
        {
            input = ByteUtil.concatenate(
                    getInput(new BigInteger(custom_hash(ByteUtil.concatenate(input_hash, custom_hash(ByteUtil.encode(nonce_ ++ * nonce))))).abs().mod(new BigInteger(Math.max(1, context.getBlockChain().currentBlock()) + "").abs()).abs().longValue(), context), byteArrayOutputStream.toByteArray());

            input_hash                        = custom_hash(ByteUtil.concatenate(input_hash, input, custom_hash(ByteUtil.encode(nonce))));

            encrypted_input                   = aes.encrypt(input_hash, input);

            byteArrayOutputStream.write(encrypted_input);
        }

        byteArrayOutputStream.flush();
        byteArrayOutputStream.close();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(aes.encrypt(byteArrayOutputStream.toByteArray()));

        hash_ = custom_hash(byteArrayOutputStream.toByteArray());
        byte prng[] = new byte[32];

        while (inputStream.available() > 0)
        {
            inputStream.read(prng);
            hash_ = custom_hash(ByteUtil.xor(hash_, prng));
        }

        inputStream.close();

        String hash = HashUtil.hashToStringBase16(hash_);

        while (hash.length() < 64) hash = '0' + hash;

        hash_ = hexStringToByteArray(hash);

        return hash_;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public byte[] verify_v3(byte input[], long nonce) throws Exception
    {
        /** generate an encoded version of input, using nonce as a seed for the AES key **/
        byte encoded[] = generate_random(input, nonce);
        /** generate a 32byte hash key from the encoded bytes **/
        byte aes_key[] = custom_hash(encoded);

        /** set the hash to the encoded version of input xor encoded using the previous aes key **/
        hash_ = AdvancedEncryptionStandard.encrypt(aes_key, ByteUtil.xor(input, encoded));

        /** keep repeating the operations sequentially until the buffer limit is reached **/
        while (encoded.length < buffer)
        {
            byte olde[] = encoded;
            long seed = ByteUtil.decode(ByteUtil.xor(ByteUtil.xor(aes_key, ByteUtil.xor(input, encoded)), custom_hash(hash_)));

            encoded = ByteUtil.concatenate(generate_random(encoded, seed), encoded);
            aes_key = custom_hash(encoded);

            hash_   = AdvancedEncryptionStandard.encrypt(aes_key, ByteUtil.xor(olde, encoded));
        }

        /** encode the hash using skein-1024-1024 giving us 32 integers **/
        ByteBuffer _32i_ = ByteBuffer.wrap(skein.encode(hash_));
        /** encode the data one last time into a bytebuffer **/
        ByteBuffer _enc_ = ByteBuffer.wrap(AdvancedEncryptionStandard.encrypt(custom_hash(_32i_.array()), ByteUtil.concatenate(encoded, hash_, aes_key)));
        /** create 32 2048 byte blocks **/
        byte[][]   _blk_ = new byte[32][2048];
        int        _inx_ = 0;

        /** loop 32i until all the random integers are used to fill the _blk_ from _enc_ **/
        while (_32i_.remaining() > 0)
        {
            int i = Math.abs(_32i_.getInt())/(_enc_.capacity() - 2048);
            int j = _inx_ ++;

            _enc_.position(i);

            _enc_.get(_blk_[j]);
        }

        int max = 32;

        /** stack operation push pop xor pop **/

        while (max > 1)
        {
            _blk_[max - 2] = ByteUtil.xor(_blk_[max - 2], _blk_[max - 1]);
            max -= 1;
        }

        /** put the last xor value in a buffer **/
        ByteBuffer _fnl_ = ByteBuffer.wrap(_blk_[0]);
        byte[][]   _bck_ = new byte[64][32];

        _inx_            = 0;


        /** unwrap the 2048 bits into 64 32 byte integers **/
        while (_fnl_.remaining() > 0)
            _fnl_.get(_bck_[_inx_ ++]);

        max = 64;

        /** xor stack operation hashed **/
        while (max > 1)
        {
            _bck_[max - 2] = custom_hash(ByteUtil.xor(_bck_[max - 2], _bck_[max - 1]));
            max -= 1;
        }

        /** return 256bit hash result **/
        hash_ = _bck_[0];

        return hash_;
    }

    public byte[] verify_v2(byte input[], long nonce, ContextI context) throws Exception
    {
        byte input_hash[] = custom_hash(ByteUtil.concatenate(input, custom_hash(ByteUtil.encode(nonce))));
        AdvancedEncryptionStandard aes = new AdvancedEncryptionStandard(input_hash);

        byte encrypted_input[] = aes.encrypt(input);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(input);
        byteArrayOutputStream.write(encrypted_input);

        while (byteArrayOutputStream.size() < buffer) {
            input_hash = custom_hash(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.write(input_hash);
            encrypted_input = aes.encrypt(input_hash, byteArrayOutputStream.toByteArray());

            byteArrayOutputStream.write(encrypted_input);
        }

        byteArrayOutputStream.flush();
        byteArrayOutputStream.close();

        hash_ = custom_hash(byteArrayOutputStream.toByteArray());

//        byte random[]       = generate_random(input, nonce);
//
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//
//        byteArrayOutputStream.write(ByteUtil.encode(nonce));
//        byteArrayOutputStream.write(input);
//        byteArrayOutputStream.write(random);
//
//        while (byteArrayOutputStream.size() < buffer)
//        {
//            long new_seed   = new BigInteger(custom_hash(byteArrayOutputStream.toByteArray())).divide(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
//
//            random          = generate_random(input, new_seed);
//            byteArrayOutputStream.write(random);
//
//            byte data[]     = byteArrayOutputStream.toByteArray();
//
//            byteArrayOutputStream.reset();
//
//            byteArrayOutputStream.write(AdvancedEncryptionStandard.encrypt(custom_hash(data), data));
//            System.out.println(byteArrayOutputStream.size());
//        }
//
//        byteArrayOutputStream.flush();
//        byteArrayOutputStream.close();
//
//        hash_ = custom_hash(byteArrayOutputStream.toByteArray());

        return hash_;
    }

    private byte[] generate_random(byte input[], long seed) throws Exception
    {
        byte input_hash[] = custom_hash(input);
        byte iseed_hash[] = custom_hash(ByteUtil.concatenate(ByteUtil.encode(seed), input_hash, input));

        byte aesky_pblk[] = ByteUtil.xor(input_hash, iseed_hash);

        return AdvancedEncryptionStandard.encrypt(custom_hash(aesky_pblk), input);
    }

    private byte[] getInput(long l, ContextI context) throws IOException
    {
        File file = new File(context.getConfig().getBlockChainDirectory() + File.separator + "block[" + l + "]");

        if(!file.exists()) return new byte[2560];

        byte input[] = new byte[(int) file.length()];

        DataInputStream stream = new DataInputStream(new FileInputStream(file));

        stream.read(input);

        stream.close();

        return input;
    }

    public Tuple<byte[], Long> mine(byte input[], BigInteger difficulty, ContextI context) throws Exception
    {
        mine(input, context);

        /** check resulting UNSIGNED hash is less than difficulty **/
        while (new BigInteger(hash_).abs().compareTo(difficulty) >= 0)
            mine(input, context);

        /** unsign the hash as a final operation **/
        hash_ = new BigInteger(hash_).abs().toByteArray();

        String hash = HashUtil.hashToStringBase16(hash_);

        /** add leading zeros (BigInteger removes them) **/
        while (hash.length() < 64)
            hash = '0' + hash;

        /** convert it back to a byte array **/
        hash_ = hexStringToByteArray(hash);

        /** return it in a pointer **/
        return new Tuple<>(hash_, nonce);
    }
}
