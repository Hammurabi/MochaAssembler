package mochaxx;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Generator
{
    private static class Opcode implements Comparable<Opcode>
    {
        String opcodeName;
        String implementationSpec;
        String textInfo;
        int    priority;
        int    numberID = -1;
        String assemblerInfo;

        @Override
        public int compareTo(Opcode o)
        {
            return (priority > o.priority) ? ((priority == o.priority) ? 0 : -1) : 1;
        }

        @Override
        public String toString()
        {
            return opcodeName + "=" + priority;
        }

        public String getHumanReadableReaderCode()
        {
            if (assemblerInfo == null) return "";
            if (assemblerInfo.length() == 0) return "";

            if (assemblerInfo.contains(":"))
            {
                String split[] = assemblerInfo.split(":");
                String collectiveMany = "";

                for (String string : split)
                    collectiveMany += "add(x);\n".replace("x", "" + string + "(in.poll())");

                return collectiveMany;
            }
            else return "add(x);".replace("x", "" + assemblerInfo.substring(2, assemblerInfo.length() - 1) + "(in.poll())");
        }
    }

    private String toWrite;

    public Generator(String data)
    {
        Queue<Opcode> queue = new PriorityQueue<>();

        String split[] = data.split("\n");
        int type = 0;
        int ops = 0;

        for (String line : split)
        {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#") || line.matches("\\s+"))
                continue;

            if (line.startsWith("opcodes:"))
            {
                type = 1;
                continue;
            }
            if (line.startsWith("types:"))
                continue;

            if (type == 1)
            {
                Matcher implementationMatcher   = Pattern.compile("\\{.*\\}").matcher(line);
                Matcher txtDescriptionMatcher   = Pattern.compile("\\[.*\\]").matcher(line);
                Matcher priorityMatcher         = Pattern.compile("p=\\d+").matcher(line);
                Matcher assemblerInfoMatcher    = Pattern.compile("&t.*&").matcher(line);
                Opcode opcode = new Opcode();
                opcode.opcodeName           = line.split("\\s+")[0];
                opcode.implementationSpec   = implementationMatcher.find() ? implementationMatcher.group() : null;
                opcode.textInfo             = txtDescriptionMatcher.find() ? txtDescriptionMatcher.group() : null;
                opcode.priority             = priorityMatcher.find() ? Integer.parseInt(priorityMatcher.group().split("=")[1]) : 0;
                opcode.numberID             = ops++;
                opcode.assemblerInfo        = assemblerInfoMatcher.find() ? assemblerInfoMatcher.group() : null;

                queue.add(opcode);
            }
        }

        System.out.println("found " + ops + " opcodes.");
        System.out.println(queue);
        System.out.println("\n\n\n");

        String implementation = "";
        String opcodes = "";
        String assemblerInfo = "";
        String map = "";
        String debugmaps = "";


        while (queue.size() > 0)
        {
            Opcode opcode = queue.poll();
            String info = "";
            String name = opcode.opcodeName;

            while (name.length() < 20)
                name += " ";

            if (opcode.textInfo != null && opcode.textInfo.length() > 1)
                info = "/**" + opcode.textInfo.substring(1, opcode.textInfo.length() - 1) +  "**/";

            if (opcode.implementationSpec != null)
                implementation += "\n\t" + info + "\n\tcase " + opcode.opcodeName + ":\n\t\t" + opcode.implementationSpec + "\n\t\tbreak;";

            if (opcode.assemblerInfo != null)
                assemblerInfo += "\n\t" + info + "\n\tcase \"" + opcode.opcodeName + "\":\n\t\tadd(" + opcode.numberID + ");" + opcode.getHumanReadableReaderCode() + "\n\t\tbreak;";

            map += "\nmap.put(\"" + opcode.opcodeName + "\", " + opcode.numberID + ");";

            debugmaps += "_dopmap_[" + opcode.numberID + "] = \"" + opcode.opcodeName + "\";\n";

            opcodes += "\n\t" + name + " = " + opcode.numberID + "," + info;
        }

        toWrite = "enum Op\n{\n"+opcodes+"\n};\n\n\n" + "void execute(...)\n{\n" + implementation + "\n}\n\n\n\n\n" + map + "\n\n\n\n\n\n" + assemblerInfo + "\n\n\n\n\n\n" + debugmaps;

        System.out.println("successfully generated outputs.");
    }

    public void write(File file) throws IOException
    {
        FileWriter stream = new FileWriter(file);
        stream.write(toWrite);
        stream.flush();
        stream.close();
    }
}
