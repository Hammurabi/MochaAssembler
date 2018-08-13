package com.riverssen.testing;

import com.riverssen.core.security.PublicAddress;
import com.riverssen.core.security.Wallet;

public class PublicAddressValidation {
    public static void test()
    {
        Wallet wallet = new Wallet("1d231", "1sd23123");

        System.out.println(wallet.getPublicKey().getAddress());

        System.out.println(PublicAddress.isPublicAddressValid(wallet.getPublicKey().getAddress().toString()));
        System.out.println(PublicAddress.isPublicAddressValid("2SCKkx6wgJHfPuMfbwpoyCc3KJMr"));
    }
}
