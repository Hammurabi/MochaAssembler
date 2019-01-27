package mochaxx.compiler;

import mochaxx.Start;
import mochaxx.Tuple;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

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

    public Tuple<File, String> loadFile(String name)
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

        return new Tuple<>(new File(main + File.separator + name), utf_program);
    }

    private static String stdlib()
            throws IOException
    {
        String utf_program = "";

        String includes[] = {
                "std", "rivercoin", "oop", "types", "utils"
        };

        for (String include : includes)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Start.class.getClassLoader().getResource("/mochaxx/libs/" + include + ".mxx").openStream()));

            String line = "";

            while ((line = reader.readLine()) != null)
                utf_program += line + "\n";

            reader.close();
        }

        return utf_program;
    }

    public String get(String d)
    {
        String utf_program = "";

        if (files.containsKey(d))
            return files.get(d);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Start.class.getClassLoader().getResource("mochaxx/libs/" + d + ".mxx").openStream()));

            String line = "";

            while ((line = reader.readLine()) != null)
                utf_program += line + "\n";

            reader.close();
        } catch (Exception e) {
//            e.printStackTrace();
            System.err.println("no include '" + d + "' found.");
            System.exit(0);
        }
        return utf_program;
    }
}