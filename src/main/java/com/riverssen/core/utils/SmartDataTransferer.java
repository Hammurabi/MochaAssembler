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

package com.riverssen.core.utils;

import java.io.*;

public class SmartDataTransferer
{
    private interface DataTransferStream{
        void        write(byte data[]) throws IOException;
        void        read(byte data[]) throws IOException;
        void        writeInt(int i) throws IOException;
        void        write(int b) throws IOException;
        void        writeShort(short s) throws IOException;
        void        writeChar(char c) throws IOException;
        void        writeLong(long l) throws IOException;
        void        writeFloat(float f) throws IOException;
        void        writeDouble(double d) throws IOException;
        void        writeUTF(String string) throws IOException;

        int         read() throws IOException;
        int         readInt() throws IOException;
        short       readShort() throws IOException;
        char        readChar() throws IOException;
        float       readFloat() throws IOException;
        long        readLong() throws IOException;
        double      readDouble() throws IOException;

        long        length();
    }

    private class DataTransferInputStream implements DataTransferStream
    {
        private DataInputStream stream;

        DataTransferInputStream(InputStream stream)
        {
            this.stream = new DataInputStream(stream);
        }

        @Override
        public void write(byte[] data) throws IOException {

        }

        @Override
        public void read(byte[] data) throws IOException {
            stream.read(data);
        }

        @Override
        public void writeInt(int i) throws IOException {

        }

        @Override
        public void write(int b) throws IOException {

        }

        @Override
        public void writeShort(short s) throws IOException {

        }

        @Override
        public void writeChar(char c) throws IOException {

        }

        @Override
        public void writeLong(long l) throws IOException {

        }

        @Override
        public void writeFloat(float f) throws IOException {

        }

        @Override
        public void writeDouble(double d) throws IOException {

        }

        @Override
        public void writeUTF(String string) throws IOException {

        }

        @Override
        public int read() throws IOException {
            return stream.read();
        }

        @Override
        public int readInt() throws IOException {
            return stream.readInt();
        }

        @Override
        public short readShort() throws IOException {
            return stream.readShort();
        }

        @Override
        public char readChar() throws IOException {
            return stream.readChar();
        }

        @Override
        public float readFloat() throws IOException {
            return stream.readFloat();
        }

        @Override
        public long readLong() throws IOException {
            return stream.readLong();
        }

        @Override
        public double readDouble() throws IOException {
            return stream.readDouble();
        }

        @Override
        public long length() {
            return 0;
        }
    }

    private DataTransferStream  stream;
    private OutputStream        ot;

    public SmartDataTransferer(InputStream in)
    {
//        stream = new DataInputStream(in);
    }

    public SmartDataTransferer(OutputStream ot)
    {
//        stream = (T) new DataOutputStream(ot);
    }

    public SmartDataTransferer(char op)
    {
//        if(op)
    }


//    /** writing **/
//    public void         write(byte data[]);
//    public void         writeInt(int i);
//    public void         writeLong(long l);
//    public void         write(int b);
//    public void         writeShort(short s);
//    public void         writeChar(char c);
//    public void         writeObject(Serializable serialisable);
//    public void         writeEncodeable(Encodeable encodeable);
//    public void         write(Exportable exportable);
//    public void         writeUTF(String string);
//
//    /** reading **/
//    public void         read(byte[] data[]);
//    public byte[]       read();
//    public int          readInt();
//    public
}
