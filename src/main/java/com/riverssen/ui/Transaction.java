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
import com.riverssen.core.security.PublicAddress;
import com.riverssen.core.security.Wallet;

import java.text.SimpleDateFormat;

public class Transaction
{
    private int  type;
    private String amount;
    private long dateTime;
    private String recipient;
    private String comment;
    private String data;

    public Transaction(Wallet wallet, int type, RiverCoin amount, long dateTime, String recipient, String comment, String data)
    {
        this.type = type;
        this.amount = amount.toRiverCoinString();
        this.dateTime = dateTime;
        this.recipient = recipient;
        this.comment = comment;
        this.data = data;

        if(wallet == null) return;

        if(wallet.containsAddress(new PublicAddress(recipient)))
            this.recipient = "me: " + wallet.getKeyPairFromAddress(new PublicAddress(recipient));
    }

    public int getType() {
        return type;
    }

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return new SimpleDateFormat("EEE dd MM yy HH:mm:ss").format(dateTime);
    }

    public String getRecipient() {
        return recipient;
    }

    public String getComment() {
        return comment;
    }

    public String getData() {
        return data;
    }
}