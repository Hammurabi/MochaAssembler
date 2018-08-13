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

import com.riverssen.core.system.Config;
import com.riverssen.core.system.Logger;
import com.riverssen.core.utils.Base58;
import com.riverssen.core.utils.ByteUtil;
import com.riverssen.core.utils.HashUtil;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;

public class PubKey
{
    public static final int SIZE_IN_BYTES = 85;

    private PublicKey           key;
    private CompressedAddress   ads;
    private PublicAddress       wad;

    public PubKey(byte address[])
    {
        this.key = addressToPublicKey(address);
        this.ads = new CompressedAddress(publicKeyToAddress());
    }

    public PubKey(String address)
    {
        this.key = addressToPublicKey(address);
        this.ads = new CompressedAddress(publicKeyToAddress());
    }

    public PubKey(PublicKey key)
    {
        this.key = key;
        this.ads = new CompressedAddress(publicKeyToAddress());
    }

    private PubKey()
    {
    }

    public static PubKey fromPublicWalletAddress(String address)
    {
        PubKey key = new PubKey();
        key.wad    = new PublicAddress(address);

        return key;
    }

    public boolean verifySignature(String data, String signature)
    {
        return verifySignature(data.getBytes(), signature);
    }

    public boolean verifySignature(byte data[], String signature)
    {
        if(signature == null) return false;
        if(signature.length() == 0) return false;
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(key);
            ecdsaVerify.update(data);
            return ecdsaVerify.verify(Base58.decode(signature));
        }catch(Exception e) {
            Logger.err("couldn't verify signature!");
            e.printStackTrace();
        }

        return false;
    }

    public byte[] sign(String data)
    {
        return sign(data.getBytes());
    }

    public byte[] sign(byte data[])
    {
        return null;
    }

    public Key getPublic()
    {
        return key;
    }

    private String publicKeyToAddress()
    {
        try{
            ECPublicKey key = (ECPublicKey) this.key;

            ECPoint point = key.getW();

            ByteBuffer pubKeyBuffer = ByteBuffer.allocate(point.getAffineX().toByteArray().length + point.getAffineY().toByteArray().length + 1);

            pubKeyBuffer.put((byte)point.getAffineX().toByteArray().length);
            pubKeyBuffer.put(point.getAffineX().toByteArray());
            pubKeyBuffer.put(point.getAffineY().toByteArray());
            pubKeyBuffer.flip();

            byte sha256[]   = HashUtil.applySha256(pubKeyBuffer.array());
            byte sha2562[]  = HashUtil.applySha256(sha256);
            byte ripeMD[]   = HashUtil.applyRipeMD160(sha2562);

            byte version    = Config.MAIN_NETWORK;
            byte key_21[]   = ByteUtil.concatenate(new byte[] {version}, ripeMD);

            byte checksum[] = ByteUtil.trim(HashUtil.applySha256(HashUtil.applySha256(key_21)), 0, 4);

            /** validation testing checks **/
//            System.out.println(ByteUtil.list(HashUtil.applySha256(HashUtil.applySha256(key_21))));
//            System.out.println(ByteUtil.list(checksum));

            wad = new PublicAddress(Base58.encode(ByteUtil.concatenate(key_21, checksum)));
            return Base58.encode(pubKeyBuffer.array());

        } catch (Exception e)
        {
            Logger.err("couldn't convert publickey to publicaddresskey");
            e.printStackTrace();
        }

        return "0";
    }

    private PublicKey addressToPublicKey(byte key[])
    {
        try{
            int xs = Byte.toUnsignedInt(key[0]);
            byte x[] = Arrays.copyOfRange(key, 1, xs + 1);
            byte y[] = Arrays.copyOfRange(key, xs + 1, (key.length - 1 - xs) + xs + 1);

            ECPoint point = new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ((ECPublicKey) Wallet.DEFAULT.getPublicKey().getPublic()).getParams());

            return Wallet.Factory.generatePublic(pubSpec);
        } catch (Exception e)
        {
        }

        return null;
    }

    private PublicKey addressToPublicKey(String key)
    {
        String realKey = key.substring(0);
        return addressToPublicKey(Base58.decode(key));
    }

    public CompressedAddress getCompressed()
    {
        return ads;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof PubKey) return key.equals(((PubKey) obj).key);
        return false;
    }

    public boolean isValid()
    {
        return key != null;
    }

    public String toString()
    {
        return this.wad.toString();
    }

    public static byte[] getBytes(String keyAddress)
    {
        return Base58.decode(keyAddress);
    }

    public PublicAddress getAddress()
    {
        return wad;
    }
}
