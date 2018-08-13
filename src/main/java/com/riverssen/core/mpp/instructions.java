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

package com.riverssen.core.mpp;

public class instructions
{
    public static final short
    /**
     * Push
     */

    /** integers **/
    push_i_8 = 0,
    push_i_16 = 1,
    push_i_32 = 2,
    push_i_64 = 3,
    push_i_128 = 4,
    push_i_256 = 5,
    push_i_8u = 6,
    push_i_16u = 7,
    push_i_32u = 8,
    push_i_64u = 9,
    push_i_128u = 10,
    push_i_256u = 11,

    /** floats **/
    push_f_8 = 12,
    push_f_16 = 13,
    push_f_32 = 14,
    push_f_64 = 15,
    push_f_128 = 16,
    push_f_256 = 17,

    push_a = 18,
    push_s = 19,


    /**
     * Pop
     */

    /** integers **/
    pop_i_8 = 20,
    pop_i_16 = 21,
    pop_i_32 = 22,
    pop_i_64 = 23,
    pop_i_128 = 24,
    pop_i_256 = 25,
    pop_i_8u = 26,
    pop_i_16u = 27,
    pop_i_32u = 28,
    pop_i_64u = 29,
    pop_i_128u = 30,
    pop_i_256u = 31,

    /** floats **/
    pop_f_8 = 32,
    pop_f_16 = 33,
    pop_f_32 = 34,
    pop_f_64 = 35,
    pop_f_128 = 36,
    pop_f_256 = 37,

    pop_a = 38,
    pop_s = 39,

    /**
     * Math Operations
     */

    op_add = 40,
    op_mul = 41,
    op_div = 42,
    op_sub = 43,
    op_mod = 44,
    op_pow = 45,

    iadd = 46,
    imul = 47,
    idiv = 48,
    isub = 49,

    fadd = 50,
    fmul = 51,
    fdiv = 52,
    fsub = 53,

    imod = 54,
    ipow = 55,
    fmod_ = 56,
    fpow = 57,

    boolean_and = 58, boolean_or = 59, boolean_not = 60,
    logical_and = 61, logical_or = 62, logical_xor = 63,

    lshift = 64, rshift = 65,

    inc = 66,

    /**
     * IO
     */

    url_read = 67, url_write = 68,
    cte_read = 69, cte_write = 70,
    fle_read = 71, fle_write = 72,

    /**
     * Other, Misc
     */

    stack_read = 73, // undefined operator
    stack_load = 74, // push stack_get(i)
    memory_read = 75, // undefined operator
    memory_load = 76, // push memory_get(i)
    memory_write = 77, // memory.set(i, data[])
    memory_write_stack = 78, //memory.set(i, stack.pop)

    stack_duplicate = 79, //push stack.peek
    stack_swap = 80, //stack.set(x, stack.get(y)) and stack.set(y, stack.get(x))

    iprint = 81,
    fprint = 82,
    print = 83,

    goto_ = 84,

    call_ = 85,
    call_native = 86,

    if_ = 87,
    elif_ = 88,
    else_ = 89,

    /** none executable instructions **/
    start_func = 90,
    end_func = 91,
    _new_ = 92,
    push = 93,
    pop = 94,
    malloc_ = 95,
    calloc_ = 96;
}
