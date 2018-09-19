package com.riverssen.core.mpp.compiler;

import java.util.ArrayList;
import java.util.List;

public class StackTrace
{
    List<String> trace;

    public StackTrace()
    {
        trace = new ArrayList<>();
    }

    public void add(String place)
    {
        trace.add("at: " + place);
    }

    public String toString()
    {
        String string = "";
        for (String str : trace) string += str + "\n";
        return string;
    }
}
