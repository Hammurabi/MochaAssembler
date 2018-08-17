package com.riverssen.core.mpp.compilation;

import java.util.ArrayList;
import java.util.List;

public class StackTrace
{
    GlobalSpace space;
    List<String> trace;

    public StackTrace(GlobalSpace space)
    {
        this.space = space;
        trace = new ArrayList<>();
    }

    public void add(String type, String name)
    {
        trace.add(type + "__" + name);
    }

    public String toString()
    {
        String string = "@space";

        for (String str : trace) string += "." + str;

        return string;
    }
}
