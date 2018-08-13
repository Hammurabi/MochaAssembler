/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Riverssen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.riverssen.core.mpp;

import com.riverssen.core.mpp.compiler.LexedProgram;
import com.riverssen.core.mpp.compiler.ParsedProgram;
import com.riverssen.core.mpp.contracts.Contracts;
import com.riverssen.core.mpp.exceptions.ParseException;

import java.io.*;

public class MainCompiler
{
    private static String stdlib()
            throws IOException
    {
        String utf_program = "";

        BufferedReader reader = new BufferedReader(new InputStreamReader(Contracts.class.getClassLoader().getResource("std.mpp").openStream()));

        String line = "";

        while ((line = reader.readLine()) != null)
            utf_program += line + "\n";

        reader.close();

        return utf_program;
    }

    public static void main(String args[]) throws ParseException, IOException {
        final String[] commands = {
                "---commands---",
                "-c <path to main-class> --compiles and exports it to class.o file--",
                "-t <path to main-class> --creates human-readable abstract syntax tree and exports it to class.t file---",
                "-compile <path to main-class>",
                "-tree <path to main-class>",
                "---arguments---",
                "--v_x --appended at end of command to define language version (e.g --v_1_0a--"
        };

        if (args == null || args.length == 0)
        {
            System.err.println("no arguments...");
            for (String string : commands)
                System.out.println(string);

            System.exit(0);
        }

        if (args.length >= 2)
        {
            File main_class = new File(args[1].substring(1, args[1].length() - 1));

            String arg = args[0];

            if (!main_class.exists())
            {
                System.err.println("file '" + main_class + "' doesn't exist.");
                System.exit(0);
            }

            if (arg.equals("-t") || arg.equals("-tree"))
            {
                String utf_program = "" + stdlib();

                BufferedReader reader = new BufferedReader(new FileReader(main_class));

                String line = "";

                while ((line = reader.readLine()) != null)
                    utf_program += line + "\n";

                reader.close();

                LexedProgram lexedProgram = new LexedProgram(utf_program);

                ParsedProgram parsedProgram = new ParsedProgram(lexedProgram);

                String t = (parsedProgram.getRoot().humanReadable(0));

                File out = new File(main_class.getParent() + File.separator + "" + main_class.getName().substring(0, main_class.getName().lastIndexOf(".")) + ".t");

                BufferedWriter writer = new BufferedWriter(new FileWriter(out));

                writer.write(t);

                writer.flush();
                writer.close();
            } else if (arg.equals("-c") || arg.equals("-compile"))
            {
                String utf_program = "" + stdlib();

                BufferedReader reader = new BufferedReader(new FileReader(main_class));

                String line = "";

                while ((line = reader.readLine()) != null)
                    utf_program += line + "\n";

                reader.close();

                LexedProgram lexedProgram = new LexedProgram(utf_program);

                ParsedProgram parsedProgram = new ParsedProgram(lexedProgram);

                System.out.println(parsedProgram.getRoot().humanReadable(0));

                CompiledProgram program = new CompiledProgram(parsedProgram);

                program.spit(new File(main_class.getParent() + File.separator + "" + main_class.getName().substring(0, main_class.getName().lastIndexOf(".")) + ".o"));
            }
        }
    }
}
