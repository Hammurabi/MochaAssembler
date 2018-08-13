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

package com.riverssen.core.networking;

import com.riverssen.core.headers.ContextI;
import com.riverssen.core.networking.messages.BasicMessage;
import com.riverssen.core.networking.messages.GoodByeMessage;
import com.riverssen.core.system.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class Client implements Runnable {
    private volatile SocketConnection connection;
    private volatile ContextI context;

    private volatile Map<String, BasicMessage> cache;
    private volatile Set<BasicMessage> toSend;
    private volatile String lock;
    private volatile AtomicBoolean relay;
    private volatile AtomicLong version;
    private volatile AtomicLong chainSize;
    private volatile AtomicBoolean greeted;
    private volatile AtomicBoolean blocked;
    private volatile ReentrantLock threadlock;

    public Client(SocketConnection connection, ContextI context) {
        this.connection = connection;
        this.context = context;
        this.cache = Collections.synchronizedMap(new LinkedHashMap<>());
        this.toSend = Collections.synchronizedSet(new LinkedHashSet<>());
        this.version = new AtomicLong(0);
        this.chainSize = new AtomicLong(0);
        this.greeted = new AtomicBoolean(false);
        this.relay = new AtomicBoolean(false);
        this.blocked = new AtomicBoolean(false);
        threadlock = new ReentrantLock();
    }

    public final void sendMessage(BasicMessage message) {
        this.sendMessage(message, "");
    }

    public final void sendMessage(BasicMessage message, String key) {
        toSend.add(message);
//        threadlock.lock();
//        try {
//            if (keyMatch(key))
//                forceSendMessage(message);
//            else toSend.add(message);
//        } finally {
//            threadlock.unlock();
//        }
    }

    private void forceSendMessage(BasicMessage message) {
        if (!cache.containsKey(message.getHashCode()))
            cache.put(message.getHashCode(), message);

        try {
//            Logger.alert("--------MSGS---------");
//            Logger.alert(message + "");

            message.sendMessage(connection, context);
            connection.getOutputStream().flush();
            message.send();
        } catch (IOException e) {
            message.send();
        }

        if (message.stopAttemptingToSend())
            cache.remove(message.getHashCode());
    }

    public void update() throws IOException {
        while (connection.getInputStream().available() > 0) {
            BasicMessage message = safeNext();

//            Logger.alert("--------MSGR---------");
//            Logger.alert(message + "");

            if (message != null)
                message.onReceive(this, connection, context);
        }

        Set<BasicMessage> messages = new LinkedHashSet<>();
        synchronized (toSend)
        {
            messages.addAll(toSend);
        }

        for (BasicMessage message : messages)
            forceSendMessage(message);

        Set<String> toRemove = new HashSet<>();

        for (String message : cache.keySet())
            if (cache.get(message).stopAttemptingToSend())
                toRemove.add(message);

        for (String message : toRemove)
            cache.remove(message);

        toSend.clear();
    }

    public final boolean keyMatch(String key) {
        threadlock.lock();
        try {
            if (lock == null) return true;

            return lock.equals(key);
        } finally {
            threadlock.unlock();
        }
    }

    public boolean lock(String key) {
        threadlock.lock();
        try {
            if (lock != null)
                return false;

            lock = key;

            return true;
        } finally {
            threadlock.unlock();
        }
    }

    public boolean unlock(String key) {
        if (lock == null) return true;

        if (lock.equals(key))
            lock = null;

        return lock == null;
    }

    public boolean isLocked() {
        threadlock.lock();
        try {
            return lock != null;
        } finally {
            threadlock.unlock();
        }
    }

    public boolean isRelay() {
        threadlock.lock();
        try {
            return relay.get();
        } finally {
            threadlock.unlock();
        }
    }

    public void setVersion(long l) {
        threadlock.lock();
        try {
            this.version.set(l);
        } finally {
            threadlock.unlock();
        }
    }

    public void setChainSize(long l) {
        threadlock.lock();
        try {
            this.chainSize.set(l);
        } finally {
            threadlock.unlock();
        }
    }

    public void setIsRelay(boolean r) {
        threadlock.lock();
        try {
            this.relay.set(r);
        } finally {
            threadlock.unlock();
        }
    }

    public void setGreeted(boolean g) {
        threadlock.lock();
        try {
            this.greeted.set(g);
        } finally {
            threadlock.unlock();
        }
    }

    public boolean isGreeted() {
        threadlock.lock();
        try {
            return greeted.get();
        } finally {
            threadlock.unlock();
        }
    }

    public void removeMessage(String s) {
        threadlock.lock();
        try {
            cache.remove(s);
        } finally {
            threadlock.unlock();
        }
    }

    public void resend(String s) {
        threadlock.lock();
        try {
            if (cache.containsKey(s))
                sendMessage(cache.get(s));
        } finally {
            threadlock.unlock();
        }
    }

    public long getChainSize() {
        threadlock.lock();
        try {
            return chainSize.get();
        } finally {
            threadlock.unlock();
        }
    }

    @Override
    public void run() {
        while (context.isRunning() && connection.isConnected()) {
            try {
                update();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(12L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeConnection() throws IOException {
        threadlock.lock();
        try {
            sendMessage(new GoodByeMessage());
            connection.closeConnection();
        } finally {
            threadlock.unlock();
        }
    }

    private BasicMessage safeNext() throws IOException {
        int type = connection.getInputStream().readInt();

        return BasicMessage.decipher(type, this);
    }

    public BasicMessage next() throws IOException {
        threadlock.lock();
        try {
            long now = System.currentTimeMillis();
            while (connection.getInputStream().available() == 0) {
                if (System.currentTimeMillis() - now >= 230_000L) return null;
            }

            return safeNext();
        } finally {
            threadlock.unlock();
        }
    }

    public void block() {
        threadlock.lock();
        try {
            this.blocked.set(true);
        } finally {
            threadlock.unlock();
        }
    }

    public void unblock() {
        threadlock.lock();
        try {
            this.blocked.set(false);
        } finally {
            threadlock.unlock();
        }
    }

    public boolean isBlocked() {
        threadlock.lock();
        try {
            return blocked.get();
        } finally {
            threadlock.unlock();
        }
    }

    public BasicMessage getReply(String digest) {
        threadlock.lock();
        try {
            return null;
        } finally {
            threadlock.unlock();
        }
    }

    @Override
    public String toString() {
        threadlock.lock();
        try {
            return "client{ip: " + connection.toString() + " relay: " + relay + " chain: " + chainSize + "}";
        } finally {
            threadlock.unlock();
        }
    }
}
