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

public class Logger
{
    public static final String COLOUR_YELLOW    = isUnixBased() ? (char)27 + "[33m" : "";
    public static final String COLOUR_LIME      = isUnixBased() ? (char)27 + "[32m" : "";
    public static final String COLOUR_WHITE     = isUnixBased() ? (char)27 + "[27m" : "";
    public static final String COLOUR_BLUE      = isUnixBased() ? (char)27 + "[34m" : "";
    public static final String COLOUR_RED       = isUnixBased() ? (char)27 + "[31m" : "";

    private static boolean isUnixBased()
    {
        return !System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static void prt(String msg)
    {
        prt(COLOUR_WHITE, msg);
    }

    public static void prt(String colour, String msg)
    {
        System.out.println(colour + msg);
    }

    public static void err(String msg)
    {
        prt(COLOUR_RED, msg);
    }

    public static void alert(String s)
    {
        prt(COLOUR_LIME, s);
    }
}