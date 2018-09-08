package com.riverssen.core.mpp.compiler;

import com.riverssen.core.mpp.Executable;
import com.riverssen.core.utils.Handler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Opcode
{
    private static final int jump = 20;
    private int             code;
    private Ops             ops;
    private String          description;
    private List<Opcode>    children;

    public Opcode(int code, String description)
    {
        this.ops = null;
        this.code = code;
        this.description = description;
        this.children = new ArrayList<>();
    }

    public Opcode(String description)
    {
        this.ops = null;
        this.code = -300;
        this.description = description;
        this.children = new ArrayList<>();
    }

    public Opcode(Ops code)
    {
        this.ops = code;
        this.code = code.get();
        this.description = code.getDesc();
        this.children = new ArrayList<>();
    }

    public Opcode(Ops code, String description)
    {
        this.ops = code;
        this.code = code.get();
        this.description = description;
        this.children = new ArrayList<>();
    }

    public static List<Opcode> convertLong(long integer)
    {
        ByteBuffer _int_ = ByteBuffer.allocate(8);
        _int_.putLong(integer);
        _int_.flip();

        List<Opcode> list = new ArrayList<>();
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));

        return list;
    }

    public static List<Opcode> convertInteger(long integer)
    {
        ByteBuffer _int_ = ByteBuffer.allocate(4);
        _int_.putInt((int) integer);
        _int_.flip();

        List<Opcode> list = new ArrayList<>();
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));

        return list;
    }

    public static List<Opcode> convertDouble(double integer)
    {
        ByteBuffer _int_ = ByteBuffer.allocate(8);
        _int_.putDouble(integer);
        _int_.flip();

        List<Opcode> list = new ArrayList<>();
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));

        return list;
    }

    public static List<Opcode> convertFloat(double integer)
    {
        ByteBuffer _int_ = ByteBuffer.allocate(4);
        _int_.putFloat((float) integer);
        _int_.flip();

        List<Opcode> list = new ArrayList<>();
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));

        return list;
    }

    public static List<Opcode> convertShort(long integer)
    {
        ByteBuffer _int_ = ByteBuffer.allocate(2);
        _int_.putShort((short) integer);
        _int_.flip();

        List<Opcode> list = new ArrayList<>();
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));
        list.add(new Opcode(Byte.toUnsignedInt(_int_.get()), "uint8_t"));

        return list;
    }

    public static List<Opcode> convertByte(long integer)
    {
        List<Opcode> list = new ArrayList<>();
        list.add(new Opcode(Byte.toUnsignedInt((byte) integer), "uint8_t"));

        return list;
    }

    public static List<Opcode> convertBytes(byte... bytes)
    {
        List<Opcode> list = new ArrayList<>();
        for (byte b : bytes)
            list.add(new Opcode(Byte.toUnsignedInt(b), "uint8_t"));

        return list;
    }

    public static List<Opcode> to40bitPointer(long unsigned_long_pointer)
    {
        List<Opcode> bytes = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.allocate(8);

        buffer.putLong(unsigned_long_pointer);
        buffer.flip();

        buffer.get();
        buffer.get();
        buffer.get();
        bytes.add(new Opcode(Byte.toUnsignedInt(buffer.get()), "uint8_t"));
        bytes.add(new Opcode(Byte.toUnsignedInt(buffer.get()), "uint8_t"));
        bytes.add(new Opcode(Byte.toUnsignedInt(buffer.get()), "uint8_t"));
        bytes.add(new Opcode(Byte.toUnsignedInt(buffer.get()), "uint8_t"));
        bytes.add(new Opcode(Byte.toUnsignedInt(buffer.get()), "uint8_t"));

        return bytes;
    }

    public Opcode add(Opcode opcode)
    {
        if (opcode == null) return this;

        this.children.add(opcode);
        return this;
    }

    public Opcode add(List<Opcode> opcode)
    {
        this.children.addAll(opcode);
        return this;
    }

    public String humanReadable(int i)
    {
        return this.humanReadable(i, false);
    }

    public String humanReadable(int i, boolean m)
    {
        return this.humanReadable(i, m, new Handler<>(0));
    }

    public String humanReadable(int i, boolean m,  Handler<Integer> line)
    {
        String t = line + ": ";

        if (code >= -256)
        {
            if (ops != null) line.set(line.get() + 2);
            else line.set(line.get() + 1);
        }

        if (i >= 0)
        for (int x = 0; x < i; x ++)
            t += "\t";

//        if (m && code >= -256)
//            t += toString();
//        else if (!m)
//            t += toString();

        if (code < -256)
            t = "";
        else t += toString() + "\n";

        for (Opcode opcode : children)
            t += opcode.humanReadable((i >= 0 ? (i + 1) : -1), m, line);

        return t;
    }

    private String space(int i)
    {
        String s = "";

        for (int x = 0; x < i; x ++)
            s += " ";

        return s;
    }

    @Override
    public String toString()
    {
        String first_part = (code < 0 ? "~" : (ops != null ? ops.name() + " " : "") + String.format("0x%x", code));
        return first_part + space(jump - first_part.length()) + description;
    }

    public Opcode add(Opcode ...opcodes)
    {
        for (Opcode opcode : opcodes)
            add(opcode);

        return this;
    }

    public Executable toExecutable()
    {
        Executable executable = new Executable();

        if (code >= -256) {
            if (ops != null)
                executable.add(executable.convertShort(code));
            else executable.add(code);
        }

        for (Opcode opcode : children)
            executable.add(opcode.toExecutable().op_codes);

        return executable;
    }

    public List<Opcode> getChildren()
    {
        return children;
    }
}
