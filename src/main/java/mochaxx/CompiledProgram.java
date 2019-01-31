package mochaxx;

import mochaxx.compiler.Modifier;
import mochaxx.compiler.Token;
import mochaxx.compiler.structure.OpcodeStream;

import java.util.*;

public class CompiledProgram
{
    static class ABI
    {
    }

    class CLASS
    {
        private CLASS       parent;
        private String      name;
        private FIELD       fields[];
        private FUNCTION    functions[];

        @Override
        public String toString()
        {
            return name;
        }

        public boolean containsField(String toString)
        {
            for (FIELD field : fields)
                if (field.name.equals(toString))
                    return true;

            return false;
        }

        public int getField(String toString)
        {
            for (int i = 0; i < fields.length; i ++)
            {
                FIELD field = fields[i];

                if (field.name.equals(toString))
                    return i;
            }

            return -1;
        }

        public boolean hasoperator(String subscript)
        {
            for (FUNCTION function : functions)
                if (function.typename.equals("operator") && function.name.equals(subscript))
                    return true;

            return false;
        }
    }

    class FIELD
    {
        String name;
        String typename;
        int    size;

        int accesslevel;
        boolean constant;
        boolean isstatic;

        @Override
        public String toString()
        {
            return "field{" + name + " " + typename + " " + accesslevel + " " + constant + " " + isstatic + " " + size + "}";
        }

        FIELD()
        {
        }

        FIELD(String name, String typename)
        {
            this.name = name;
            this.typename = typename;
        }


        public void calculatesize()
        {
            switch (typename)
            {
                case "char":
                case "uint8":
                case "int8":
                case "bool":
                case "boolean":
                    size = 1;
                    break;
                case "short":
                case "int16":
                case "uint16":
                    size = 2;
                    break;
                case "int":
                case "uint":
                case "int32":
                case "uint32":
                    size = 4;
                    break;
                case "long":
                case "int64":
                case "uint64":
                    size = 8;
                    break;
                case "float":
                    size = 4;
                    break;
                case "double":
                    size = 8;
                    break;
                case "void":
                case "MemObject":
                    if (allowmemoryuse)
                        size = 8;
                    else
                    {
                        System.err.println("memory manipulation not allowed.");
                        System.exit(0);
                    }
                    break;
                default:
                    if (!classes.containsKey(typename) && !typename.equals("void") && !typename.equals("MemObject"))
                    {
                        System.err.println("use of undeclared type " + typename + ".");
                        System.exit(0);
                    }
                    size = 8;
                    break;
            }
        }
    }

    class FUNCTION
    {
        String name;
        String typename;

        int accesslevel;
        boolean constant;
        boolean isstatic;
        boolean isnative;
        int     returnsize;

        Token parn_token;
        Token body_token;

        @Override
        public String toString()
        {
            return "function{" + name + " " + typename + " " + accesslevel + " " + constant + " " + isstatic + " " + isnative + " " + returnsize + "}";
        }

        public void calculatesize()
        {
            switch (typename)
            {
                case "char":
                case "uint8":
                case "int8":
                case "bool":
                case "boolean":
                    returnsize = 1;
                    break;
                case "short":
                case "int16":
                case "uint16":
                    returnsize = 2;
                    break;
                case "int":
                case "uint":
                case "int32":
                case "uint32":
                    returnsize = 4;
                    break;
                case "long":
                case "int64":
                case "uint64":
                    returnsize = 8;
                    break;
                case "float":
                    returnsize = 4;
                    break;
                case "double":
                    returnsize = 8;
                    break;
                case "MemObject":
                    if (allowmemoryuse)
                        returnsize = 8;
                    else
                    {
                        System.err.println("memory manipulation not allowed.");
                        System.exit(0);
                    }
                    break;
                case "void":
                case "operator":
                    returnsize = 0;
                    break;
                default:
                    if (!classes.containsKey(typename) && !typename.equals("void"))
                    {
                        System.err.println("use of undeclared type " + typename + ".");
                        System.exit(0);
                    }
                    returnsize = 8;
                    break;
            }
        }
    }

    HashMap<String, CLASS> classes = new HashMap<>();
    private boolean                 allowmemoryuse;
    private boolean                 allowexcasting;

    public CompiledProgram(ParsedProgram program)
    {
        System.out.println("compilation started.");
        for (CompilerFlag flag : program.GetCompilerFlags())
            switch (flag)
            {
                case MEMORY: allowmemoryuse = true; break;
                case CASTING: allowexcasting = true; break;
            }

        ABI abi = new ABI();

        for (Token token : program.getTokens())
        {
            if (token.equals(Token.Type.CLASS_DECLARATION))
            {
                CLASS class_ = new CLASS();
                class_.name     = token.get(Token.Type.NAME).toString();
                List<FIELD> fields = new ArrayList<>();
                List<FUNCTION> functions = new ArrayList<>();

                for (Token class_token : token.get(Token.Type.BRACES))
                {
                    if (class_token.equals(Token.Type.EMPTY_DECLARATION))
                    {
                        FIELD field = new FIELD();
                        fields.add(field);

                        field.name = class_token.get(Token.Type.NAME).toString();
                        field.typename = class_token.get(Token.Type.VARTYPE).toString();

                        if (class_token.isModifier(Modifier.PUBLIC))
                            field.accesslevel = 0;
                        else if (class_token.isModifier(Modifier.PRIVATE))
                            field.accesslevel = 1;
                        else if (class_token.isModifier(Modifier.PROTECTED))
                            field.accesslevel = 2;
                        else
                            field.accesslevel = 1;

                        if (class_token.isModifier(Modifier.CONST))
                            field.constant = true;

                        if (class_token.isModifier(Modifier.STATIC))
                            field.isstatic = true;
                    } else
                    if (class_token.equals(Token.Type.METHOD_DECLARATION))
                    {
                        FUNCTION function = new FUNCTION();
                        functions.add(function);

                        function.name = class_token.get(Token.Type.NAME).toString();
                        function.typename = class_token.get(Token.Type.VARTYPE).toString();

                        if (class_token.isModifier(Modifier.PUBLIC))
                            function.accesslevel = 0;
                        else if (class_token.isModifier(Modifier.PRIVATE))
                            function.accesslevel = 1;
                        else if (class_token.isModifier(Modifier.PROTECTED))
                            function.accesslevel = 2;
                        else
                            function.accesslevel = 1;

                        if (class_token.isModifier(Modifier.CONST))
                            function.constant = true;

                        if (class_token.isModifier(Modifier.STATIC))
                            function.isstatic = true;

                        if (class_token.isModifier(Modifier.NATIVE))
                        {
                            System.err.println(function.name + " in " + class_.name + " cannot be 'native' because it's declared.");
                            System.exit(0);
                        }

                        function.parn_token = class_token.get(Token.Type.PARENTHESIS);
                        function.body_token = class_token.get(Token.Type.BRACES);
                    } else
                    if (class_token.equals(Token.Type.METHOD_EMPTY_DECLARATION))
                    {
                        FUNCTION function = new FUNCTION();
                        functions.add(function);

                        function.name = class_token.get(Token.Type.NAME).toString();
                        function.typename = class_token.get(Token.Type.VARTYPE).toString();

                        if (class_token.isModifier(Modifier.PUBLIC))
                            function.accesslevel = 0;
                        else if (class_token.isModifier(Modifier.PRIVATE))
                            function.accesslevel = 1;
                        else if (class_token.isModifier(Modifier.PROTECTED))
                            function.accesslevel = 2;
                        else
                            function.accesslevel = 1;

                        if (class_token.isModifier(Modifier.CONST))
                            function.constant = true;

                        if (class_token.isModifier(Modifier.STATIC))
                            function.isstatic = true;

                        if (class_token.isModifier(Modifier.NATIVE))
                            function.isnative = true;

                        function.parn_token = class_token.get(Token.Type.PARENTHESIS);

                        if (class_token.isModifier(Modifier.NATIVE) && !class_token.isModifier(Modifier.STATIC))
                        {
                            System.err.println("native functions must be static.");
                            System.exit(0);
                        }
                    }
                }

                class_.fields = new FIELD[fields.size()];
                class_.functions = new FUNCTION[functions.size()];

                fields.<FIELD>toArray(class_.fields);
                functions.<FUNCTION>toArray(class_.functions);

                if (classes.containsKey(class_.name))
                {
                    System.err.println("redefenition of class type '" + class_.name + "'.");
                    System.exit(0);
                }
                classes.put(class_.name, class_);
            }
        }

        for (Token token : program.getTokens())
        {
            if (token.equals(Token.Type.CLASS_DECLARATION))
            {
                String name     = token.get(Token.Type.NAME).toString();

                if (token.get(Token.Type.PARENT_CLASS) != null)
                {
                    classes.get(name).parent = classes.get(token.get(Token.Type.PARENT_CLASS).toString());

                    if (classes.get(name).parent == null)
                    {
                        System.err.println("error in class '" + name + "'.\nparent of type '" + token.get(Token.Type.PARENT_CLASS).toString() + "' does not exist.");
                        System.exit(0);
                    }
                }
            }
        }

        for (String string : classes.keySet())
        {
            CLASS _class_ = classes.get(string);

            for (FIELD field : _class_.fields)
                field.calculatesize();

            for (FUNCTION function : _class_.functions)
                function.calculatesize();
        }


        OpcodeStream stream = new OpcodeStream();


        for (String string : classes.keySet())
        {
            CLASS _class_ = classes.get(string);

            for (FUNCTION function : _class_.functions)
                if (!function.isnative)
                    invoke(stream, function.isstatic ? null : _class_, _class_.name, function);
        }

        System.out.println(stream);
    }

    private class
    STACKFIELD
    {
        STACKFIELD()
        {
        }

        STACKFIELD(String name, String type)
        {
            this.name = name;
            this.type = type;
        }

        String name;
        String type;
        int    indx;
    }

    private
    void stack_push(final Stack<STACKFIELD> stack_sim, STACKFIELD stackfile)
    {
        stackfile.indx = stack_sim.size();
        stack_sim.push(stackfile);
    }

    private
    void invoke(OpcodeStream stream,
                CLASS __this__, String __nmsp__, FUNCTION __funcn__)
    {
        Stack<STACKFIELD> stack_sim = new Stack<>();

        if (__this__ != null)
        {
            STACKFIELD field = new STACKFIELD();
            field.name = "this";
            field.type = __this__.name;

            stack_push(stack_sim, field);
        }

        Queue<Token> tokenQueue = new LinkedList<>(__funcn__.body_token.getChildren());
//        String funcidentifier = __funcn__.name + "_" + __funcn__.typename;
        String funcidentifier = __nmsp__ + "_" + __funcn__.name;

        for (Token token : __funcn__.parn_token)
            if (!token.equals(Token.Type.COMMA))
                funcidentifier += "_" + token.get(Token.Type.VARTYPE);

        stream.write(funcidentifier + ":");

        for (Token token : tokenQueue)
            interpret(stream, token, __this__, stack_sim, true);
    }

    private
    void interpret(OpcodeStream stream, Token token, CLASS __this__, Stack<STACKFIELD> stack_sim, boolean ismaininvoke)
    {
        switch (token.getType())
        {
            case RETURN:
                interpret(stream, token.getChild(0), __this__, stack_sim, false);
                stream.op_retm("return from function.");
                stack_sim.pop();
                return;

                case IDENTIFIER:
                if (stack_containskey(stack_sim, token.toString()))
                {
                    stack_movetotop(stream, stack_sim, token.toString());
                } else if (__this__ != null && __this__.containsField(token.toString()))
                {
                    stack_movetotop(stream, stack_sim, "this");
                    stream.op_dup("duplicate this.");
//                    stack_sim.push(stack_sim.peek());
                    stream.op_load(__this__.getField(token.toString()), "load " + token.toString() + " from object (" + __this__.name + ").");


                    STACKFIELD field = new STACKFIELD();
                    stack_push(stack_sim, field);
                    field.type = __this__.fields[__this__.getField(token.toString())].typename;
                    field.name = __this__.name + "_" + __this__.fields[__this__.getField(token.toString())].name;
                } else
                    err(token, "use of undeclared identifier '" + token.toString() + "'");
                break;

            case SUBSCRIPT:
                interpret(stream, token.getChild(0), __this__, stack_sim, false);

                if (classes.containsKey(stack_sim.peek().type))
                {
                    if (classes.get(stack_sim.peek().type).hasoperator("subscript"))
                    {
                    }
                    else
                        err(token, "subscript modifier not allowed on class type '" + stack_sim.peek().type + "'.");
                }
                else if ((stack_sim.peek().type).equals("MemObject") || (stack_sim.peek().type).equals("void"))
                {
                    stack_sim.pop();
                    interpret(stream, token.getChild(1), __this__, stack_sim, false);
                    stream.op_add(1, "add to pointer.");
                }
                else
                    err(token, "subscript modifier not allowed on primitive type '" + stack_sim.peek().type + "'.");

                break;

            case NUMBER:
                stack_push(stack_sim, new STACKFIELD(System.currentTimeMillis() + "", "int"));
                stream.op_psh(token.toString());
                break;
            case STATEMENT:
                for (Token toke : token)
                    interpret(stream, toke, __this__, stack_sim, false);
                break;

            case PROCEDURAL_ACCESS:
                interpret(stream, token.getChild(0), __this__, stack_sim, false);
                if (token.getChild(1).equals(Token.Type.PROCEDURAL_ACCESS));
                else if (token.getChild(1).equals(Token.Type.STATIC_ACCESS))
                    err(token, "static access is nested.");
                else
                {
                    if (!classes.containsKey(stack_sim.peek().type))
                        err(token.getChild(1), "'" + strip(token.getChild(0)) + "' is not an object.");

                    proc_interpret(stream, token.getChild(1), classes.get(stack_sim.peek().type), stack_sim);
                }

                break;
        }

        if (ismaininvoke)
            stream.op_ret("return from function.");
    }

    private
    Token strip(final Token token)
    {
        if (token.equals(Token.Type.STATEMENT))
            return token.getChild(0);

        return token;
    }

    private
    void proc_interpret(OpcodeStream stream, Token token, CLASS __typeoflastproc__, Stack<STACKFIELD> stack_sim)
    {
        switch (token.getType())
        {
            case IDENTIFIER:
                if (__typeoflastproc__ != null && __typeoflastproc__.containsField(token.toString()))
                {
                    STACKFIELD field = new STACKFIELD();
                    stack_sim.pop();
                    stack_push(stack_sim, field);
                    field.type = __typeoflastproc__.fields[__typeoflastproc__.getField(token.toString())].typename;
                    field.name = "tempproc_" + __typeoflastproc__.name + "_" + __typeoflastproc__.fields[__typeoflastproc__.getField(token.toString())].name;
                    stream.op_load(__typeoflastproc__.getField(token.toString()), "load " + token.toString() + " from object (" + __typeoflastproc__.name + ").");
                } else
                    err(token, "use of undeclared identifier '" + token.toString() + "'");
                break;

            case STATEMENT:
                for (Token toke : token)
                    proc_interpret(stream, toke, __typeoflastproc__, stack_sim);
                break;

            case PROCEDURAL_ACCESS:
                proc_interpret(stream, token.getChild(0), __typeoflastproc__, stack_sim);

                if (token.getChild(1).equals(Token.Type.PROCEDURAL_ACCESS));
                else if (token.getChild(1).equals(Token.Type.STATIC_ACCESS))
                    err(token, "static access is nested.");
                else
                {
                    if (!classes.containsKey(stack_sim.peek().type))
                        err(token.getChild(1), "'" + strip(token.getChild(0)) + "' is not an object.");

                    proc_interpret(stream, token.getChild(1), classes.get(stack_sim.peek().type), stack_sim);
                }

                break;
        }
    }

    private
    void static_access(final OpcodeStream stream, final Token token_proc2, final Stack<STACKFIELD> stack_sim)
    {
    }

    private
    void stack_movetotop(OpcodeStream stream, Stack<STACKFIELD> stack_sim, String field)
    {
        if (stack_sim.size() < 2)
            return;

        int field_item = stack_getkey(stack_sim, field);
        int maxitem___ = stack_sim.size() - 1;

        stream.op_swap(field_item, "get " + field);

        STACKFIELD item = stack_sim.get(field_item);
        STACKFIELD item2 = stack_sim.get(maxitem___);

        stack_sim.set(field_item, stack_sim.get(maxitem___));
        stack_sim.set(maxitem___, item);

        item.indx = maxitem___;
        item2.indx = field_item;
    }

    private
    boolean stack_containskey(Stack<STACKFIELD> stack_sim, String field)
    {
        return stack_getkey(stack_sim, field) >= 0;
    }

    private
    int stack_getkey(Stack<STACKFIELD> stack_sim, String field)
    {
        int r = -1;

        for (int i = 0; i < stack_sim.size(); i ++)
        {
            STACKFIELD stackfield = stack_sim.get(i);

            if (stackfield.name.equals(field))
                r = i;
        }

        return r;
    }

    void err(final Token token, final String errstring)
    {
        System.err.println("error at line '" + token.line + "'.\n" + errstring + "\n");
        System.exit(0);
    }
}