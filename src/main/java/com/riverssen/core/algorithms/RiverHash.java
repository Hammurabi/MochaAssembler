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

public class RiverHash implements HashAlgorithm
{
    public static final HashAlgorithm keccak = new Keccak(), keccak2 = new Keccak384(), keccak3 = new Keccak512(), skein2 = new Skein_256_256(), skein3 = new Skein_512_512(), skein_4 = new Skein_1024_1024(), sha = new Sha1(), sha2 = new Sha256(), sha3 = new Sha3(), sha4 = new Sha4(), blake = new Blake(), gost = new Gost(), ripemd1 = new RipeMD128(), ripemd2 = new RipeMD160(), ripemd3 = new RipeMD256();

    @Override
    public byte[] encode(byte[] data)
    {
        return skein2.encode(keccak.encode(blake.encode(gost.encode(sha3.encode(skein3.encode(sha2.encode(ripemd2.encode(sha4.encode(ripemd3.encode(keccak2.encode(keccak3.encode(skein_4.encode(data)))))))))))));
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
}
