/**
 * Copyright 2018 Ragnarr Ivarssen
 * This Software Is Free For Use And Must
 *
 * Not Be Sold And Or Distributed Without The Written Permission Of
 * (Ragnarr Ivarssen Riverssen@gmail.com).
 *
 * The Software's Code Must Not Be Made Public, The Software Must Not Be Decompiled, Reverse Engineered, Or Unobfuscated In Any Way
 * Without The Written Permission Of (Ragnarr Ivarssen Riverssen@gmail.com).
 *
 * The Creator (Ragnarr Ivarssen Riverssen@Gmail.com) Does Not Provide Any Warranties
 * To The Quality Of The Software And It Is Provided "As Is".
 */

package com.riverssen.riverssen;

public final class UTXO
{
    private final byte hash[];
    private final byte valu[];

    public UTXO(byte hash[], byte value[])
    {
        this.hash = hash;
        this.valu = value;
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getValue() {
        return valu;
    }
}