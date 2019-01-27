package mochaxx.compiler;

import mochaxx.Base16;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mochaxx.compiler.Opcodes.*;

public class OpcodeReader
{
    private ByteArrayOutputStream   out;
    private static Map<String, Integer>    map;
    private static Map<String, Integer>    map2;
    private Map<String, Long>       funcRegister;
    private Map<String, Long>       funcPoint;
    private Map<String, String>     stringMap;
    private static final String     stringRegex = "(\\%\"\\%).*(\\%\"\\%)";

    static {
        map = new LinkedHashMap<>();
        map2 = new LinkedHashMap<>();

        map.put("add", 0);
        map.put("mul", 1);
        map.put("sub", 2);
        map.put("div", 3);
        map.put("invoke", 4);
        map.put("idynmc", 5);
        map.put("ispcal", 6);
        map.put("push", 7);
        map.put("push_0", 8);
        map.put("push_1", 9);
        map.put("ret", 10);
        map.put("mret", 11);
        map.put("mod", 12);
        map.put("and", 13);
        map.put("or", 14);
        map.put("xor", 15);
        map.put("not", 16);
        map.put("rshift", 17);
        map.put("lshift", 18);
        map.put("mret2", 19);
        map.put("mret4", 20);
        map.put("hpush", 21);
        map.put("pop", 22);
        map.put("cast", 23);
        map.put("cast_itf", 24);
        map.put("cast_fti", 25);
        map.put("jump", 26);
        map.put("mark", 27);
        map.put("goto", 28);
        map.put("cmp", 29);
        map.put("branch", 30);
        map.put("mov", 31);
        map.put("dup", 32);
        map.put("load", 33);
        map.put("store", 34);

        map.put("malloc", 35);
        map.put("calloc", 36);
        map.put("delete", 37);
        map.put("swap", 38);

        map.put("scpy", 39);
        map.put("memcpy", 40);















        /**
         * The ALU arguments.
         */

        map.put("signed", 0);
        map.put("unsigned", 1);
        map.put("float", 2);

        map.put("$0", 0);
        map.put("$1", 1);
        map.put("$fp", 2);

        /**
         * The ALU comparator arguments.
         */
        map.put("$eq", 0);
        map.put("$lt", 1);
        map.put("$gt", 2);
        map.put("$le", 3);
        map.put("$ge", 4);

        map.put("[$0+$lt]", 5);
        map.put("[$0+$gt]", 6);
        map.put("[$0+$le]", 7);
        map.put("[$0+$ge]", 8);

        map.put("[$fp+$lt]", 9);
        map.put("[$fp+$gt]", 10);
        map.put("[$fp+$le]", 11);
        map.put("[$fp+$ge]", 12);


//            spv = 0, //stack pop (1x stack_elementsize_t) --lower case v contains one corner;
//            spe = 1, //stack pop (2x stack_elementsize_t) --lower case e contains two corners;
//            spx = 2, //stack pop (4x stack_elementsize_t) --lower case x contains four corners;
//
//            /** cast a an integer to float **/
//            itf = 3,
//
//            /** cast an integer to double **/
//            itd = 4,
//
//            /** cast a single stack_elementsize_t to float (this assumes its a double)**/
//            etf = 5,
//
//        /**
//         * All push operations take a stack_elementsize_t
//         *
//         * stack.push ops.pop
//         */
//            psv = 6, //stack push (1x stack_elementsize_t) --lower case v contains one corner;
//            pse = 7, //stack push (2x stack_elementsize_t) --lower case e contains two corners;
//            psx = 8, //stack push (4x stack_elementsize_t) --lower case x contains four corners;
//
//            bpb = 9, //basepointer set (byte)
//            bps = 10, //basepointer set (2 byte)
//            bpi = 11, //basepointer set (4 byte)
//            bpv = 12, //basepointer set (8 byte)
//            bpe = 13, //basepointer set (16 byte)
//            bpx = 14, //basepointer set (32 byte)
//
//            pgb = 15, //basepointer get (byte)
//            pgb_s = 16, //basepointer get (byte)
//
//            pgs = 17, //basepointer get (2 byte)
//            pgs_s = 18, //basepointer get (2 byte)
//
//            pgi = 19, //basepointer get (4 byte)
//            pgi_s = 20, //basepointer get (4 byte)
//
//            pgv = 21, //basepointer get (8 byte)
//            pgv_s = 22, //basepointer get (8 byte)
//
//            pge = 23, //basepointer get (16 byte)
//            pge_s = 24, //basepointer get (16 byte)
//
//            pgx = 25, //basepointer get (32 byte)
//            pgx_s = 26, //basepointer get (32 byte)
//
//            pgf = 27, //basepointer get float

        /** Element Stack Pop **/
        map.put("$ESP+v", 0);
        map.put("$ESP+e", 1);
        map.put("$ESP+x", 2);

        map.put("$EEI", 3);
        map.put("$EEF", 4);
        map.put("$EFE", 5);

        /** Element Set Base **/
        map.put("$ESB+b", 9);
        map.put("$ESB+s", 10);
        map.put("$ESB+i", 11);
        map.put("$ESB+v", 12);
        map.put("$ESB+e", 13);
        map.put("$ESB+x", 14);

        /** Element Access Base **/
        map.put("$EAB+b", 15);
        map.put("$EAB+b$1", 16);
        map.put("$EAB+s", 17);
        map.put("$EAB+s$1", 18);
        map.put("$EAB+i", 19);
        map.put("$EAB+i$1", 20);
        map.put("$EAB+v", 21);
        map.put("$EAB+v$1", 22);
        map.put("$EAB+e", 23);
        map.put("$EAB+e$1", 24);
        map.put("$EAB+x", 25);
        map.put("$EAB+x$1", 26);
        map.put("$EAB+f", 27);
//        map.put("add", 33);
//        map.put("add", 34);
//        map.put("add", 35);
//        map.put("add", 36);
//        map.put("add", 37);
//        map.put("add", 38);























        /**
         * The ALU arguments.
         */

        map2.put("signed", 0);
        map2.put("unsigned", 1);
        map2.put("float", 2);

        map2.put("$0", 0);
        map2.put("$1", 1);
        map2.put("$fp", 2);

        /**
         * The ALU comparator arguments.
         */
        map2.put("$eq", 0);
        map2.put("$lt", 1);
        map2.put("$gt", 2);
        map2.put("$le", 3);
        map2.put("$ge", 4);

        map2.put("[$0+$lt]", 5);
        map2.put("[$0+$gt]", 6);
        map2.put("[$0+$le]", 7);
        map2.put("[$0+$ge]", 8);

        map2.put("[$fp+$lt]", 9);
        map2.put("[$fp+$gt]", 10);
        map2.put("[$fp+$le]", 11);
        map2.put("[$fp+$ge]", 12);


//            spv = 0, //stack pop (1x stack_elementsize_t) --lower case v contains one corner;
//            spe = 1, //stack pop (2x stack_elementsize_t) --lower case e contains two corners;
//            spx = 2, //stack pop (4x stack_elementsize_t) --lower case x contains four corners;
//
//            /** cast a an integer to float **/
//            itf = 3,
//
//            /** cast an integer to double **/
//            itd = 4,
//
//            /** cast a single stack_elementsize_t to float (this assumes its a double)**/
//            etf = 5,
//
//        /**
//         * All push operations take a stack_elementsize_t
//         *
//         * stack.push ops.pop
//         */
//            psv = 6, //stack push (1x stack_elementsize_t) --lower case v contains one corner;
//            pse = 7, //stack push (2x stack_elementsize_t) --lower case e contains two corners;
//            psx = 8, //stack push (4x stack_elementsize_t) --lower case x contains four corners;
//
//            bpb = 9, //basepointer set (byte)
//            bps = 10, //basepointer set (2 byte)
//            bpi = 11, //basepointer set (4 byte)
//            bpv = 12, //basepointer set (8 byte)
//            bpe = 13, //basepointer set (16 byte)
//            bpx = 14, //basepointer set (32 byte)
//
//            pgb = 15, //basepointer get (byte)
//            pgb_s = 16, //basepointer get (byte)
//
//            pgs = 17, //basepointer get (2 byte)
//            pgs_s = 18, //basepointer get (2 byte)
//
//            pgi = 19, //basepointer get (4 byte)
//            pgi_s = 20, //basepointer get (4 byte)
//
//            pgv = 21, //basepointer get (8 byte)
//            pgv_s = 22, //basepointer get (8 byte)
//
//            pge = 23, //basepointer get (16 byte)
//            pge_s = 24, //basepointer get (16 byte)
//
//            pgx = 25, //basepointer get (32 byte)
//            pgx_s = 26, //basepointer get (32 byte)
//
//            pgf = 27, //basepointer get float

        /** Element Stack Pop **/
        map2.put("$ESP+v", 0);
        map2.put("$ESP+e", 1);
        map2.put("$ESP+x", 2);

        map2.put("$EEI", 3);
        map2.put("$EEF", 4);
        map2.put("$EFE", 5);

        /** Element Set Base **/
        map2.put("$ESB+b", 9);
        map2.put("$ESB+s", 10);
        map2.put("$ESB+i", 11);
        map2.put("$ESB+v", 12);
        map2.put("$ESB+e", 13);
        map2.put("$ESB+x", 14);

        /** Element Access Base **/
        map2.put("$EAB+b", 15);
        map2.put("$EAB+b$1", 16);
        map2.put("$EAB+s", 17);
        map2.put("$EAB+s$1", 18);
        map2.put("$EAB+i", 19);
        map2.put("$EAB+i$1", 20);
        map2.put("$EAB+v", 21);
        map2.put("$EAB+v$1", 22);
        map2.put("$EAB+e", 23);
        map2.put("$EAB+e$1", 24);
        map2.put("$EAB+x", 25);
        map2.put("$EAB+x$1", 26);
        map2.put("$EAB+f", 27);
//        map.put("add", 33);
//        map.put("add", 34);
//        map.put("add", 35);
//        map.put("add", 36);
//        map.put("add", 37);
//        map.put("add", 38);
        map.put("halt", 255);
    }

    public static String op(int op)
    {
        for (String opn : map.keySet())
            if (map.get(opn).equals(op))
                return opn;

        return null;
    }

    public static String arg(int op)
    {
        for (String opn : map2.keySet())
            if (map2.get(opn).equals(op))
                return opn;

        return null;
    }

    public OpcodeReader(String string)
    {
        out = new ByteArrayOutputStream();
        funcRegister = new LinkedHashMap<>();
        funcPoint = new LinkedHashMap<>();
        stringMap = new LinkedHashMap<>();

        Matcher matcher = Pattern.compile("\\%\"\\%.*\\%\"\\%").matcher(string);

        while (matcher.find())
        {
            String match = matcher.group();

            string = matcher.replaceFirst("string_" + stringMap.size());
            matcher = Pattern.compile("\\%\"\\%.*\\%\"\\%").matcher(string);

            stringMap.put("string_" + stringMap.size(), match);
        }

        String split_lines[] = string.trim().split("\n");

        string = "";
        for (String line : split_lines)
        {
            line = line.trim().replaceAll("\\#.*", "");

            if (line.startsWith("#"))
                continue;

            string += line + "\n";
        }

        String split[] = string.replaceAll("\n", " ").split("\\s+");

        LinkedList<String> splitter = new LinkedList<>();

        long funcs = 0;

        for (String op : split)
        {
            if (op.endsWith(":"))
                funcRegister.put(op.substring(0, op.length() - 1), funcs ++);

            splitter.push(op);
        }

        Collections.reverse(splitter);

        read(splitter, true);

        out.reset();


        for (String op : split)
        {
            splitter.push(op);
        }

        Collections.reverse(splitter);

        read(splitter, false);

        try{
            out.flush();
            out.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void read(Queue<String> in, boolean first)
    {
        String func = "";
        while (in.size() > 0)
        {
            String op = in.poll();
            if (!first)
//            System.out.println("op_" + op);
//            System.out.println( "\t\t\t-" + (op = in.poll()) + " -" + map.get(op));
            try{
                switch (op)
                {
                    case "swap":
                        add(map.get("swap"));
                        add(UnsignedLong(in.poll()));
                        break;

                    case "add":
                    case "mul":
                    case "sub":
                    case "div":
                        add(map.get(op));
                        op = in.poll();
                        if (map.containsKey(op))
                            add(UnsignedByte(map.get(op) + ""));
                        else
                            add(UnsignedByte(op));
                        break;

                    case "push":
                        add(push);

                        op = in.poll();

                        if (op.contains("."))
                            add(Double(op));
                        else if (op.endsWith("f") || op.endsWith("F") || op.endsWith("d") || op.endsWith("D"))
                            add(Double(op));
                        else
                            add(UnsignedLong(op));
                        break;

                    case "hpush":
                        add(hpush);
                        op = in.poll();
                        add(UnsignedInt(op));
                        break;

                    case "jump":
                        add(jump);
                        op = in.poll();
                        add(UnsignedInt(op));
                        break;

                    case "mark":
                        add(mark);
                        op = in.poll();
                        add(UnsignedShort(op));
                        break;

                    case "goto":
                        add(gto);
                        op = in.poll();
                        add(UnsignedShort(op));
                        break;

                    case "branch":
                    case "if":
                        add(branch);
                        add(UnsignedInt(in.poll()));
                        break;

                    case "malloc":
                    case "new":
                        add(push);
                        add(UnsignedLong(in.poll()));
                        add(m_alloc);
                        break;

                    case "calloc":
                        add(push);
                        add(UnsignedLong(in.poll()));
                        add(push);
                        add(UnsignedLong(in.poll()));
                        add(m_calloc);
                        break;

                    case "mov":
                        add(mov);

                        while (in.peek().startsWith("["))
                        {
                            String unspent = in.poll();
                            unspent = unspent.substring(1, unspent.length() - 1);
//                            String data[] = unspent.split("\\+");

                            System.out.print(" " + unspent);
                            if (map.containsKey(unspent))
                                add(UnsignedByte(map.get(unspent) + ""));
                            else
                                add(UnsignedByte(unspent));
                        }

//                        for (String register : data)
//                            if (map.containsKey(register))
//                                add(UnsignedByte(map.get(register) + ""));
//                            else
//                                add(UnsignedByte(register));
                        break;

                    case "delete":
                        add(m_delete);
                        break;

                    case "compare":
                    case "cmp":
                        add(cmp);
                        if (map.containsKey(op))
                            add(UnsignedByte(map.get(op) + ""));
                        else
                            add(UnsignedByte(op));
                        break;

                    case "invokespecial":
                    case "ispcal":
                    case "invokenative":
                        add(push);
                        if (funcRegister.containsKey((op = in.poll())))
                            add(UnsignedInt(funcRegister.get(op) + ""));
                        else
                            add(UnsignedInt(op));
                        add(ispcal);
                        break;

                    case "invokedynamic":
                    case "idynamic":
                    case "idynmc":
                        add(push);
                        if (funcRegister.containsKey((op = in.poll())))
                            add(UnsignedInt(funcRegister.get(op) + ""));
                        else
                            add(UnsignedInt(op));
                        add(idynmc);
                        break;
                    /**invoke a function (static or otherwise)] p=12 {execute(globalTable, nativeTable, globalPointer, basePointer, scope, globalTable[ops.getUnsignedLong()**/
                    case "invoke":
                    case "call":
                        add(invoke);
                        if (funcRegister.containsKey((op = in.poll())))
//                    {
                            add(UnsignedInt(funcRegister.get(op) + ""));
//                        System.out.println(op + " " + funcRegister.get(op));
//                    }
                        else
                            add(UnsignedInt(op));
                        break;
                    /**push a double**/

                    default:
                        if ((op.equals("ret") || op.contains("mret") || op.contains("mret2") || op.contains("mret4") || op.contains("aret")) && first)
                        {
                            add(map.get(op));
                            funcPoint.put(func, (long) out.size());
                        }
                        else if ((op.equals("ret") || op.contains("mret") || op.contains("mret2") || op.contains("mret4") || op.contains("aret")) && !first)
                            add(map.get(op));
                        else if (op.endsWith(":") && first)
                        {
                            func = op.substring(0, op.length() - 1);
                            add(map.get("func"));
                            add(UnsignedLong("0"));
                        }
                        else if (op.endsWith(":") && !first)
                        {
                            func = op.substring(0, op.length() - 1);
                            add(map.get("func"));
                            add(UnsignedLong(funcPoint.get(func) + ""));
                        }
                        else
//                    if (op.endsWith(":"))
//                    {
//                        add(map.get("func"));
//                        funcRegister.put(op.substring(0, op.length() - 1), (long) funcRegister.size());
                            if (funcRegister.containsKey(op))
                                add(UnsignedLong(funcRegister.get(op) + ""));
                            else
                                add(map.get(op));
                        break;
                }
            } catch (Exception e)
            {
                System.err.println(op);
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    public byte[] MMString(String s)
    {
        String string = stringMap.get(s);

        string = string.substring(3, string.length() - 3);

        ByteBuffer buffer = ByteBuffer.allocate(2 + string.length());

        buffer.putShort((short) string.length());
        for (int i = 0; i < string.length(); i ++)
            buffer.put((byte) string.charAt(i));

        buffer.flip();
        return buffer.array();
    }

    public byte[] Byte(String s)
    {
        long i = Long.parseLong(s);
        ByteBuffer ops = ByteBuffer.allocate(1);

        ops.put((byte) i);

        return ops.array();
    }

    public byte[] UnsignedByte(String s)
    {
        long i = Long.parseLong(s);
        ByteBuffer ops = ByteBuffer.allocate(1);

        ops.put((byte) i);

        return ops.array();
    }

    public byte[] UnsignedShort(String s)
    {
        long i = Long.parseLong(s);
        ByteBuffer ops = ByteBuffer.allocate(2);

        ops.putChar((char) i);

        return ops.array();
    }

    public byte[] UnsignedInt(String s)
    {
        long i = Long.parseLong(s);
        ByteBuffer ops = ByteBuffer.allocate(4);

        ops.putInt((int) i);

        return ops.array();
    }

    public byte[] UnsignedLong(String s)
    {
        long i = Long.parseLong(s);
        ByteBuffer ops = ByteBuffer.allocate(8);

        ops.putLong(i);

        return ops.array();
    }

    public byte[] Float(String s)
    {
        float i = Float.parseFloat(s);
        ByteBuffer ops = ByteBuffer.allocate(4);

        ops.putFloat(i);

        return ops.array();
    }

    public byte[] Double(String s)
    {
        double i = Double.parseDouble(s);
        ByteBuffer ops = ByteBuffer.allocate(8);

        ops.putDouble(i);

        return ops.array();
    }

    public byte[] DoubleDouble(String s)
    {
        double i = Double.parseDouble(s);
        ByteBuffer ops = ByteBuffer.allocate(16);

        ops.putDouble(i);
        ops.putDouble(i);

        return ops.array();
    }

    public byte[] DoubleFloat(String s)
    {
        double i = Double.parseDouble(s);
        ByteBuffer ops = ByteBuffer.allocate(32);

        ops.putDouble(i);
        ops.putDouble(i);
        ops.putDouble(i);
        ops.putDouble(i);

        return ops.array();
    }

    public byte[] LongInt(String s)
    {
        BigInteger i = new BigInteger(s);
        ByteBuffer ops = ByteBuffer.allocate(16);
        String is = i.toString(16);
        while (is.length() < 32)
            is = "0" + is;

        if (is.length() > 32) is = is.substring(0, 32);

        return Base16.decode(is);
    }

    public byte[] UnsignedLongInt(String s)
    {
        BigInteger i = new BigInteger(s);
        ByteBuffer ops = ByteBuffer.allocate(16);
        String is = i.toString(16);
        while (is.length() < 32)
            is = "0" + is;

        if (is.length() > 32) is = is.substring(0, 32);

        return Base16.decode(is);
    }

    public byte[] LongLong(String s)
    {
        BigInteger i = new BigInteger(s);
        ByteBuffer ops = ByteBuffer.allocate(16);
        String is = i.toString(16);
        while (is.length() < 64)
            is = "0" + is;

        if (is.length() > 64) is = is.substring(0, 64);

        return Base16.decode(is);
    }

    public byte[] UnsignedLongLong(String s)
    {
        BigInteger i = new BigInteger(s);
        ByteBuffer ops = ByteBuffer.allocate(16);
        String is = i.toString(16);
        while (is.length() < 64)
            is = "0" + is;

        if (is.length() > 64) is = is.substring(0, 64);

        return Base16.decode(is);
    }

    public byte[] Pointer(String s)
    {
        long i = Long.parseLong(s);
        ByteBuffer ops = ByteBuffer.allocate(8);

        ops.putLong(i);

        return ops.array();
    }

    public byte[] Short(String s)
    {
        long i = Long.parseLong(s);
        ByteBuffer ops = ByteBuffer.allocate(2);

        ops.putChar((char) i);

        return ops.array();
    }

    public byte[] Int(String s)
    {
        long i = Long.parseLong(s);
        ByteBuffer ops = ByteBuffer.allocate(4);

        ops.putInt((int) i);

        return ops.array();
    }

    public byte[] Long(String s)
    {
        long i = Long.parseLong(s);
        ByteBuffer ops = ByteBuffer.allocate(8);

        ops.putLong(i);

        return ops.array();
    }

    public void add(int op)
    {
        try{
            ByteBuffer ops = ByteBuffer.allocate(2);
            ops.putChar((char) op);
            ops.flip();
            out.write(ops.array());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void add(byte ops[])
    {
        try{
            out.write(ops);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public byte[] getStream()
    {
        return out.toByteArray();
    }

    public void write(File file) throws IOException
    {
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(out.toByteArray());
        stream.flush();
        stream.close();
    }
}
