package com.riverssen.core.mpp.compiler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreprocessedProgram
{
    private String finalProgram;

    public PreprocessedProgram(String program, File location, DynamicLibraryLoader dynamicLibraryLoader)
    {
        removeComments(program);
        checkIncludes(program, dynamicLibraryLoader);
        String lines[] = program.split("\n");
        checkIfndIfndefs(lines, 0);
    }

    private String checkIfndIfndefs(final String lines[], int start)
    {
        String test = "";

        Map<String, String> defines = new HashMap<>();

        for (int i = 0; i < lines.length; i ++)
        {
            String line = lines[i];

            if (line.matches("\\#(define)\\s+[A-z](\\w*)*"))
            {
                defines.put(line.replaceAll("\\#(define)\\s*", ""), "");
            } else if (line.matches("\\#(undefine)\\s+[A-z](\\w*)*"))
            {
                if (!defines.containsKey(line.replaceAll("\\#(define)\\s*", ""))
                {
                    System.err.println("'" + line.replaceAll("\\#(define)\\s*", "") + "' cannot be undefined.");
                    System.exit(0);
                }
                defines.remove(line.replaceAll("\\#(define)\\s*", ""));
            } else if (line.matches("\\#(define)(\\s+[A-z](\\w*)*\\s+[A-z](\\w*)*)"))
            {
                String def = line.replaceAll("\\#(define)\\s*", "").replaceAll("\\s+", " ");

                String define[] = def.split(" ");

                defines.put(define[0], define[1]);
            } else if (line.matches("\\#(ifdef)(\\s+[A-z](\\w*)*\\s+[A-z](\\w*)*)"))
            {
                String if_case = "";
            }
        }

        return test;
    }

    private String getText(final String[] lines, int startIndex)
    {
        String text = "";

        return text;
    }

    private String getAllBetweenIfs(final String[] lines, int startIndex)
    {
        String betweenIfs = "";

        return betweenIfs;
    }

    private String checkIncludes(final String control, DynamicLibraryLoader dynamicLibraryLoader)
    {
        String test = control;

        String internal_include_regex = "\\#(include)\\s*\\<\\w(\\w|\\.|\\-|\\/)*\\>";
        String external_include_regex = "\\#(include)\\s*\"\\w(\\w|\\.|\\-|\\/)*\"";

        Matcher internal_include = Pattern.compile(internal_include_regex).matcher(test);

        while (internal_include.find())
        {
            int start   = internal_include.start();
            int end     = internal_include.end();

            String A = test.substring(0, start);
            String B = test.substring(end, test.length());

            String d = internal_include.group();
            d        = d.substring(d.indexOf("<") + 1, d.indexOf(">"));

            String c = dynamicLibraryLoader.get(d);

            test = A + c + B;
        }

        Matcher external_include = Pattern.compile(external_include_regex).matcher(test);

        while (external_include.find())
        {
            int start   = external_include.start();
            int end     = external_include.end();

            String A = test.substring(0, start);
            String B = test.substring(end, test.length());

            String d = external_include.group();
            d        = d.substring(d.indexOf('"') + 1, d.lastIndexOf('"'));

            String c = dynamicLibraryLoader.loadFile(d);

            test = A + c + B;
        }

        return test;
    }

    private String removeComments(final String control)
    {
        String test = "";

        String lines[] = control.split("\n");

        for (String line : lines)
        {
            String adjusted = line.replaceAll("\\/\\/.*", "").replaceAll("\\/\\*\\*.*\\*\\*\\/", "").replaceAll("\\/\\*\\*.*", "").replaceAll(".*\\*\\*\\/", "");

            test += adjusted + "\n";
        }

        return test;
    }

    public String getFinalProgram()
    {
        return finalProgram;
    }
}