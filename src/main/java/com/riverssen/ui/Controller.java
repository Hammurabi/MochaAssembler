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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable
{
    @FXML
    ToggleButton my_wallet;
    @FXML
    ToggleButton transactions;
    @FXML
    ToggleButton settings;
    @FXML
    ToggleButton dark_theme;
    @FXML
    ToggleButton light_theme;

    @FXML
    TabPane tabpane;

    @FXML
    Pane main;

    @FXML
    ListView keypair_list;

    @FXML
    TableView<Transaction> txlist;

    public void selectMyWallet()
    {
        my_wallet.setSelected(true);
        transactions.setSelected(false);
        settings.setSelected(false);

        tabpane.getSelectionModel().select(0);

        keypair_list.getItems().add("bro");
    }

    public void selectTransactions()
    {
        my_wallet.setSelected(false);
        transactions.setSelected(true);
        settings.setSelected(false);

        tabpane.getSelectionModel().select(1);

        if(txlist.getColumns().size() == 0)
        {
            TableColumn<Transaction, Integer> type = new TableColumn<>("Type");
            type.setMinWidth(3);
            type.setCellValueFactory(new PropertyValueFactory<>("type"));

            TableColumn<Transaction, String> amount = new TableColumn<>("Amount");
            amount.setMinWidth(150);
            amount.setCellValueFactory(new PropertyValueFactory<>("amount"));

            TableColumn<Transaction, String> date = new TableColumn<>("Date");
            date.setMinWidth(150);
            date.setCellValueFactory(new PropertyValueFactory<>("date"));

            TableColumn<Transaction, String> recipient = new TableColumn<>("Recipient");
            recipient.setMinWidth(150);
            recipient.setCellValueFactory(new PropertyValueFactory<>("recipient"));

            TableColumn<Transaction, String> comment = new TableColumn<>("Comment");
            comment.setMinWidth(300);
            comment.setCellValueFactory(new PropertyValueFactory<>("comment"));

            TableColumn<Transaction, String> data = new TableColumn<>("Data");
            data.setMinWidth(200);
            data.setCellValueFactory(new PropertyValueFactory<>("data"));

            txlist.getColumns().addAll(type, amount, date, recipient, comment, data);
        }

        txlist.getItems().add(new Transaction(null, 0, new RiverCoin(Math.random() + ""), System.currentTimeMillis(), "jafjiwhfi12e1", "hello world", "hello world data"));
    }

    public void selectSettings()
    {
        my_wallet.setSelected(false);
        transactions.setSelected(false);
        settings.setSelected(true);

        tabpane.getSelectionModel().select(2);
    }

    public void selectDarkTheme()
    {
        main.getStylesheets().set(0, getClass().getClassLoader().getResource("style.css").toExternalForm());
        dark_theme.setSelected(true);
        light_theme.setSelected(false);
    }

    public void selectLightTheme()
    {
        main.getStylesheets().set(0, getClass().getClassLoader().getResource("light.css").toExternalForm());
        dark_theme.setSelected(false);
        light_theme.setSelected(true);
    }

    public void sendFunds(ContextI context, Wallet from, String to, String amt, String comment)
    {
        TXIList list = new TXIList();
        com.riverssen.core.transactions.Transaction trxn = new com.riverssen.core.transactions.Transaction(from.getPublicKey().getCompressed(), new PublicAddress(to), list, new
                RiverCoin(amt), comment);
        trxn.sign(from.getPrivateKey());
        context.getTransactionPool().addInternal(trxn);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}