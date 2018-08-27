package com.riverssen.core.mpp.compiler;

import com.riverssen.core.utils.Tuple;

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
        finalProgram = program;
        program = removeComments(program);
        program = checkIncludes(program, dynamicLibraryLoader);
        String lines[] = program.split("\n");
        program = checkIfndIfndefs(lines, -1, new HashMap<>(), true).getI();

        finalProgram = program;
    }

    private Tuple<String, Integer> checkIfndIfndefs(final String lines[], int start, Map<String, String> master, boolean allow)
    {
        String test = "";

        Map<String, String> defines = master;

        for (int i = start + 1; i < lines.length; i ++)
        {
            String line = lines[i];

            if (line.matches("(typedef)\\s+(.+)\\s+(.+)\\;"))
            {
                if (allow)
                    defines.put(line.replaceAll("\\#(define)\\s*", ""), "");
                test += "\n";
            }
            else if (line.matches("\\#(define)\\s+[A-z](\\w*)*"))
            {
                if (allow)
                defines.put(line.replaceAll("\\#(define)\\s*", ""), "");
                test += "\n";
            } else if (line.matches("\\#(undefine)\\s+[A-z](\\w*)*"))
            {
                if (allow)
                {
                    if (!defines.containsKey(line.replaceAll("\\#(define)\\s*", "")))
                    {
                        System.err.println("'" + line.replaceAll("\\#(define)\\s*", "") + "' cannot be undefined.");
                        System.exit(0);
                    }
                    defines.remove(line.replaceAll("\\#(define)\\s*", ""));
                }
                test += "\n";
            } else if (line.matches("\\#(define)(\\s+[A-z](\\w*)*\\s+(\\w*)*)"))
            {
                if (allow)
                {
                    String def = line.replaceAll("\\#(define)\\s*", "").replaceAll("\\s+", " ");

                    String define[] = def.split(" ");

                    defines.put(define[0], define[1]);
                }
                test += "\n";
            } else if (line.matches("\\#(ifdef)\\s+([A-z](\\w*)*)"))
            {
                String if_reason = line.replaceAll("\\#(ifdef)\\s+", "");
                Tuple<String, Integer> if_case = getAllBetweenIfs(lines, i, defines.containsKey(if_reason), defines);
                test += if_case.getI();
                i = if_case.getJ();
            } else if (line.matches("\\#(ifndef)\\s+([A-z](\\w*)*)"))
            {
                String if_reason = line.replaceAll("\\#(ifndef)\\s+", "");
                Tuple<String, Integer> if_case = getAllBetweenIfs(lines, i, !defines.containsKey(if_reason), defines);
                test += if_case.getI();
                i = if_case.getJ();
            } else if (line.matches("\\#(endif)"))
            {
                if (start == -1)
                {
                    System.err.println("No if case for endif.");
                    System.exit(0);
                }
                return new Tuple<>(test + "\n", i);
            } else {
                String adjusted = line;

                for (String def : defines.keySet())
                    adjusted = adjusted.replaceAll("\\b" + def + "\\b", defines.get(def));

                test += adjusted + "\n";
            }
        }

        return new Tuple<>(test, -1);
    }

    private Tuple<String, Integer> getAllBetweenIfs(final String[] lines, int startIndex, boolean add, Map<String, String> master)
    {
        String betweenIfs = "";

        Tuple<String, Integer> text = checkIfndIfndefs(lines, startIndex, master, add);

        if (text.getJ() == -1)
        {
            System.err.println("No endif found.");
            System.exit(0);
        }

        if (add) return text;

        String l[] = text.getI().split("\n");

        for (String line : l)
            betweenIfs += "\n";

        return new Tuple<>(betweenIfs, text.getJ());
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