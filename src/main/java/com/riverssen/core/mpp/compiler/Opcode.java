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

public class Opcode
{
    private static short i = 0;
    public static final short
    HLT = 0,

    ADD = 0,
    SUB = 1,
    MUL = 2,
    DIV = 3,
    MOD = 4,
    POW = 5,
    SIN = 6,
    COS = 7,
    TAN = 8,

    AND = 9,
    OR = 10,
    RSHIFT = 11,
    LSHIFT = 12,
    ASSERT = 13,
    LESSTHAN = 14,
    MORETHAN = 15,
    LESSTHANE = 16,
    MORETHANE = 17,

    PUSH = 18,
    POP = 19,

    PUSH_INT = 20,
    PUSH_UINT = 21,
    PUSH_UINT256 = 22,
    PUSH_FLOAT = 23,
    PUSH_DECIMAL = 24,

    MOV = 25, //move from stack to memory (object is popped)
    LOD = 26, //load from memory to stack (object is duplicated)
    NEW = 27, //create a new element in memory
    NEW_INT = 28,
    NEW_UINT = 29,
    NEW_UINT256 = 30,
    NEW_FLOAT = 31,
    NEW_DECIMAL = 32,

    IF = 33,
    ELSEIF = 34,
    ELSE = 35,
    LOOP = 36,
    FOR = 37,
    FOREACH = 38,
    WHILE = 39,

    FUN = 40,
    CALL = 41,

    ENCRYPT = 42;

//    public static final short
//    HLT = 0,
//
//    //MATH
//    ADD = 1,
//    SUB = 2,
//    MLT = 3,
//    DIV = 4,
//    MOD = 5,
//    POW = 6,
//    SIN = 7,
//    COS = 8,
//
//    //BOOL
//    AND = 9,
//    OR = 10,
//    RSHFT = 11,
//    LSHFT = 12,
//    ASSERT = 13,
//
//    //OPERATIONAL
//    SET = 14, //STACK.SET(INDEX, OBJECT)
//    PUT = 15, //MEMORY.PUT(INDEX, OBJECT)
//    PRT = 16,
//
//    POP = 17,
//    PUSH = 18,
//    PUSH_INT = 19,
//    PUSH_UINT = 20,
//    PUSH_UINT256 = 21,
//    PUSH_FLOAT = 22,
//    PUSH_BIGDECIMAL = 23,
//
//    NEW = 24,
//    CALL = 25,
//    IF = 26,
//    ELSEIF = 27,
//    ELSE = 28,
//    FOR = 29,
//    WHILE = 30,
//    LOOP = 31,
//
//    FUN = 32,
//
//    LOAD = 33, //stack.push(stack.get(int))
//
//    EXT = 400;
}
