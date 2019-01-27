package mochaxx;

import java.util.LinkedHashMap;
import java.util.Map;

public class OpcodeGenerator
{
    private static interface Operation{
        String getOperation(String... args);
    }

    public static void generateOpcodes(boolean genNumbers)
    {
        /**
         * List of all possible native types
         */

        Map<String, String> typesAndPrefixes = new LinkedHashMap<>();
        typesAndPrefixes.put("b",   "byte");
        typesAndPrefixes.put("s",   "short");
        typesAndPrefixes.put("i",   "int");
        typesAndPrefixes.put("l",   "long");
        typesAndPrefixes.put("f",   "float");
        typesAndPrefixes.put("d",   "double");
        typesAndPrefixes.put("df",  "double float");
        typesAndPrefixes.put("dd",  "double double");
        typesAndPrefixes.put("li",  "long int");
        typesAndPrefixes.put("ll",  "long long");
        typesAndPrefixes.put("a",   "pointer");

        Map<String, String> opsPerType = new LinkedHashMap<>();
        Map<String, Operation> OpsPerType = new LinkedHashMap<>();

//        OpsPerType.put("const",         (args)->{ return "stack.push%st_a(ops.get%st_a);"; });
//        OpsPerType.put("const_0",       (args)->{ return "stack.push%st_a(0);" ;});
//        OpsPerType.put("const_1",       (args)->{ return "stack.push%st_a(1);" ;});
//        OpsPerType.put("const_2",       (args)->{ return "stack.push%st_a(2);" ;});
//        OpsPerType.put("const_3",       (args)->{ return "stack.push%st_a(3);" ;});
//        OpsPerType.put("const_4",       (args)->{ return "stack.push%st_a(4);" ;});
//        OpsPerType.put("const_5",       (args)->{ return "stack.push%st_a(5);" ;});
//        OpsPerType.put("const_6",       (args)->{ return "stack.push%st_a(6);" ;});
//        OpsPerType.put("const_9",       (args)->{ return "stack.push%st_a(9);" ;});
//        OpsPerType.put("const_10",      (args)->{ return "stack.push%st_a(10);"; });
//        OpsPerType.put("const_11",      (args)->{ return "stack.push%st_a(11);"; });
//        OpsPerType.put("const_12",      (args)->{ return "stack.push%st_a(12);"; });
//
//        OpsPerType.put("load",      (args)->{ return "stack.push(lvt[ops.getUnsignedInt40().l].%lvtt_a)"; });
//        OpsPerType.put("load_0",    (args)->{ return "stack.push(lvt[0].%lvtt_a)"; });
//        OpsPerType.put("load_1",    (args)->{ return "stack.push(lvt[1].%lvtt_a)"; });
//        OpsPerType.put("load_2",    (args)->{ return "stack.push(lvt[2].%lvtt_a)"; });
//        OpsPerType.put("load_3",    (args)->{ return "stack.push(lvt[3].%lvtt_a)"; });
//        OpsPerType.put("load_4",    (args)->{ return "stack.push(lvt[4].%lvtt_a)"; });
//
//        OpsPerType.put("store",     (args)->{ return "lvt[ops.getUnsignedInt40().l] = lve_%t(stack.pop%st());"; });
//        OpsPerType.put("store_0",   (args)->{ return "lvt[0] = lve_%t(stack.pop%st());"; });
//        OpsPerType.put("store_1",   (args)->{ return "lvt[1] = lve_%t(stack.pop%st());"; });
//        OpsPerType.put("store_2",   (args)->{ return "lvt[2] = lve_%t(stack.pop%st());"; });
//        OpsPerType.put("store_3",   (args)->{ return "lvt[3] = lve_%t(stack.pop%st());"; });
//        OpsPerType.put("store_4",   (args)->{ return "lvt[4] = lve_%t(stack.pop%st());"; });
//
//        OpsPerType.put("dup",       (args)->{ return "stack.push%st_a(stack.peek%st_a());"; });
//        OpsPerType.put("dup2",      (args)->{ return "stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a());"; });
//        OpsPerType.put("dup3",      (args)->{ return "stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a());"; });
//        OpsPerType.put("dup4",      (args)->{ return "stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a());"; });
//        OpsPerType.put("dup5",      (args)->{ return "stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a());"; });
//        OpsPerType.put("dup6",      (args)->{ return "stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a());"; });
//        OpsPerType.put("dup10",     (args)->{ return "stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a()); stack.push%st_a(stack.peek%st_a());"; });
//
//        OpsPerType.put("set", (args)->{ return "accessMemoryAndSet%tr(base, ops.getUnsignedInt(), stack.pop());"; });
//        OpsPerType.put("get", "get a %t from a field on base pointer to stack.");
//
//        OpsPerType.put("vset", "set a %t from value into a field on base pointer.");
//        OpsPerType.put("sget", "get a %t from a field on base pointer to stack using address from stack.");
//        OpsPerType.put("sset", "set a %t from stack into a field on base pointer using address from stack.");
//
//        OpsPerType.put("setl", "set a %t from local variable into a field on base pointer.");
//
//        OpsPerType.put("return", "return a %t into the main stack.");

        opsPerType.put("const",     "push a const %t into the stack.");
        opsPerType.put("const_0",   "push a const %t into the stack (value = 0).");
        opsPerType.put("const_1",   "push a const %t into the stack (value = 1).");
        opsPerType.put("const_2",   "push a const %t into the stack (value = 2).");
        opsPerType.put("const_3",   "push a const %t into the stack (value = 3).");
        opsPerType.put("const_4",   "push a const %t into the stack (value = 4).");
        opsPerType.put("const_5",   "push a const %t into the stack (value = 5).");
        opsPerType.put("const_6",   "push a const %t into the stack (value = 6).");
        opsPerType.put("const_9",   "push a const %t into the stack (value = 9).");
        opsPerType.put("const_10",   "push a const %t into the stack (value = 10).");
        opsPerType.put("const_11",   "push a const %t into the stack (value = 11).");
        opsPerType.put("const_12",   "push a const %t into the stack (value = 12).");

        opsPerType.put("load",   "load a %t into the stack from local variable.");
        opsPerType.put("load_0",   "load a %t into the stack from local variable.");
        opsPerType.put("load_1",   "load a %t into the stack from nth local variable.");
        opsPerType.put("load_2",   "load a %t into the stack from nth local variable.");
        opsPerType.put("load_3",   "load a %t into the stack from nth local variable.");
        opsPerType.put("load_4",   "load a %t into the stack from nth local variable.");

        opsPerType.put("store",     "store a %t from stack into local variable.");
        opsPerType.put("store_0",   "store a %t from stack into local variable.");
        opsPerType.put("store_1",   "store a %t from stack into nth local variable.");
        opsPerType.put("store_2",   "store a %t from stack into nth local variable.");
        opsPerType.put("store_3",   "store a %t from stack into nth local variable.");
        opsPerType.put("store_4",   "store a %t from stack into nth local variable.");

        opsPerType.put("dup", "duplicate a %t on the stack.");
        opsPerType.put("dup2", "duplicate a %t on the stack 2 times.");
        opsPerType.put("dup3", "duplicate a %t on the stack 3 times.");
        opsPerType.put("dup4", "duplicate a %t on the stack 4 times.");
        opsPerType.put("dup5", "duplicate a %t on the stack 5 times.");
        opsPerType.put("dup6", "duplicate a %t on the stack 6 times.");
        opsPerType.put("dup10", "duplicate a %t on the stack 10 times.");

        opsPerType.put("set", "set a %t from stack into a field on base pointer.");
        opsPerType.put("get", "get a %t from a field on base pointer to stack.");

        opsPerType.put("vset", "set a %t from value into a field on base pointer.");
        opsPerType.put("sget", "get a %t from a field on base pointer to stack using address from stack.");
        opsPerType.put("sset", "set a %t from stack into a field on base pointer using address from stack.");

        opsPerType.put("setl", "set a %t from local variable into a field on base pointer.");

        opsPerType.put("return", "return a %t into the main stack.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("cast_" + prefix, "cast %t to type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("cast_u" + prefix, "cast %t to type 'unsigned " + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("ucast_u" + prefix, "cast unsigned %t to type 'unsigned " + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("ucast_" + prefix, "cast unsigned %t to type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("add_" + prefix, "add %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("sub_" + prefix, "sub %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("mul_" + prefix, "multiply %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("div_" + prefix, "divide %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("mod_" + prefix, "modulo %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("and_" + prefix, "and %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("or_" + prefix, "or %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("xor_" + prefix, "xor %t with type '" + typesAndPrefixes.get(prefix) + "'.");


        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_add_" + prefix, "add %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_sub_" + prefix, "sub %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_mul_" + prefix, "multiply %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_div_" + prefix, "divide %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_mod_" + prefix, "modulo %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_and_" + prefix, "and %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_or_" + prefix, "or %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_xor_" + prefix, "xor %t with type '" + typesAndPrefixes.get(prefix) + "'.");


        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_add_u" + prefix, "add %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_sub_u" + prefix, "sub %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_mul_u" + prefix, "multiply %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_div_u" + prefix, "divide %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_mod_u" + prefix, "modulo %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_and_u" + prefix, "and %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_or_u" + prefix, "or %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("u_xor_u" + prefix, "xor %t with type '" + typesAndPrefixes.get(prefix) + "'.");



        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("add_u" + prefix, "add %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("sub_u" + prefix, "sub %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("mul_u" + prefix, "multiply %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("div_u" + prefix, "divide %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("mod_u" + prefix, "modulo %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("and_u" + prefix, "and %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("or_u" + prefix, "or %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        for (String prefix : typesAndPrefixes.keySet())
            opsPerType.put("xor_u" + prefix, "xor %t with type '" + typesAndPrefixes.get(prefix) + "'.");

        Map<String, String> typesAndPrefixesOfStack = new LinkedHashMap<>();
        typesAndPrefixesOfStack.put("b",   "Byte");
        typesAndPrefixesOfStack.put("s",   "Short");
        typesAndPrefixesOfStack.put("i",   "Int");
        typesAndPrefixesOfStack.put("l",   "Long");
        typesAndPrefixesOfStack.put("f",   "Float");
        typesAndPrefixesOfStack.put("d",   "Double");
        typesAndPrefixesOfStack.put("df",  "DoubleFloat");
        typesAndPrefixesOfStack.put("dd",  "DoubleDouble");
        typesAndPrefixesOfStack.put("li",  "LongInt");
        typesAndPrefixesOfStack.put("ll",  "LongLong");
        typesAndPrefixesOfStack.put("a",   "Pointer");

        typesAndPrefixesOfStack.put("ub",  "UnsignedByte");
        typesAndPrefixesOfStack.put("us",  "UnsignedShort");
        typesAndPrefixesOfStack.put("ui",  "UnsignedInt");
        typesAndPrefixesOfStack.put("ul",  "UnsignedLong");
        typesAndPrefixesOfStack.put("uf",  "UnsignedFloat");
        typesAndPrefixesOfStack.put("ud",  "UnsignedDouble");
        typesAndPrefixesOfStack.put("udf", "UnsignedDoubleFloat");
        typesAndPrefixesOfStack.put("udd", "UnsignedDoubleDouble");
        typesAndPrefixesOfStack.put("uli", "UnsignedLongInt");
        typesAndPrefixesOfStack.put("ull", "UnsignedLongLong");
        typesAndPrefixesOfStack.put("ua",  "UnsignedPointer");

        Map<String, String> c_typesAndPrefixes = new LinkedHashMap<>();
        c_typesAndPrefixes.put("b",   "int_8");
        c_typesAndPrefixes.put("s",   "int_16");
        c_typesAndPrefixes.put("i",   "int_32");
        c_typesAndPrefixes.put("l",   "int_64");
        c_typesAndPrefixes.put("f",   "flt_32");
        c_typesAndPrefixes.put("d",   "flt_64");
        c_typesAndPrefixes.put("df",  "flt_128");
        c_typesAndPrefixes.put("dd",  "flt_256");
        c_typesAndPrefixes.put("li",  "int_128");
        c_typesAndPrefixes.put("ll",  "int_256");
        c_typesAndPrefixes.put("a",   "pointer");
        c_typesAndPrefixes.put("ub",   "uint_8");
        c_typesAndPrefixes.put("us",   "uint_16");
        c_typesAndPrefixes.put("ui",   "uint_32");
        c_typesAndPrefixes.put("ul",   "uint_64");
        c_typesAndPrefixes.put("uli",  "uint_128");
        c_typesAndPrefixes.put("ull",  "uint_256");

        int i = 0;
        String n = genNumbers ? " = %i, /** . **/" : ", /** . **/";

        for (String prefix : typesAndPrefixes.keySet())
        {
            for (String op : opsPerType.keySet())
            {
                String s = prefix + op;
                int spaces = 20 - s.length();
                for (int x = 0; x < spaces; x ++)
                    s += " ";
                System.out.println(s + n.replace("%i", i ++ + "").replace(".", opsPerType.get(op).replace("%t", typesAndPrefixes.get(prefix))));
            }
        }
    }
}
