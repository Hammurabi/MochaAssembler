package mochaxx;

import mochaxx.compiler.DynamicLibraryLoader;
import mochaxx.compiler.LexedProgram;
import mochaxx.compiler.PreprocessedProgram;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;

public class Start
{
    public static long version = 0;

    private static String readData(String link) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(new File(link)));
        String data = "";
        String lineofdata = "";

        while ((lineofdata = reader.readLine()) != null)
            data += lineofdata + "\n";

        reader.close();

        return data;
    }

    private static void assemble(String link) throws IOException
    {
        String data = readData(link);

        OpcodeReader opcodeReader = new OpcodeReader(data);

        opcodeReader.write(new File(new File(link).getParent().toString() + File.separator + new File(link).getName().substring(0, new File(link).getName().indexOf(".")) + ".mops"));
    }

    private static void generate(String link) throws IOException
    {
        Generator generator = new Generator(readData(link));
        generator.write(new File(new File(link).getParent().toString() + File.separator + new File(link).getName().substring(0, new File(link).getName().indexOf(".")) + ".menvgen"));
    }

    public static void main(String[] args) throws IOException
    {
        if (args == null || (args != null && args.length == 0))
        {
            System.err.println("MochaAssembler: no arguments provided.");
            System.exit(0);
        }

        LinkedList<String> queue = new LinkedList<>();

        for (String arg : args)
            queue.push(arg);

        Collections.reverse(queue);

        while (queue.size() > 0)
        {
            String arg = queue.poll();

            switch (arg)
            {
                case "-c":
                    compile(queue.poll());
                    break;
                case "-v":
                    version = Long.parseLong(queue.poll());
                    break;
                case "-assembler":
                case "-a":
                    assemble(queue.poll());
                    break;
                case "-generator":
                case "-g":
                    generate(queue.poll());
                    break;
                    default:
                        System.err.println("MochaAssembler: unknown command 'cmd'.".replace("cmd", arg));
                        System.exit(0);
                        break;
            }
        }
    }

    private static void compile(String poll) throws IOException
    {
        String data = readData(poll);
        PreprocessedProgram program = new PreprocessedProgram(data, new File(poll), new DynamicLibraryLoader(new File(poll)));
        LexedProgram lexedProgram = new LexedProgram(program.getFinalProgram());
        ParsedProgram parsedProgram = new ParsedProgram(lexedProgram);
    }
}
