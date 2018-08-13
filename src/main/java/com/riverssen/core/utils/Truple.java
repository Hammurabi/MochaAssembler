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

public class Truple<I, J, C>
{
    private I i;
    private J j;
    private C c;

    public Truple(I i, J j, C c)
    {
        this.i = i;
        this.j = j;
        this.c = c;
    }

    public synchronized I getI()
    {
        return i;
    }

    public synchronized void setI(I i)
    {
        this.i = i;
    }

    public synchronized J getJ()
    {
        return j;
    }

    public synchronized void setJ(J j)
    {
        this.j = j;
    }

    @Override
    public String toString()
    {
        return i + " " + j + " " + c;
    }

    public C getC() {
        return c;
    }

    public void setC(C c) {
        this.c = c;
    }
}