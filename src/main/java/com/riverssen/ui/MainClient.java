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

package com.riverssen.ui;

import com.riverssen.core.RiverCoin;
import com.riverssen.core.headers.ContextI;
import com.riverssen.core.security.PublicAddress;
import com.riverssen.core.security.Wallet;
import com.riverssen.core.transactions.TXIList;
import com.riverssen.core.transactions.Transaction;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.LinkedHashSet;
import java.util.Set;

public class MainClient extends Application
{
    private Set<Wallet> wallets = new LinkedHashSet<>();
    private ContextI    context;

    public static void main(String ...args)
    {
        launch(args);
    }

    public MainClient()
    {
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("wallet.fxml"));
        primaryStage.setTitle("Rivercoin wallet");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public void sendFunds(Wallet from, String to, String amt, String comment)
    {
        TXIList list = new TXIList();
        Transaction trxn = new Transaction(from.getPublicKey().getCompressed(), new PublicAddress(to), list, new
                RiverCoin(amt), comment);
        trxn.sign(from.getPrivateKey());
        context.getTransactionPool().addInternal(trxn);
    }
}