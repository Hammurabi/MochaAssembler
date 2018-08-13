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

package com.riverssen.core.utils;

import com.riverssen.core.system.Logger;
import com.riverssen.core.security.PubKey;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

public class HashUtil
{
//     --- Provider SUN, version 1.80 ---
//    Algorithm name: "MD2"
//    Algorithm name: "MD5"
//    Algorithm name: "SHA"
//    Algorithm name: "SHA-224"
//    Algorithm name: "SHA-256"
//    Algorithm name: "SHA-384"
//    Algorithm name: "SHA-512"
//    Alias: "SHA-1" -> "SHA"
//    Alias: "OID.1.3.14.3.2.26" -> "SHA"
//    Alias: "1.3.14.3.2.26" -> "SHA"
//    Alias: "OID.2.16.840.1.101.3.4.2.4" -> "SHA-224"
//    Alias: "OID.2.16.840.1.101.3.4.2.3" -> "SHA-512"
//    Alias: "OID.2.16.840.1.101.3.4.2.2" -> "SHA-384"
//    Alias: "OID.2.16.840.1.101.3.4.2.1" -> "SHA-256"
//    Alias: "2.16.840.1.101.3.4.2.4" -> "SHA-224"
//    Alias: "2.16.840.1.101.3.4.2.3" -> "SHA-512"
//    Alias: "2.16.840.1.101.3.4.2.2" -> "SHA-384"
//    Alias: "2.16.840.1.101.3.4.2.1" -> "SHA-256"
//    Alias: "SHA1" -> "SHA"
//
//        --- Provider BC, version 1.59 ---
//    Algorithm name: "GOST3411"
//    Algorithm name: "GOST3411-2012-256"
//    Algorithm name: "GOST3411-2012-512"
//    Algorithm name: "KECCAK-224"
//    Algorithm name: "KECCAK-288"
//    Algorithm name: "KECCAK-256"
//    Algorithm name: "KECCAK-384"
//    Algorithm name: "KECCAK-512"
//    Algorithm name: "MD2"
//    Algorithm name: "MD4"
//    Algorithm name: "MD5"
//    Algorithm name: "SHA-1"
//    Algorithm name: "RIPEMD128"
//    Algorithm name: "RIPEMD160"
//    Algorithm name: "RIPEMD256"
//    Algorithm name: "RIPEMD320"
//    Algorithm name: "SHA-224"
//    Algorithm name: "SHA-256"
//    Algorithm name: "SHA-384"
//    Algorithm name: "SHA-512"
//    Algorithm name: "SHA-512/224"
//    Algorithm name: "SHA-512/256"
//    Algorithm name: "SHA3-224"
//    Algorithm name: "SHA3-256"
//    Algorithm name: "SHA3-384"
//    Algorithm name: "SHA3-512"
//    Algorithm name: "2.16.840.1.101.3.4.2.7"
//    Algorithm name: "OID.2.16.840.1.101.3.4.2.7"
//    Algorithm name: "2.16.840.1.101.3.4.2.8"
//    Algorithm name: "OID.2.16.840.1.101.3.4.2.8"
//    Algorithm name: "2.16.840.1.101.3.4.2.9"
//    Algorithm name: "OID.2.16.840.1.101.3.4.2.9"
//    Algorithm name: "2.16.840.1.101.3.4.2.10"
//    Algorithm name: "OID.2.16.840.1.101.3.4.2.10"
//    Algorithm name: "Skein-256-128"
//    Algorithm name: "Skein-256-160"
//    Algorithm name: "Skein-256-224"
//    Algorithm name: "Skein-256-256"
//    Algorithm name: "Skein-512-128"
//    Algorithm name: "Skein-512-160"
//    Algorithm name: "Skein-512-224"
//    Algorithm name: "Skein-512-256"
//    Algorithm name: "Skein-512-384"
//    Algorithm name: "Skein-512-512"
//    Algorithm name: "Skein-1024-384"
//    Algorithm name: "Skein-1024-512"
//    Algorithm name: "Skein-1024-1024"
//    Algorithm name: "SM3"
//    Algorithm name: "TIGER"
//    Algorithm name: "WHIRLPOOL"
//    Algorithm name: "BLAKE2B-512"
//    Algorithm name: "BLAKE2B-384"
//    Algorithm name: "BLAKE2B-256"
//    Algorithm name: "BLAKE2B-160"
//    Algorithm name: "BLAKE2S-256"
//    Algorithm name: "BLAKE2S-224"
//    Algorithm name: "BLAKE2S-160"
//    Algorithm name: "BLAKE2S-128"
//    Algorithm name: "DSTU7564-256"
//    Algorithm name: "DSTU7564-384"
//    Algorithm name: "DSTU7564-512"
//    Algorithm name: "1.2.804.2.1.1.1.1.2.2.1"
//    Algorithm name: "OID.1.2.804.2.1.1.1.1.2.2.1"
//    Algorithm name: "1.2.804.2.1.1.1.1.2.2.2"
//    Algorithm name: "OID.1.2.804.2.1.1.1.1.2.2.2"
//    Algorithm name: "1.2.804.2.1.1.1.1.2.2.3"
//    Algorithm name: "OID.1.2.804.2.1.1.1.1.2.2.3"
//    Alias: "SHA256" -> "SHA-256"
//    Alias: "SHA224" -> "SHA-224"
//    Alias: "1.3.36.3.2.3" -> "RIPEMD256"
//    Alias: "1.3.36.3.2.2" -> "RIPEMD128"
//    Alias: "1.3.36.3.2.1" -> "RIPEMD160"
//    Alias: "1.3.14.3.2.26" -> "SHA-1"
//    Alias: "GOST-3411-2012-256" -> "GOST3411-2012-256"
//    Alias: "SHA512/224" -> "SHA-512/224"
//    Alias: "2.16.840.1.101.3.4.2.6" -> "SHA-512/256"
//    Alias: "2.16.840.1.101.3.4.2.5" -> "SHA-512/224"
//    Alias: "2.16.840.1.101.3.4.2.4" -> "SHA-224"
//    Alias: "2.16.840.1.101.3.4.2.3" -> "SHA-512"
//    Alias: "2.16.840.1.101.3.4.2.2" -> "SHA-384"
//    Alias: "2.16.840.1.101.3.4.2.1" -> "SHA-256"
//    Alias: "SM3" -> "SM3"
//    Alias: "SHA" -> "SHA-1"
//    Alias: "1.3.6.1.4.1.1722.12.2.2.8" -> "BLAKE2S-256"
//    Alias: "1.3.6.1.4.1.1722.12.2.2.7" -> "BLAKE2S-224"
//    Alias: "1.3.6.1.4.1.1722.12.2.2.5" -> "BLAKE2S-160"
//    Alias: "1.3.6.1.4.1.1722.12.2.2.4" -> "BLAKE2S-128"
//    Alias: "GOST-2012-256" -> "GOST3411-2012-256"
//    Alias: "SHA1" -> "SHA-1"
//    Alias: "GOST" -> "GOST3411"
//    Alias: "1.3.6.1.4.1.1722.12.2.1.16" -> "BLAKE2B-512"
//    Alias: "1.3.6.1.4.1.1722.12.2.1.12" -> "BLAKE2B-384"
//    Alias: "1.3.6.1.4.1.1722.12.2.1.8" -> "BLAKE2B-256"
//    Alias: "1.3.6.1.4.1.1722.12.2.1.5" -> "BLAKE2B-160"
//    Alias: "SHA512" -> "SHA-512"
//    Alias: "1.2.643.7.1.1.2.3" -> "GOST3411-2012-512"
//    Alias: "1.2.643.7.1.1.2.2" -> "GOST3411-2012-256"
//    Alias: "GOST-3411" -> "GOST3411"
//    Alias: "SHA512256" -> "SHA-512/256"
//    Alias: "1.2.840.113549.2.5" -> "MD5"
//    Alias: "1.2.840.113549.2.4" -> "MD4"
//    Alias: "1.2.840.113549.2.2" -> "MD2"
//    Alias: "GOST-3411-2012-512" -> "GOST3411-2012-512"
//    Alias: "SHA384" -> "SHA-384"
//    Alias: "1.2.156.197.1.401" -> "SM3"
//    Alias: "1.2.643.2.2.9" -> "GOST3411"
//    Alias: "GOST-2012-512" -> "GOST3411-2012-512"

    public static String compressPublicKey(PubKey key)
    {
        ECPublicKey key1 = (ECPublicKey) key.getPublic();

        ECPoint w = key1.getW();

        return null;
    }

    public static byte[] base64Encode(byte data[])
    {
        return Base64.getUrlEncoder().withoutPadding().encode(data);
    }

    public static byte[] base64StringDecode(String data)
    {
        return Base64.getUrlDecoder().decode(data);
    }

    public static String base64Encode(String string)
    {
        return new String(base64Encode(string.getBytes()));
    }

    public static String base64StringEncode(byte data[])
    {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    public static String base36Encode(byte data[])
    {
        return new BigInteger(data).toString(36);
    }

    public static byte[] base36Decode(String data)
    {
        return new BigInteger(data, 36).toByteArray();
    }

    public static String createDifficultyString()
    {
        return new String(new byte[1000]).replaceAll("\0", "0");
    }

    public static byte[] applySha256(byte[] input)
    {
        return hashWith(input, "SHA-256");
    }

    public static byte[] applyBlake2b(byte[] input)
    {
        return hashWith(input, "BLAKE2B-256");
    }

    public static byte[] applySha3(byte[] input)
    {
        return hashWith(input, "SHA3-256");
    }

    public static byte[] applySha512(byte[] input)
    {
        return hashWith(input, "SHA-512");
    }

    public static byte[] applyKeccak(byte[] input)
    {
        return hashWith(input, "KECCAK-256");
    }

    public static byte[] applyKeccak(byte[] input, int number)
    {
        return hashWith(input, "KECCAK-" + number);
    }

    public static byte[] applyRipeMD128(byte[] input) { return hashWith(input, "RIPEMD128"); }
    public static byte[] applyRipeMD160(byte[] input) { return hashWith(input, "RIPEMD160"); }
    public static byte[] applyRipeMD256(byte[] input) { return hashWith(input, "RIPEMD256"); }
    public static byte[] applyGost3411(byte[] input) { return hashWith(input, "GOST3411"); }

    public static byte[] applySkein(byte[] input, int numa, int numb)
    {
        return hashWith(input, "Skein-" + numa + "-" + numb);
    }

    public static byte[] hashWith(byte[] input, String instanceName)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance(instanceName, "BC");
            byte[] hash = digest.digest(input);
            return hash;
        } catch (Exception e)
        {
            System.out.println(Logger.COLOUR_RED + e.getMessage());
            return null;
        }
    }

    public static String hashToStringBase16(byte[] hash)
    {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++)
        {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static String publicKeyExport(String key)
    {
        while(key.length() < 85) key += "\0" + key;

        return key;
    }

    public static String publicKeyImport(String key)
    {
        String k = "";

        for(int i = 0; i < key.length(); i ++)
            if(key.charAt(i) != '\0') k += key.charAt(i);

        return k;
    }

    public static String blockHashExport(String hash)
    {
        while(hash.length() < 256)
            hash = "\0" + hash;

        return hash;
    }

    public static String blockHashImport(String hash)
    {
        String k = "";

        for(int i = 0; i < hash.length(); i ++)
            if(hash.charAt(i) != '\0') k += hash.charAt(i);

        return k;
    }

    public static byte[] applySha1(byte[] bytes)
    {
        return hashWith(bytes, "SHA1-256");
    }
}
