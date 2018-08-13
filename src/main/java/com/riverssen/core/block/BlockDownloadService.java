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

package com.riverssen.core.block;

import java.io.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class BlockDownloadService implements Comparable<BlockDownloadService>
{
    private volatile long blockID;
    private volatile byte data[];

    public BlockDownloadService(DataInputStream stream) throws IOException {
        this.blockID    = stream.readLong();
        int size        = stream.readInt();
        this.data       = new byte[size];
        stream.read(data);
    }

    public BlockDownloadService(FullBlock block) throws IOException {
        ByteArrayOutputStream stream1   = new ByteArrayOutputStream();
        DataOutputStream      stream    = new DataOutputStream(stream1);

        byte data[] = getData(block);

        stream.writeLong(block.getBlockID());
        stream.writeInt(data.length);
        stream.write(data);

        stream.flush();
        stream.close();

        this.data = stream1.toByteArray();
    }

    private byte[] getData(FullBlock block) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream      blockStream = new DataOutputStream(new DeflaterOutputStream(stream));
        block.export(blockStream);
        blockStream.flush();
        blockStream.close();

        blockStream.flush();
        blockStream.close();

        return stream.toByteArray();
    }

    public DataInputStream decompressedInputStream()
    {
        return new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(data)));
    }

    public byte[] getData() {
        return data;
    }

    public long getBlockID() {
        return blockID;
    }

    @Override
    public int compareTo(BlockDownloadService o) {
        if(getBlockID() > o.getBlockID()) return 1;
        else if(getBlockID() == o.getBlockID()) return 0;
        return -1;
    }

    @Override
    public String toString() {
        return "downloadservice{"+blockID + " " + data +"}";
    }
}
