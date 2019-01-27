package mochaxx.compiler;

public class Opcodes
{
    public static int
            func = 256,
    /**
     * Correct usage
     * [ARITHMETIC_OP] [SIGN BITS]
     */
    add = 0,
            mul = 1,
            sub = 2,
            div = 3,

    /**
     * NO OPS
     **/
    sif = 0, //SIGNED INT
            uif = 1, //UNSIGNED INT
            nif = 2, //NON INT

    /**
     * Invoke a method
     */
    invoke = 4,
    /**
     * Invoke by pointer (programmatic).
     */
    idynmc = 5,
    /**
     * Invoke a native function
     */
    ispcal = 6,
    /**
     * push a number (takes any 64 bit value as argument)
     */
    push = 7,
    /**
     * push a 0
     */
    push_0 = 8,
    /**
     * push a 1
     */
    push_1 = 9,

    ret = 10, //return null
            mret = 11, //return single stack element

    /**
     * Correct usage
     * [ARITHMETIC_OP] [SIGN BITS]
     */
    mod = 12,
    /**
     * Correct usage:
     * [ARITHMETIC_OP]
     */
    and_ = 13,
            or_ = 14,
            xor_ = 15,
            not_ = 16,
            rshift = 17,
            lshift = 18,

    mret2 = 19, //return 2 stack elements
            mret4 = 20, //return 4 stack elements

    hpush = 21, //half-int push (short)
            pop = 22, //pop an element from the stack

    cast = 23, //cast from one type to the other.
            cast_itf = 24, //cast an element from the stack (long) to a double
            cast_fti = 25, //cast an element from the stack (double) to a long

    jump = 26,
            mark = 27,
            gto = 28,
            cmp = 29, //compare less equal more lessequal moreequal

    let = -1,
            eql = 0,
            grt = 1,
            lse = 2,
            gte = 3,


    branch = 30, //branch if true

    /**
     * correct usage:
     * mov [type] [register]
     */
    mov = 31,

    dup = 32,

    load = 33,
            store = 34,
            m_alloc = 35,
            m_calloc = 36,
            m_delete = 37,

    swap = 38,
    scpy = 39,
    m_memcpy = 40,

    halt = 255;
}