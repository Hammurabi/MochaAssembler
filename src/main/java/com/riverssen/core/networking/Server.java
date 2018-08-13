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

import com.riverssen.core.block.BlockDownloadService;
import com.riverssen.core.block.FullBlock;
import com.riverssen.core.headers.ContextI;
import com.riverssen.core.headers.TransactionI;
import com.riverssen.core.networking.messages.*;
import com.riverssen.core.system.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sun.rmi.runtime.Log;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.*;

public class Server implements NetworkManager
{
    public static final String  seedNodeUrl = "http://www.rivercoin.net/developers/api/seednodes.php";

    private volatile Set<String>         ipAddresses;
    private volatile Set<Client>         communications;
    private volatile ContextI            context;
    private volatile ServerSocket        socket;

    public Server(ContextI context) throws Exception
    {
        this.ipAddresses    = new LinkedHashSet<>();
        this.communications = Collections.synchronizedSet(new LinkedHashSet<>());
        this.context        = context;

        this.socket         = new ServerSocket();
        this.socket.bind(new InetSocketAddress("0.0.0.0", context.getConfig().getPort()));
    }

    public void establishConnection() throws Exception
    {
        addSavedIps();
        addSeedIPs();
        createListener();
        ipAddresses.add("77.22.250.86");

        if(ipAddresses.size() == 0)
            throw new Exception("no seed ip address found.");

        establishConnections();
    }

    private void createListener()
    {
        context.getExecutorService().execute(()->{
            while(context.isRunning())
            {
                try{
                    SocketConnection connection = new SocketConnection(socket);
                    Client client = new Client(connection, context);
                    context.getExecutorService().execute(client);
                    communications.add(client);

                    Thread.sleep(12L);
                } catch (Exception e)
                {
                }
            }
        });
    }

    @Override
    public void broadCastNewTransaction(TransactionI transaction)
    {
        for (Client communicator : communications) communicator.sendMessage(new TransactionMessage(transaction));
    }

    @Override
    public Set<Client> getCommunicators()
    {
        return communications;
    }

    @Override
    public int amountNodesConnected()
    {
        int amt = 0;

        for(Client communicator : communications)
            if(communicator.isRelay()) amt ++;
        return amt;
    }

    @Override
    public void downloadLongestChain()
    {
        List<Client> nodes = new ArrayList<>();

        for(Client communicator : communications)
            if(communicator.isRelay()) nodes.add(communicator);

        //Descending Ordered List
        nodes.sort((a, b)->{ if(a.getChainSize() == b.getChainSize()) return 0;
                                else if(a.getChainSize() > b.getChainSize()) return -1;
                                else return 1;
        });

        for(Client node : nodes)
            for(long i = context.getBlockChain().currentBlock() - 1; i < node.getChainSize(); i ++)
                node.sendMessage(new RequestBlockMessage(i));
    }

    @Override
    public void sendBlock(BlockDownloadService block)
    {
        for(Client communicator : communications)
            synchronized (communicator)
            {
                communicator.sendMessage(new BlockMessage(block, false));
            }
    }

    @Override
    public void sendMessage(GoodByeMessage message) {
        for (Client client : communications)
            client.sendMessage(message);
    }

    @Override
    public void sendBlock(BlockDownloadService block, Client... client) {
        Set<Client> communicators = new LinkedHashSet<>(this.communications);
        for(Client client1 : client)
            communicators.remove(client1);

        for(Client client_0 : communicators)
            client_0.sendMessage(new BlockMessage(block, false));
    }

    private void establishConnections()
    {
        for(String ipAddress : ipAddresses)
            connectToIp(ipAddress);
    }

    private void connectToIp(String ip)
    {
        try{
            Logger.alert("Attempting to connect to '"+ip+"'");
            SocketConnection connection = new SocketConnection(ip, context.getConfig().getPort());
            if(connection.isConnected())
            {
                Logger.alert("'" + ip + "' connected.");
                Client client = new Client(connection, context);
                client.sendMessage(new GreetMessage());

                context.getExecutorService().execute(client);
                communications.add(client);
            }
        } catch (Exception e)
        {
            Logger.err("failed to connect to '" + ip + "'.");
            Logger.err(e.getMessage());
        }
    }

    private void addSavedIps()
    {
        ipAddresses.addAll(getList());
    }

    private void addSeedIPs() throws Exception
    {
        Connection connection = Jsoup.connect(seedNodeUrl);
        Document doc = connection.get();
        String text = doc.body().text();

        String ips[] = text.split(" ");

        for(String string : ips)
            ipAddresses.add(string);
    }

    public Set<String> getList()
    {
        Set<String> ips = new HashSet<>();

        try {
            File file = new File(context.getConfig().getBlockChainDirectory() + File.separator + "network.info");
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line = "";

            while((line = reader.readLine()) != null) ips.add(line);
        } catch (Exception e)
        {
        }

        return ips;
    }

    public void terminate()
    {
        for(Client peer : communications) try{peer.closeConnection();} catch (Exception e) {}

        String ipAddresses = "";

        this.ipAddresses.addAll(getList());

        for(String peer : this.ipAddresses) ipAddresses += peer + "\n";

        try {
            File file = new File(context.getConfig().getBlockChainDirectory() + File.separator + "network.info");

            FileWriter writer = new FileWriter(file);

            writer.write(ipAddresses);

            writer.flush();

            writer.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
