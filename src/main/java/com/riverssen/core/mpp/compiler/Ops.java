package com.riverssen.core.mpp.compiler;

/** Object oriented opcodes inspired by the java vm's own opcodes with a little twist. **/
public enum Ops
{

    ;
    
    private static int i;
    private String desc;
    private int    opn;

    private Ops(){this("-");}

    private Ops(String data)
    {
        this(-1, data);
//        this.opn  = instructions.i ++;
//        this.desc = data;
    }

    private Ops(int op, String data)
    {
        this.opn  = instructions.i ++;
        this.desc = data;
    }

    public String getDesc()
    {
        return desc;
    }

    public int get()
    {
        return opn;
    }

    @Override
    public String toString()
    {
        return super.toString();/** + " " + this.desc;**/
    }
}
