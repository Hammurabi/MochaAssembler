package com.riverssen.core.mpp.exceptions;

import com.riverssen.core.mpp.compiler.Token;

public class ParseException extends Exception
{
    public ParseException(String msg, Token token, String lines[])
    {
        super(msg + " at line: " + token.getLine() + " offset: " + token.getOffset() + "\n>" + (token.getLine() >= 0 ? lines[token.getLine()] : ""));
    }

    static String assemble(String lines[])
    {
        String string = "";

        for (String line : lines)
            string += line + "\n";

        return string;
    }
}
