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

public class MppCompiler
{
    private interface ASTGrammar
    {
        void onMain();
        void onCallMethod();
        void onAccessValue();
    }

    public static byte[] compile(String string, ASTGrammar astGrammar)
    {
        return compile(parse(lex(string)), astGrammar);
    }

    private static class LexicalString
    {
        private String  value;
        private int     line;
        private int     offset;
        private int     whitespace;

        public LexicalString(String value, int line, int offset, int whitespace)
        {
            this.value = value;
            this.line = line;
            this.offset = offset;
            this.whitespace = whitespace;
        }
    }

    private static LexedProgram lex(String text)
    {
        return new LexedProgram(text);
    }

    private static class ParsedProgram
    {
    }

    private static ParsedProgram parse(LexedProgram program)
    {
        return new ParsedProgram();
    }

    private static byte[] compile(ParsedProgram program, ASTGrammar astGrammar)
    {
        return null;
    }
}