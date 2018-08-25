package com.riverssen.core.mpp.compiler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Opcode
{
    private int             code;
    private String          description;
    private List<Opcode>    children;

    public Opcode(int code, String description)
    {
        this.code = code;
        this.description = description;
        this.children = new ArrayList<>();
    }

    public static List<Opcode> convertLong(long integer)
    {
        ByteBuffer _int_ = ByteBuffer.allocate(8);
        _int_.putLong(integer);
        _int_.flip();

        List<Opcode> list = new ArrayList<>();
        list.add(new Opcode(_int_.get(), "uint8_t"));
        list.add(new Opcode(_int_.get(), "uint8_t"));
        list.add(new Opcode(_int_.get(), "uint8_t"));
        list.add(new Opcode(_int_.get(), "uint8_t"));
        list.add(new Opcode(_int_.get(), "uint8_t"));
        list.add(new Opcode(_int_.get(), "uint8_t"));
        list.add(new Opcode(_int_.get(), "uint8_t"));
        list.add(new Opcode(_int_.get(), "uint8_t"));

        return list;
    }

    public Opcode add(Opcode opcode)
    {
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
        String t = "";

        for (int x = 0; x < i; x ++)
            t += "\t";

        t += toString();

        for (Opcode opcode : children)
            t += "\n" + opcode.humanReadable(i + 1);

        return t;
    }

    @Override
    public String toString()
    {
        return (code < 0 ? "~" : String.format("0x%x", code)) + " " + description;
    }

    public Opcode add(Opcode ...opcodes)
    {
        for (Opcode opcode : opcodes)
            add(opcode);

        return this;
    }
}
