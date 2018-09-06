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

    bconst(38,   "push a byte onto the stack"),
    bconst_0(39, "push a byte onto the stack with value 0"),
    bconst_1(40, "push a byte onto the stack with value 1"),
    bconst_2(41, "push a byte onto the stack with value 2"),
    bconst_3(42, "push a byte onto the stack with value 3"),
    bconst_4(43, "push a byte onto the stack with value 4"),
    bconst_5(44, "push a byte onto the stack with value 5"),
    bconst_e(44, "push a byte onto the stack with no value"),
    bconstrld("load a byte onto the register from input"),
    bconstrld_u("load an unsigned byte onto the register from input"),

    sconst(38,   "push a short onto the stack"),
    sconst_0(39, "push a short onto the stack with value 0"),
    sconst_1(40, "push a short onto the stack with value 1"),
    sconst_2(41, "push a short onto the stack with value 2"),
    sconst_3(42, "push a short onto the stack with value 3"),
    sconst_4(43, "push a short onto the stack with value 4"),
    sconst_5(44, "push a short onto the stack with value 5"),
    sconst_e(44, "push a short onto the stack with no value"),
    sconstrld("load a short onto the register from input"),
    sconstrld_u("load an unsigned short onto the register from input"),

    iconst(38, "push an integer onto the stack"),
    iconst_0(39, "push an integer onto the stack with value 0"),
    iconst_1(40, "push an integer onto the stack with value 1"),
    iconst_2(41, "push an integer onto the stack with value 2"),
    iconst_3(42, "push an integer onto the stack with value 3"),
    iconst_4(43, "push an integer onto the stack with value 4"),
    iconst_5(44, "push an integer onto the stack with value 5"),
    iconst_e(44, "push an integer onto the stack with no value"),
    iload(0,   "load an integer onto the stack from a local variable"),
    iload_0(1, "load an integer onto the stack from a local variable 0"),
    iload_1(2, "load an integer onto the stack from a local variable 1"),
    iload_2(3, "load an integer onto the stack from a local variable 2"),
    iload_3(4, "load an integer onto the stack from a local variable 3"),
    iconstrld("load a integer onto the register from input"),
    iconstrld_u("load an unsigned integer onto the register from input"),

    lconst(38,   "push a long onto the stack"),
    lconst_0(39, "push a long onto the stack with value 0"),
    lconst_1(40, "push a long onto the stack with value 1"),
    lconst_2(41, "push a long onto the stack with value 2"),
    lconst_3(42, "push a long onto the stack with value 3"),
    lconst_4(43, "push a long onto the stack with value 4"),
    lconst_5(44, "push a long onto the stack with value 5"),
    lconst_e(44, "push a long onto the stack with no value"),
    lconstrld("load a long onto the register from input"),
    lconstrld_u("load an unsigned long onto the register from input"),

    liconst(38,   "push a 128bit integer onto the stack"),
    liconst_0(39, "push a 128bit integer onto the stack with value 0"),
    liconst_1(40, "push a 128bit integer onto the stack with value 1"),
    liconst_2(41, "push a 128bit integer onto the stack with value 2"),
    liconst_3(42, "push a 128bit integer onto the stack with value 3"),
    liconst_4(43, "push a 128bit integer onto the stack with value 4"),
    liconst_5(44, "push a 128bit integer onto the stack with value 5"),
    liconst_e(44, "push a 128bit integer onto the stack with no value"),
    liconstrld("load a 128bit integer onto the register from input"),
    liconstrld_u("load an unsigned 128bit integer onto the register from input"),

    llconst(38,   "push a 256bit integer onto the stack"),
    llconst_0(39, "push a 256bit integer onto the stack with value 0"),
    llconst_1(40, "push a 256bit integer onto the stack with value 1"),
    llconst_2(41, "push a 256bit integer onto the stack with value 2"),
    llconst_3(42, "push a 256bit integer onto the stack with value 3"),
    llconst_4(43, "push a 256bit integer onto the stack with value 4"),
    llconst_5(44, "push a 256bit integer onto the stack with value 5"),
    llconst_e(44, "push a 256bit integer onto the stack with no value"),
    llconstrld("load a 256bit integer onto the register from input"),
    llconstrld_u("load an unsigned 256bit integer onto the register from input"),

    fconst(45, "push a float onto the stack"),
    fconst_0(46, "push a float onto the stack with value 0"),
    fconst_1(47, "push a float onto the stack with value 1"),
    fconst_2(48, "push a float onto the stack with value 2"),
    fconst_3(49, "push a float onto the stack with value 3"),
    fconst_4(50, "push a float onto the stack with value 4"),
    fconst_5(51, "push a float onto the stack with value 5"),
    fconst_e(52, "push a float onto the stack with no value"),
    fload(0,   "load a float onto the stack from a local variable"),
    fload_0(1, "load a float onto the stack from a local variable 0"),
    fload_1(2, "load a float onto the stack from a local variable 1"),
    fload_2(3, "load a float onto the stack from a local variable 2"),
    fload_3(4, "load a float onto the stack from a local variable 3"),
    fconstrld("load a long onto the register from input"),
    fconstrld_u("load an unsigned long onto the register from input"),

    dconst(45,   "push a double onto the stack"),
    dconst_0(46, "push a double onto the stack with value 0"),
    dconst_1(47, "push a double onto the stack with value 1"),
    dconst_2(48, "push a double onto the stack with value 2"),
    dconst_3(49, "push a double onto the stack with value 3"),
    dconst_4(50, "push a double onto the stack with value 4"),
    dconst_5(51, "push a double onto the stack with value 5"),
    dconst_e(52, "push a double onto the stack with no value"),
    dconstrld("load a long onto the register from input"),
    dconstrld_u("load an unsigned long onto the register from input"),

    dfconst(45,   "push a 128bit double onto the stack"),
    dfconst_0(46, "push a 128bit double onto the stack with value 0"),
    dfconst_1(47, "push a 128bit double onto the stack with value 1"),
    dfconst_2(48, "push a 128bit double onto the stack with value 2"),
    dfconst_3(49, "push a 128bit double onto the stack with value 3"),
    dfconst_4(50, "push a 128bit double onto the stack with value 4"),
    dfconst_5(51, "push a 128bit double onto the stack with value 5"),
    dfconst_e(52, "push a 128bit double onto the stack with no value"),
    dfconstrld("load a long onto the register from input"),
    dfconstrld_u("load an unsigned long onto the register from input"),

    ddconst(45,   "push a 256bit double onto the stack"),
    ddconst_0(46, "push a 256bit double onto the stack with value 0"),
    ddconst_1(47, "push a 256bit double onto the stack with value 1"),
    ddconst_2(48, "push a 256bit double onto the stack with value 2"),
    ddconst_3(49, "push a 256bit double onto the stack with value 3"),
    ddconst_4(50, "push a 256bit double onto the stack with value 4"),
    ddconst_5(51, "push a 256bit double onto the stack with value 5"),
    ddconst_e(52, "push a 256bit double onto the stack with no value"),
    ddconstrld("load a long onto the register from input"),
    ddconstrld_u("load an unsigned long onto the register from input"),

    csconst(45,   "push a string onto the stack"),
    csconst_e(52, "push a string onto the stack with no value"),

    aself(53, "set 'this'"),
    pop("pop the top value from the stack"),
    pop2("pop the top two values from the stack"),

    bstore,
    sstore,

    istore("store an integer value into a local variable"),
    istore_0("store an integer value into a local variable 0"),
    istore_1("store an integer value into a local variable 1"),
    istore_2("store an integer value into a local variable 2"),
    istore_3("store an integer value into a local variable 3"),

    lstore,
    listore,
    llstore,

    fstore("store a float value into a local variable"),
    fstore_0("store a float value into a local variable 0"),
    fstore_1("store a float value into a local variable 1"),
    fstore_2("store a float value into a local variable 2"),
    fstore_3("store a float value into a local variable 3"),

    dstore,
    dfstore,
    ddstore,

//    bset("set current object"),
//    bastore("store a reference into object"),
//    bistore("store an integer into object"),
//    bfstore("store a float into object"),

    anew("create a new heap array"),
    adel("delete a heap array"),
    amov("move a reference from heap data onto stack"),
    bmov,
    smov,
    imov("move an integer from heap data onto stack"),
    lmov,
    limov,
    llmov,
    fmov("move a float from heap data onto stack"),
    dmov,
    dfmov,
    ddmov,
    csmov,
    mload_a("pop a reference from stack into a heap array"),
    mload_i("pop an integer from stack into a heap array"),
    mload_f("pop an integer from stack into a heap array"),

    mregi("load an integer onto the method argument register"),
    mregf("load a float onto the method argument register"),
    mrega("load a reference onto the method argument register"),
    mregi_0,
    mregi_1,
    mregi_2,
    mregi_3,
    mregf_0,
    mregf_1,
    mregf_2,
    mregf_3,
    mrega_0,
    mrega_1,
    mrega_2,
    mrega_3,
    ptb("store a byte into a heap variable 'this'"),
    pts("store a short into a heap variable 'this'"),
    pti("store an integer into a heap variable 'this'"),
    ptl("store a long into a heap variable 'this'"),
    ptli("store a 128bit integer into a heap variable 'this'"),
    ptll("store a 256bit integer into a heap variable 'this'"),
    ptf("store a float into a heap variable 'this'"),
    ptd("store a double into a heap variable 'this'"),
    ptdf("store a 128bit float into a heap variable 'this'"),
    ptdd("store a 256bit float into a heap variable 'this'"),
    pta("store a reference into a heap variable 'this'"),
    ptcs("store a string into a heap variable 'this'"),

    btb("store a byte into a heap variable"),
    bts("store a short into a heap variable"),
    bti("store an integer into a heap variable"),
    btl("store a long into a heap variable"),
    btli("store a 128bit integer into a heap variable"),
    btll("store a 256bit integer into a heap variable"),
    btf("store a float into a heap variable"),
    btd("store a double into a heap variable"),
    btdf("store a 128bit float into a heap variable"),
    btdd("store a 256bit float into a heap variable"),
    bta("store a reference into a heap variable"),
    btcs("store a string into a heap variable"),

    slp("force the virtual machine to sleep"),
    halt("halt the virtual machine"),
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
