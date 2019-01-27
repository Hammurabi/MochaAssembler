package mochaxx.compiler.structure;

import mochaxx.compiler.OpcodeReader;
import mochaxx.compiler.Opcodes;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static mochaxx.compiler.OpcodeReader.*;
import static mochaxx.compiler.Opcodes.*;

public class OpcodeStream
{
    private StringBuilder builder;
    private StringBuilder linebuilder;
    private Map<String, Long>           marks;

    public OpcodeStream()
    {
        builder = new StringBuilder();
        linebuilder = new StringBuilder();
        marks = new HashMap<>();
    }

    public void write(String string)
    {
        builder.append(string).append("\n");
    }

    public void op_add(String comment)
    {
        op_add(0, comment);
    }
    public void op_add(int arg, String comment)
    {
        _op(add, arg(arg), comment);
    }

    public void op_sub(int arg, String comment)
    {
        _op(sub, arg(arg), comment);
    }

    public void op_mul(int arg, String comment)
    {
        _op(mul, arg(arg), comment);
    }

    public void op_div(int arg, String comment)
    {
        _op(div, arg(arg), comment);
    }

    public void op_swap(long with, String comment)
    {
        _op(swap, " " + with, comment);
    }

    public void _op(int op, String args, String comment)
    {
        linebuilder.delete(0, linebuilder.length());
        linebuilder.append("\t").append(op(op)).append(" ").append(args);

        int length = 75 - linebuilder.length();
        for (int i = 0; i < length; i ++)
            linebuilder.append(" ");
        linebuilder.append("#").append(comment).append("\n");

        builder.append(linebuilder);
    }

    public void op_func(String name)
    {
        builder.append(name).append(":\n");
    }

    public void op_mov(int i[], String comment)
    {
        String args = "";

        for (int arg : i)
            if (arg(arg) != null)
                args += "[" + arg(arg) + "] ";
            else args += "[" + arg + "] ";

        if (args.length() > 0)
            args = args.substring(0, args.length() - 1);

        _op(mov, args, comment);
    }

    public void op_mov(String i[], String comment)
    {
        String args = "";

        for (String arg : i)
            args += arg + " ";

        if (args.length() > 0)
            args = args.substring(0, args.length() - 1);

        _op(mov, args, comment);
    }

    public void op_str(String data)
    {
        _op(m_alloc, (data.length() + 8) + "", data);
    }

    public void op_psh(String data)
    {
        op_psh(data, "");
    }
    public void op_psh(String data, String comment)
    {
        if (data.equals("0"))
            _op(push_0, "", comment.length() == 0 ? "push an int with value 0" : comment);
        else if (data.equals("1"))
            _op(push_1, "", comment.length() == 0 ? "push an int with value 1" : comment);
        else _op(push, data, comment.length() == 0 ? "push an int with value '" + data + "'" : comment);
    }

    public void op_dup(String comment)
    {
        _op(dup, "", comment);
    }

    public String convertDoubleToStringInt(String d)
    {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putDouble(Double.parseDouble(d));
        buffer.flip();

        return buffer.getLong() + "";
    }

    @Override
    public String toString()
    {
        return builder.toString();
    }

    public void op_mark(String mark)
    {
        _op(Opcodes.mark, marks.size() + "", "mark for scope: " + mark);
        marks.put(mark, (long) marks.size());
    }

    public void op_goto(String mark)
    {
        _op(gto, String.valueOf(marks.get(mark)), "goto scope mark: " + mark);
    }

    public void op_cmpless(String args, String comment)
    {
        _op(cmp, args, comment);
    }

    public void op_branch(int skip)
    {
        _op(branch,  String.valueOf(skip), "branch if true");
    }

    public int length()
    {
        OpcodeReader reader = new OpcodeReader(toString());
        return reader.getStream().length;
    }

    public void write(OpcodeStream newstream)
    {
        write(newstream.toString());
    }

    public void op_cpy(int item, String comment)
    {
        _op(scpy, item + "", comment);
    }

    public void op_ivk(int i, String data)
    {
        _op(invoke, String.valueOf(i), data);
    }

    public void op_memcpy(long length)
    {
        _op(m_memcpy, String.valueOf(length), "");
    }

    public void op_malloc(int i)
    {
        _op(m_alloc, String.valueOf(i), "");
    }

    public void op_new(int i)
    {
        _op(m_alloc, String.valueOf(i), "");
    }
}