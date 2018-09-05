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

public class instructions
{
    public static final short
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

    push_f_8 = 12,

    push_f_16 = 13,

    push_f_32 = 14,

    push_f_64 = 15,

    push_f_128 = 16,

    push_f_256 = 17,

    push_a = 18,

    push_s = 19,

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

    pop_f_8 = 32,

    pop_f_16 = 33,

    pop_f_32 = 34,

    pop_f_64 = 35,

    pop_f_128 = 36,

    pop_f_256 = 37,

    pop_a = 38,

    pop_s = 39,

    op_add = 40,

    op_mul = 41,

    op_div = 42,

    op_sub = 43,

    op_mod = 44,

    op_pow = 45,

    boolean_and = 46,
    boolean_or = 47,
    boolean_not = 48,

    logical_and = 49,
    logical_or = 50,
    logical_xor = 51,

    lshift = 52,
    rshift = 53,

    inc = 54,
    dec = 55,

    url_read = 56,
    url_write = 57,

    cte_read = 58,
    cte_write = 59,

    fle_read = 60,
    fle_write = 61,

    mov = 62,
    /** move from memory to register **/
    load = 63,

    stack_read = 64,
    // undefined operator
    stack_load = 65,
    // push stack_get(i)
    stack_write = 66,
    // push stack_get(i)
    stack_set = 67,
    // push stack_get(i)
    memory_read = 68,
    // undefined operator
    memory_load = 69,
    // push memory_get(i)
    memory_write = 70,

    memory_write_stack = 71,

    stack_duplicate = 72,
    //push stack.peek
    stack_swap = 73,

    iprint = 74,

    fprint = 75,

    print = 76,

    goto_ = 77,

    call_ = 78,

    call_native = 79,

    if_ = 80,

    elif_ = 81,

    else_ = 82,

    start_func = 83,

    end_func = 84,

    push = 85,

    pop = 86,

    malloc_ = 87,

    calloc_ = 88,

    free_ = 89,

    less_ = 90,

    more_ = 91,

    lesseq_ = 92,

    moreeq_ = 93,

    equal_ = 94,

    halt = 95;
}
