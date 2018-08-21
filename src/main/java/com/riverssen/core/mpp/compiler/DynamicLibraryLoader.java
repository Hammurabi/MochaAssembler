package com.riverssen.core.mpp.compiler;

import com.riverssen.core.mpp.MainCompiler;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DynamicLibraryLoader
{
    private Map<String, String> files = new HashMap<>();
    private File                main;

    public static final String extensions[] = {
            "mxx", "m++", "mpp", "m", "mocha", "mochapp", "mochaplusplus"
    };

    public DynamicLibraryLoader(File main, File... file)
    {
        this.main = main;
    }

    public String loadLibrary(String name)
    {
        return null;
    }

    public String loadFile(String name)
    {
        String utf_program = "";

        try{
            BufferedReader reader = new BufferedReader(new FileReader(new File(main + File.separator + name)));

            String line = "";

            while ((line = reader.readLine()) != null)
                utf_program += line + "\n";

            reader.close();
        } catch (Exception e)
        {
            System.err.println("cannot find file '" + name + "'.");
            System.exit(0);
        }

        return utf_program;
    }

    private static String stdlib()
            throws IOException
    {
        String utf_program = "";

        String includes[] = {
                "std", "rivercoin", "oop", "types"
        };

        for (String include : includes)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(MainCompiler.class.getClassLoader().getResource(include + ".mxx").openStream()));

            String line = "";

            while ((line = reader.readLine()) != null)
                utf_program += line + "\n";

            reader.close();
        }

        return utf_program;
    }

    public String get(String d)
    {
        String utf_program = "A";
        return utf_program;
    }
}