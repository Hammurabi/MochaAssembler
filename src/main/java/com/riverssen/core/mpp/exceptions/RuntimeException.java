package com.riverssen.core.mpp.exceptions;

public class RuntimeException extends Exception
{
    public RuntimeException(String msg)
    {
        this(msg, new StackTrace());
    }

    public RuntimeException(String msg, StackTrace stackTrace)
    {
        super(msg + " \nat: " + stackTrace.toString());
    }
}
