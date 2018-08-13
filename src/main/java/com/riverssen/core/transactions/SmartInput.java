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

package com.riverssen.core.transactions;

import com.riverssen.core.utils.ByteUtil;

public class SmartInput
{
    public static int MAX_SIZE = 288;
    public static final int MODE_BYTE = 0;
    public static final int MODE_SHORT= 1;
    public static final int MODE_INT  = 2;
    private byte input[];

    public SmartInput(int utxos[], TransactionOutput outputs[])
    {
        int mode        = 0;
        for(int u : utxos)
            if(u > 255 && u <= Short.MAX_VALUE)
                mode = MODE_SHORT;
            else if(u > Short.MAX_VALUE)
                mode = MODE_INT;

        int size        = utxos.length;

        int index       = 0;
        input           = new byte[2];
        input[index ++] = (byte) mode;
        input[index ++] = (byte) size;

        switch (mode)
        {
            case MODE_BYTE:
                for(int utxo : utxos)
                    input = ByteUtil.concatenate(input, new byte[] {(byte) utxo});
                break;
            case MODE_SHORT:
                for(int utxo : utxos)
                    input = ByteUtil.concatenate(input, new byte[] {(byte) utxo});
                break;
            case MODE_INT:
                for(int utxo : utxos)
                    input = ByteUtil.concatenate(input, new byte[] {(byte) utxo});
                break;
        }

        input = ByteUtil.concatenate(input, ByteUtil.defaultEncoder().encode(input));
    }
}
