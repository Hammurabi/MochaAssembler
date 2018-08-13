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

import com.riverssen.core.utils.Base58;
import com.riverssen.core.utils.HashUtil;

/** this interface represents an object that can be converted to a byte array for hashing **/
public interface Encodeable
{
    /** returns a raw byte array containing the hash of the object **/
    default byte[] encode(HashAlgorithm algorithm)
    {
        return algorithm.encode(getBytes());
    }
    /** return a hex representation of the object's hash **/
    default String encode16(HashAlgorithm algorithm)
    {
        return algorithm.encode16(getBytes());
    }
    /** return a hex representation of the object's hash **/

    default String encode32(HashAlgorithm algorithm)
    {
        return algorithm.encode32(getBytes());
    }
    /** return a hex representation of the object's hash **/

    default String encode58(HashAlgorithm algorithm)
    {
        return algorithm.encode58(getBytes());
    }
    /** return a hex representation of the object's hash **/

    default String encode64(HashAlgorithm algorithm)
    {
        return algorithm.encode64(getBytes());
    }

    /** returns a raw byte array containing the hash of the object **/
    default String encode16()
    {
        return HashUtil.hashToStringBase16(getBytes());
    }
    /** return a hex representation of the object's hash **/

    default String encode32()
    {
        return HashUtil.base36Encode(getBytes());
    }
    /** return a hex representation of the object's hash **/

    default String encode58()
    {
        return Base58.encode(getBytes());
    }
    /** return a hex representation of the object's hash **/

    default String encode64()
    {
        return HashUtil.base64StringEncode(getBytes());
    }
    /** return a byte array to feed into the hash function **/

    byte[] getBytes();
}
