package com.riverssen.core.mpp.compiler;

/** Object oriented opcodes inspired by the java vm's own opcodes with a little twist. **/
public enum Ops
{
    //load a reference onto the stack from a local variable #index
    aload(0, "load a reference onto the stack from a local variable"),
    aload_0(1, "load a reference onto the stack from a local variable 0"),
    aload_1(2, "load a reference onto the stack from a local variable 1"),
    aload_2(3, "load a reference onto the stack from a local variable 2"),
    aload_3(4, "load a reference onto the stack from a local variable 3"),
//    aaload(29, "load a reference onto the stack from a local object"),
    //load null reference to stack
    aconst_null(5, "push a null reference onto the stack"),
    aconst_new("push a new object onto the stack"),

    //store a reference into local variable #index
    astore(6, "store a reference into a local variable"),
    astore_0(7, "store a reference into a local variable 0"),
    astore_1(8, "store a reference into a local variable 1"),
    astore_2(9, "store a reference into a local variable 2"),
    astore_3(10, "store a reference into a local variable 3"),
    astore_null(11, "store a null reference into a local variable"),

    //load an integer onto the register from a local variable #index #size_in_bytes #cast_type
    iregld(12, "load an integer onto the register from a local variable"),
    iregld_0(13, "load an integer onto the register from a local variable 0"),
    iregld_1(14, "load an integer onto the register from a local variable 1"),
    iregld_2(15, "load an integer onto the register from a local variable 2"),
    iregld_3(16, "load an integer onto the register from a local variable 3"),
    iload(0,   "load an integer onto the stack from a local variable"),
    iload_0(1, "load an integer onto the stack from a local variable 0"),
    iload_1(2, "load an integer onto the stack from a local variable 1"),
    iload_2(3, "load an integer onto the stack from a local variable 2"),
    iload_3(4, "load an integer onto the stack from a local variable 3"),
    fload(0,   "load a float onto the stack from a local variable"),
    fload_0(1, "load a float onto the stack from a local variable 0"),
    fload_1(2, "load a float onto the stack from a local variable 1"),
    fload_2(3, "load a float onto the stack from a local variable 2"),
    fload_3(4, "load a float onto the stack from a local variable 3"),

    //load a float onto the register from a local variable #index #size_in_bytes #cast_type
    fregld(17, "load a float onto the register from a local variable"),
    fregld_0(18, "load a float onto the register from a local variable 0"),
    fregld_1(19, "load a float onto the register from a local variable 1"),
    fregld_2(20, "load a float onto the register from a local variable 2"),
    fregld_3(21, "load a float onto the register from a local variable 3"),

    //load an integer onto the register from data #size_in_bytes #data_bytes* #cast_type
    iregst(22, "load an integer onto the register from data"),
    fregst(23, "load a float onto the register from data"),

    mul(24, "perform a multiplication on the register"),
    div(25, "perform a multiplication on the register"),
    sub(26, "perform a multiplication on the register"),
    add(27, "perform a multiplication on the register"),
    mod(28, "perform a multiplication on the register"),
//    aastore(30, "store a reference into a local object"),
    iiregld(31, "load an integer onto the register from a local object"),
    ffregld(32, "load a float onto the register from a local object"),

    areturn(33, "return a reference from a method"),
    ireturn(34, "return an integer from a method"),
    freturn(35, "return a float from a method"),
    returnvoid("return void"),
    returnnull("return null"),

    invoke(36, "invoke a method"),
    invokentv(37, "invoke a native method"),

    iconst(38, "push an integer onto the stack"),
    iconst_0(39, "push an integer onto the stack with value 0"),
    iconst_1(40, "push an integer onto the stack with value 1"),
    iconst_2(41, "push an integer onto the stack with value 2"),
    iconst_3(42, "push an integer onto the stack with value 3"),
    iconst_4(43, "push an integer onto the stack with value 4"),
    iconst_5(44, "push an integer onto the stack with value 5"),
    iconst_e(44, "push an integer onto the stack with no value"),

    fconst(45, "push a float onto the stack"),
    fconst_0(46, "push a float onto the stack with value 0"),
    fconst_1(47, "push a float onto the stack with value 1"),
    fconst_2(48, "push a float onto the stack with value 2"),
    fconst_3(49, "push a float onto the stack with value 3"),
    fconst_4(50, "push a float onto the stack with value 4"),
    fconst_5(51, "push a float onto the stack with value 5"),
    fconst_e(52, "push a float onto the stack with no value"),

    aself(53, "set 'this'"),
    pop("pop the top value from the stack"),
    pop2("pop the top two values from the stack"),

    istore("store an integer value into a local variable"),
    istore_0("store an integer value into a local variable 0"),
    istore_1("store an integer value into a local variable 1"),
    istore_2("store an integer value into a local variable 2"),
    istore_3("store an integer value into a local variable 3"),

    fstore("store a float value into a local variable"),
    fstore_0("store a float value into a local variable 0"),
    fstore_1("store a float value into a local variable 1"),
    fstore_2("store a float value into a local variable 2"),
    fstore_3("store a float value into a local variable 3"),

//    bset("set current object"),
//    bastore("store a reference into object"),
//    bistore("store an integer into object"),
//    bfstore("store a float into object"),

    anew("create a new heap array"),
    adel("delete a heap array"),
    amov("move a reference from heap data onto stack"),
    imov("move an integer from heap data onto stack"),
    fmov("move a float from heap data onto stack"),
    mload_a("pop a reference from stack into a heap array"),
    mload_i("pop an integer from stack into a heap array"),
    mload_f("pop an integer from stack into a heap array"),
    ;
//    sin,
//    cos,
//    tan

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
        return super.toString() + " " + this.desc;
    }
}
