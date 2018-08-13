package com.riverssen.testing;

import com.riverssen.core.headers.ContextI;
import com.riverssen.core.security.AdvancedEncryptionStandard;
import com.riverssen.core.system.Logger;
import com.riverssen.core.utils.Base58;
import com.riverssen.core.utils.ByteUtil;

import java.io.*;

public class Wallet {

    public static com.riverssen.core.security.Wallet generate(ContextI context, String...args)
    {
        if (args == null)
        {
            Logger.err("must launch with 4 arguments: <name> <seed> <encryption password> <num addresses>");
        }

        if(args.length != 4)
        {
            Logger.err("must launch with 4 arguments: <name> <seed> <encryption password> <num addresses>");
        }

        String name = args[0];
        String seed = args[1];
        String pass = args[2];
        String numa = args[3];

        while (pass.length() < 16)
            pass = pass += '0';

        pass = pass.substring(0, 16);

        com.riverssen.core.security.Wallet wallet = new com.riverssen.core.security.Wallet(name, seed);

        for(int i = 0; i < Integer.parseInt(numa); i ++)
            wallet.generateNewKeyPair("default");

        wallet.export(pass, context);

        return wallet;
    }

    public static void launchTests(ContextI context)
    {
        com.riverssen.core.security.Wallet wallet = new com.riverssen.core.security.Wallet("burger-test", "burger-test");
        wallet.generateNewKeyPair("broski");
        wallet.generateNewKeyPair("broski");
        wallet.generateNewKeyPair("broski");
        wallet.generateNewKeyPair("broski");
        wallet.generateNewKeyPair("broski");
        wallet.generateNewKeyPair("broski");
        wallet.generateNewKeyPair("broski");
        wallet.generateNewKeyPair("broski");
        wallet.generateNewKeyPair("broski");
        wallet.generateNewKeyPair("broski");
        wallet.generateNewKeyPair("broski");

        wallet.export("broski password ", context);

        com.riverssen.testing.Wallet.WalletReadTest("burger-test", "broski password ", context);
    }

    public static void WalletReadTest(String name, String password, ContextI context)
    {
        AdvancedEncryptionStandard advancedEncryptionStandard = new AdvancedEncryptionStandard(password.getBytes());

        File diry = new File(context.getConfig().getBlockChainWalletDirectory() + name + "//");
        diry.mkdirs();

        File file = new File(context.getConfig().getBlockChainWalletDirectory() + name + "//" + name + ".rwt");
        File pub = new File(context.getConfig().getBlockChainWalletDirectory() + name + "//readme.txt");

        try {
            DataInputStream stream = new DataInputStream(new FileInputStream(file));

            int size = stream.readInt();

            for(int i = 0; i < size; i ++)
                System.out.println("name: " + stream.readUTF() + " private: " + Base58.encode(advancedEncryptionStandard.decrypt(ByteUtil.read(stream, stream.readInt()))) + " public: " + Base58.encode(ByteUtil.read(stream, stream.readInt())));

            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void test()
    {
        String nameOfWallet = "name"; //unimportant
        String seedOfWallet = "seed"; //extremely important and must be secure + unique.
        com.riverssen.core.security.Wallet
                    wallet  = new com.riverssen.core.security.Wallet(nameOfWallet, seedOfWallet);

        //Name Of Keypair Is Not Important To Security.
        wallet.generateNewKeyPair("test pair a");
        wallet.generateNewKeyPair("test pair b");
        wallet.generateNewKeyPair("test pair c");
        wallet.generateNewKeyPair("test pair d");
        wallet.generateNewKeyPair("test pair e");
        wallet.generateNewKeyPair("test pair f");
        wallet.generateNewKeyPair("test pair g");
        wallet.generateNewKeyPair("test pair h");
        wallet.generateNewKeyPair("test pair i");
        wallet.generateNewKeyPair("test pair j");
        wallet.generateNewKeyPair("test pair k");
        wallet.generateNewKeyPair("test pair l");
        wallet.generateNewKeyPair("test pair m");
        wallet.generateNewKeyPair("test pair n");

        System.out.println(wallet.getPublicAddress(0));
        System.out.println(wallet.getPublicAddress(1));
        System.out.println(wallet.getPublicAddress(2));
        System.out.println(wallet.getPublicAddress(3));
        System.out.println(wallet.getPublicAddress(4));
        System.out.println(wallet.getPublicAddress(5));
        System.out.println(wallet.getPublicAddress(6));
        System.out.println(wallet.getPublicAddress(7));
        System.out.println(wallet.getPublicAddress(8));
        System.out.println(wallet.getPublicAddress(9));
        System.out.println(wallet.getPublicAddress(10));
        System.out.println(wallet.getPublicAddress(11));
        System.out.println(wallet.getPublicAddress(12));
        System.out.println(wallet.getPublicAddress(13));
        System.out.println(wallet.getPublicAddress(14));

        //Going out of lists bounds will overflow the index and go back to 0
        System.out.println("overflow address: " + wallet.getPublicAddress(15));

        System.exit(0);
    }
}
