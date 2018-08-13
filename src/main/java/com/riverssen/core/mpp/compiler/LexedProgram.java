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

import java.util.*;

public class LexedProgram
{
    private Set<Token> allChars = new LinkedHashSet<>();

    public LexedProgram(String program)
    {
        program = program.replaceAll("//.*", "");

        while (program.contains("/*"))
            program = program.replace(program.substring(program.indexOf("/*"), program.indexOf("*/") + 2), "");

        final char separators[] = { '.', '=', '+', '-', '\'', '"', ',', '<', '>', '?', ';', ':', '!', '\\', '/', '[', ']', '{', '}', '(', ')', '*', '&', '^', '%', '$', '#', '@', '~' };

        /** this type of splitting into tokens will not work with strings, ie: "hello world" will turn into "hello and world" **/
        String raw_words[] = program.replaceAll("\n", " ").split("\\s+");

        final char EOF = '\0';
        final char END = '\n';
        final char WTS = ' ';
        final char TAB = '\t';

        boolean isString = false;

        Token token = null;
        int line = 1;
        int whitespace = 0;
        int offset = 0;

        for (int i = 0; i < program.length(); i++)
        {
            char current = program.charAt(i);
            char last = i > 0 ? program.charAt(i - 1) : '\0';
            char next = i < program.length() - 1 ? program.charAt(i + 1) : '\0';

            if (current == END)
            {
                line++;
                allChars.add(token);
                allChars.add(new Token(Token.Type.END));
                token = null;
                offset = 0;
                continue;
            } else if (current == WTS && !(token != null && (token.toString().startsWith("\"") || token.toString().startsWith("\'"))))
            {
                whitespace++;
                allChars.add(token);
                offset ++;
                token = null;
                continue;
            } else if (current == TAB && !(token != null && (token.toString().startsWith("\"") || token.toString().startsWith("\'"))))
            {
                whitespace += 4;
                offset += 4;
                allChars.add(token);
                token = null;
                continue;
            }

            boolean separator = false;

            for (char s : separators) if (current == s) separator = true;

            boolean wasnull = token == null;

            if (token == null) token = new Token("", line, offset, whitespace);

            isString = (token.toString().startsWith("\"") || token.toString().startsWith("\'"));

            if (isString || (wasnull && (current == '"' || current == '\'')))
            {
                if (current == '"' || current == '\'' || separator)
                {
                    if (last == '\\') token.append(current);
                    else if (token.toString().startsWith(current + ""))
                    {
                        token.append(current);
                        allChars.add(token);
                        token = null;
                    } else token.append(current);
                } else token.append(current);

                offset++;
                continue;
            } else if (separator)
            {
                allChars.add(token);
                token = new Token("" + current, line, offset, whitespace);
                allChars.add(token);
                token = null;
            } else token.append(current);

            offset++;
        }

        allChars.remove(null);
    }

    public Set<Token> getTokens()
    {
        return allChars;
    }
}