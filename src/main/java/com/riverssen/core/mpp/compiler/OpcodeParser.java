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

package com.riverssen.core.mpp.compiler;

import com.riverssen.core.mpp.Opcode;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class OpcodeParser
{
    Opcode list[];

    public OpcodeParser(byte opcode[]) throws BufferUnderflowException
    {
        ByteBuffer opcodes = ByteBuffer.allocate(opcode.length);
        opcodes.put(opcode);
        opcodes.flip();

//        while(opcodes.remaining() > 0)
//        {
//            int b = opcodes.get();
//
//            switch (b)
//            {
//                case push:  new Push(opcodes);
//                case pop:   new Pop(opcodes);
//                case print: new Print(opcodes);
//
//                /** math **/
//                case add:   new Add(opcodes);
//                case sub:   new Sub(opcodes);
//                case mul:   new Mul(opcodes);
//                case div:   new Div(opcodes);
//                case mod:   new Mod(opcodes);
//                case get:   new Get(opcodes);
//            }
//        }
    }

    public Opcode[] getOpcode()
    {
        return list;
    }
}