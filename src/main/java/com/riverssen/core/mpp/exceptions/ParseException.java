package com.riverssen.core.mpp.exceptions;

import com.riverssen.core.mpp.compiler.Token;

public class ParseException extends Exception
{
    public ParseException(String msg, Token token)
    {
        super(msg + " at line: " + token.getLine() + " offset: " + token.getOffset());
    }
}
