package com.riverssen.core.mpp.compiler;

public class DefaultErrorCallBack implements ErrorCallBackI
{
    @Override
    public void execute(String error, StackTrace stackTrace, int severity)
    {
        System.err.println("error: " + error);
        if (stackTrace == null) System.err.println("no stacktrace provided.");
        else System.err.println("at: " + stackTrace.toString());

        if (severity == 1) System.exit(0);
    }
}
