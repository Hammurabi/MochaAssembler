package com.riverssen.core.mpp.compiler;

public interface ErrorCallBackI
{
    public static int UNSEVERE = 0, SEVERE = 1, MESSAGE = 2;
    void execute(String error, StackTrace stackTrace, int severity);
}