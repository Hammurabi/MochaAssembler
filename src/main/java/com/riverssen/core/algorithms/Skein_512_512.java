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

public class Skein_512_512 implements HashAlgorithm
{
    @Override
    public byte[] encode(byte[] data)
    {
        return HashUtil.applySkein(data, 512, 512);
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