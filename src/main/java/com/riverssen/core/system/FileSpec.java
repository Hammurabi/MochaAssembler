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

package com.riverssen.core.system;

import java.io.*;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public abstract class FileSpec
{
    private File file;

    public FileSpec(File file)
    {
        this.file = file;
    }

    public DataInputStream asInputStream() throws IOException
    {
        return new DataInputStream(new InflaterInputStream(new FileInputStream(file)));
    }

    public DataOutputStream asOutputStream() throws IOException
    {
        return new DataOutputStream(new DeflaterOutputStream(new FileOutputStream(file)));
    }

    public byte[] asRawData() throws IOException {
        if (!file.exists()) return null;

        byte data[] = new byte[(int) file.length()];

        FileInputStream stream = new FileInputStream(file);

        stream.read(data);

        stream.close();

        return data;
    }

    public static FileSpec fromFile(File file)
    {
        if(file.isDirectory()) return new DirectoryFileSpec(file);
        else return new FileFileSpec(file);
    }

    public boolean isDirectory()
    {
        return file.isDirectory();
    }

    public File getFile()
    {
        return file;
    }

    public abstract FileSpec next();
    public abstract void     rewind();
    public abstract int      length();
    public abstract int      remaining();

    public static class DirectoryFileSpec extends FileSpec
    {
        private FileSpec subFiles[];
        private int      fileIndex;

        public DirectoryFileSpec(File file) {
            super(file);
            File files[] = file.listFiles();

            Arrays.sort(files, (a, b)->{
                if (a.lastModified() > b.lastModified()) return 1;
                else if (a.lastModified() == b.lastModified()) return 0;
                else return -1;
            });

            for (File file1 : files)
                if(file1.isDirectory()) subFiles[fileIndex ++] = new DirectoryFileSpec(file1);
                else subFiles[fileIndex ++] = new FileFileSpec(file1);

            rewind();
        }

        public FileSpec next()
        {
            return subFiles[fileIndex ++];
        }

        @Override
        public void rewind()
        {
            fileIndex = 0;
        }

        @Override
        public int length()
        {
            return subFiles.length;
        }

        @Override
        public int remaining() {
            return subFiles.length - fileIndex;
        }
    }

    public static class FileFileSpec extends FileSpec
    {
        public FileFileSpec(File file) {
            super(file);
        }

        @Override
        public FileSpec next() {
            return null;
        }

        @Override
        public void rewind() {
        }

        @Override
        public int length() {
            return 0;
        }

        @Override
        public int remaining() {
            return 0;
        }
    }
}
