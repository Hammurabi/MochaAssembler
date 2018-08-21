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
    int line = 1;
    int whitespace = 0;
    int offset = 0;

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
        Token prevs = null;

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
                preprocesses(token, prevs);
                prevs = token;
                token = null;
                offset = 0;
                whitespace = 0;
                continue;
            } else if (current == WTS && !(token != null && (token.toString().startsWith("\"") || token.toString().startsWith("\'"))))
            {
                whitespace++;
                allChars.add(token);
                offset ++;
                preprocesses(token, prevs);
                prevs = token;
                token = null;
                continue;
            } else if (current == TAB && !(token != null && (token.toString().startsWith("\"") || token.toString().startsWith("\'"))))
            {
                whitespace += 4;
                offset += 4;
                allChars.add(token);
                preprocesses(token, prevs);
                prevs = token;
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
                        prevs = token;
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
                prevs = token;
                token = null;
            } else token.append(current);

            offset++;
        }

        allChars.remove(null);
        preprocess();
    }

    private void preprocesses(Token token, Token prevs)
    {
        if (token != null && token.toString().equals("reset") && prevs != null && prevs.toString().equals("#"))
        {
            line = 1;
            allChars.remove(prevs);
            allChars.remove(token);
        }
    }

    private void preprocess()
    {
        List<Token> control = new ArrayList<>(allChars);
        List<Token> all = new ArrayList<>(allChars);

        for (int i = 0; i < all.size(); i ++)
        {
            Token a = all.get(i);

            if (all.size() > (i + 1) && a.getType().equals(Token.Type.SYMBOL) && a.toString().equals("#"))
            {
                Token b = all.get(i + 1);

                if (b.toString().equals("define"))
                {
                    String value = "";

                    if (all.size() > (i + 2))
                    {
                        Token c = all.get(i + 2);

                        if (c.getType().equals(Token.Type.END))
                        {
                        } else {
                            for (int x = i + 2; x < all.size(); x ++)
                            {
                            }
                        }
                    }
                }
            }
        }
    }

    public Set<Token> getTokens()
    {
        return allChars;
    }
}