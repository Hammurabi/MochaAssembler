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

package com.riverssen.core.security;

import com.riverssen.core.system.Logger;
import com.riverssen.core.utils.Base58;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.PrivateKey;
import java.security.Signature;

public class PrivKey
{
    private PrivateKey key;

    public PrivKey(PrivateKey key)
    {
        this.key = key;
    }

    public boolean verifySignature(PubKey pky, byte data[])
    {
        return false;
    }

    public String sign(String data)
    {
        return sign(data.getBytes());
    }

    public String sign(byte data[])
    {
        byte output[] = signedBytes(data);

        return Base58.encode(output);
    }

    public byte[] signEncoded(byte data[])
    {
        byte output[] = signedBytes(data);

        return (output);
    }

    public byte[] signedBytes(byte data[])
    {
        Signature dsa;
        byte[] output = new byte[1];

        try
        {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(key);
            byte[] strByte = data;
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        } catch (Exception e)
        {
            Logger.err("couldn't sign data!");
            e.printStackTrace();
        }

        return output;
    }

    public Key getPrivate()
    {
        return key;
    }

    public String decrypt(byte msg[])
    {
        try{
            Cipher cipher = Cipher.getInstance("ECDSA", "BC");
            cipher.init(Cipher.DECRYPT_MODE, key);

            return new String(cipher.doFinal(msg));
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return "";
    }
}
