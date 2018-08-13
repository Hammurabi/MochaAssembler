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

package com.riverssen.core.networking.messages;

import com.riverssen.core.headers.ContextI;
import com.riverssen.core.networking.Client;
import com.riverssen.core.networking.SocketConnection;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BasicMessage
{
    public static final int OP_GREET    = 0;
    public static final int OP_HALT     = 1;
    public static final int OP_TXN      = 2;
    public static final int OP_BLK      = 3;
    public static final int OP_BKH      = 4;
    public static final int OP_PRS      = 5;
    public static final int OP_GREET1   = 6;
    public static final int OP_FAILED   = 7;
    public static final int OP_SUCCESS  = 8;

    public static final int OP_REQUEST  = 9;
    public static final int OP_BKI      = 10;
    public static final int OP_FAILED0  = 11;
    public static final int OP_ALL      = 12;
    public static final int OP_NODE     = 13;
    public static final int OP_OTHER    = 14;
    public static final int OP_MSM      = 15;
    public static final int OP_BLH      = 16;
    public static final int OP_BLR      = 17;


    public static final int OP_MSG      = 2;


    private String hashCode;
    private final AtomicInteger timesSent;
    private final AtomicLong timeStamp;

    public BasicMessage(String hashCode)
    {
        this.hashCode = hashCode;
        this.timesSent= new AtomicInteger(0);
        this.timeStamp= new AtomicLong(System.currentTimeMillis());
    }

    public void send()
    {
        timesSent.set(timesSent.intValue() + 1);
    }
    public abstract void sendMessage(SocketConnection connection, ContextI context) throws IOException;
    public abstract void onReceive(Client client, SocketConnection connection, ContextI context) throws IOException;

    public static synchronized BasicMessage decipher(int type, Client client)
    {
        switch (type)
        {
            case OP_GREET: return new GreetMessage();
            case OP_GREET1:
                //prevent spam
                if(!client.isGreeted()) return new GreetReplyMessage(); return null;
            case OP_BKH: return new BlockHashMessage();
            case OP_BLH: return new RequestBlockHashMessage();
            case OP_BLR: return new RequestBlockMessage();
            case OP_BLK: return new BlockMessage();
            case OP_HALT: return new GoodByeMessage();
            case OP_FAILED: return new FailedMessage();
            case OP_SUCCESS: return new SuccessMessage();
            case OP_TXN: return new TransactionMessage();
                default:
                    return null;
        }
    }

    public synchronized String getHashCode()
    {
        return hashCode;
    }

    public synchronized void setTimesSent(int numTimesSent)
    {
        this.timesSent.set(numTimesSent);
    }

    public boolean stopAttemptingToSend()
    {
        return (timesSent.intValue() > 10 || timesSent.intValue() < 0) || System.currentTimeMillis() - timeStamp.longValue() >= 180_000L;
    }
}