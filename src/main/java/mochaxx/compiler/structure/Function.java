//package mochaxx.compiler.structure;
//
//import mochaxx.compiler.Modifier;
//import mochaxx.compiler.Token;
//import mochaxx.Base58;
//import mochaxx.Handler;
//import mochaxx.Tuple;
//
//import java.util.*;
//
//import static mochaxx.compiler.Token.Type.*;
//import static util.HashUtil.applyRipeMD160;
//
//public class Function
//{
//    long id;
//    String name;
//    Token token;
//    Token struct;
//    Collection<Token> tokens;
//
//    OpcodeStream stream;
//
//    public Function(String name, long id, Token token, Token struct, Collection<Token> tokens)
//    {
//        this.name = name;
//        this.id = id;
//        this.token = token;
//        this.struct = struct;
//        stream = new OpcodeStream();
//        this.tokens = tokens;
//        stream.op_func(name.replaceAll("\\:", "_"));
//    }
//
//    public void compile()
//    {
////        for (Token t : token)
//            docompile(token);
//    }
//
//
//    private
//    class scope{
//        Field   stack[] = new Field[1000];
//        long    esp = 0;
//
//        public void pop()
//        {
//            esp --;
//
//            if (esp < 0)
//                esp = 0;
//        }
//
//        public boolean containsKey(String name)
//        {
//            return get(name) != null;
//        }
//
//        public boolean push(String name, Struct type)
//        {
//            if (containsKey(name))
//                return false;
//
//            long index = esp ++;
//
//            Field field = new Field(name);
//            field.setIndex(index);
//            field.setType(type);
//
//            stack[(int) index] = field;
//
//            return true;
//        }
//
//        public void push()
//        {
//            esp ++;
//        }
//
//        public Field get(String name)
//        {
//            for (int i = 0; i < esp; i ++)
//                if (stack[i].getName().equals(name))
//                    return stack[i];
//
//            return null;
//        }
//
//        /**
//         * @return true if item exists.
//         *
//         * This function gets the requested item from the stack.
//         * If the item is not the topmost stack item it is swapped
//         * with the topmost item and returned.
//         */
//        public boolean move_to_top(String name, OpcodeStream ops)
//        {
//            Field field = get(name);
//
//            if (field == null) return false;
//
//            if (field.getIndex() != (esp - 1))
//            {
//                long highestIndex = esp - 1;
//
//                long currentIndex = field.getIndex();
//
//                ops.op_swap(currentIndex, "move '" + name + "' to the top of the stack");
//
//                return true;
//            }
//
//            return true;
//        }
//    }
//
//    private class sfield{
//        String              type;
//        String              name;
//        long                location;
//        Set<String>         generics;
//        Set<Modifier>       mods;
//
//        public sfield(String type, String name, long location)
//        {
//            this.type = type;
//            this.name = name;
//            this.location = location;
//            this.mods = new LinkedHashSet<>();
//            this.generics = new LinkedHashSet<>();
//        }
//
//        public boolean isUnsigned()
//        {
//            return mods.contains(Modifier.UNSIGNED);
//        }
//
//        public void setGenerics(Set<String> generics)
//        {
//            this.generics = generics;
//        }
//
//        @Override
//        public String toString()
//        {
//            return "[" + type + " " + name + "]";
//        }
//
//        public boolean isFloatingPointType()
//        {
//            return type.equals("float") || type.equals("double");
//        }
//
//        public int alu_op(sfield b)
//        {
//            if ((isFloatingPointType() && !b.isFloatingPointType()) || (!isFloatingPointType() && b.isFloatingPointType()))
//            {
//                System.err.println("compile-err: float x int operation not allowed.");
//                System.exit(0);
//            }
//
//            if (isFloatingPointType())
//                return 2;
//            else if (isUnsigned() && b.isUnsigned())
//                return 1;
//            else if (!isUnsigned() || !b.isUnsigned())
//                return 0;
//
//            return 0;
//        }
//
//        public int mov_from_ebp()
//        {
//            switch (type)
//            {
//                case "char":
//                    return (15);
//                case "short":
//                    return (17);
//                case "int":
//                    return (19);
//                case "long":
//                case "mstring":
//                    return 21;
//                case "float":
//                    return (19);
//                case "double":
//                    return 21;
//            }
//
//            return -1;
//        }
//
//        public String mov_to_ebp()
//        {
//            switch (type)
//            {
//                case "char":
//                    return "[$ESB+b]";
//                case "short":
//                    return "[$ESB+s]";
//                case "int":
//                    return "[$ESB+i]";
//                case "long":
//                case "mstring":
//                    return "[$ESB+v]";
//                case "float":
//                    return "[$EFE] [$ESB+i]";
//                case "double":
//                    return "[$ESB+v]";
//            }
//
//            return "0";
//        }
//
//        public int size(Token token)
//        {
//            switch (type)
//            {
//                case "char":
//                    return (1);
//                case "short":
//                    return (2);
//                case "int":
//                    return (4);
//                case "long":
//                case "mstring":
//                    return 8;
//                case "float":
//                    return (4);
//                case "double":
//                    return 8;
//            }
//
//            return 8;
//        }
//
//        public void setModifiers(Set<Modifier> modifiers)
//        {
//            this.mods.addAll(modifiers);
//        }
//
//        public boolean isnativetype()
//        {
//            switch (type)
//            {
//                case "char":
//                case "short":
//                case "int":
//                case "long":
//                case "float":
//                case "double":
//                    return true;
//
//                    default:
//                        break;
//            }
//
//            return false;
//        }
//
//        public int realsize(Collection<Token> tokens)
//        {
//            if (isnativetype())
//                return size(null);
//
//            for (Token token : tokens)
//                if (!token.equals(TYPEDEF) && token.get(NAME).data.equals(type))
//                {
//                    long size = 0;
//
//                    for (Token field : token.get(BRACES))
//                    {
//
//                    }
//
//
//                    return (int) size;
//                }
//
//            return -1;
//        }
//    }
//
//    private void docompile(Token token)
//    {
//        Token template = getTemplate();
//        Token argument = getArguments();
//
//        if (token.get(Token.Type.BRACES) == null)
//            return;
//
//        Map<String, sfield> localFields = new HashMap<>();
//        long location = 0;
//        for (Token field : struct.get(Token.Type.BRACES))
//        {
//            if (field.equals(EMPTY_DECLARATION))
//            {
//                sfield field_ = null;
//                localFields.put(field.get(NAME).data, field_ = new sfield(field.get(VARTYPE).data, field.get(NAME).data, location));
//                field.setModifiers(field.modifiers);
//
//                location += field_.size(field);
//            }
//        }
//        Stack<sfield> scopeFields = new Stack<>();
//        if (!token.isModifier(Modifier.STATIC) && !token.isModifier(Modifier.NATIVE))
//            scopeFields.push(new sfield("pointer", "this", 0));
//
//        for (Token uncompiled : token.get(PARENTHESIS))
//        {
//        }
//
//        for (Token uncompiled : token.get(Token.Type.BRACES))
//            leftcompile(uncompiled, localFields, scopeFields, new HashMap<>(), stream, new Handler<>(null));
//
//
//        if (!lastStack.equals(scopeFields.toString()))
//        {
//            stream.write("#\t\t\t" + scopeFields.toString());
//            lastStack = scopeFields.toString();
//        }
//
//        System.out.println(stream.toString());
//    }
//
//    private void compile(Token uncompiled, Map<String, sfield> local, Stack<sfield> scope)
//    {
//        compile(uncompiled, local, scope, new HashMap<>(), stream);
//    }
//
//    private int inscope(Stack<sfield> scope, String item)
//    {
//        for (int i = 0; i < scope.size(); i ++)
//            if (scope.get(i).name.equals(item))
//                return i;
//
//        return -1;
//    }
//
//    private String lastStack = "";
//
//    private void compile(Token uncompiled, Map<String, sfield> local, Stack<sfield> scope, Map<String, Long> objectLocal, OpcodeStream stream)
//    {
//        compile(uncompiled, local, scope, objectLocal, stream, new Handler<>(null));
//    }
//
//    private void compileRightSide(Token uncompiled, Map<String, sfield> local, Stack<sfield> scope, Map<String, Long> objectLocal, OpcodeStream stream, Handler<Token> fieldInfo)
//    {
//        switch (uncompiled.getType())
//        {
//            case PARENTHESIS:
//            case BRACES:
//            case STATEMENT:
//            case EQUALS:
//                for (Token toke : uncompiled)
//                    compileRightSide(toke, local, scope, objectLocal, stream, fieldInfo);
//                break;
//            case ADDITION:
//                stream.op_add(scope.get(scope.size() - 2).alu_op(scope.peek()), "");
//                scope.pop();
//                scope.pop();
//                break;
//            case SUBTRACTION:
//                stream.op_sub(scope.get(scope.size() - 2).alu_op(scope.peek()), "");
//                scope.pop();
//                scope.pop();
//                break;
//            case MULTIPLICATION:
//                stream.op_mul(scope.get(scope.size() - 2).alu_op(scope.peek()), "");
//                scope.pop();
//                scope.pop();
//                break;
//            case SUBDIVISION:
//                stream.op_div(scope.get(scope.size() - 2).alu_op(scope.peek()), "");
//                scope.pop();
//                scope.pop();
//                break;
//
//            case ASSERT:
//                stream.op_cmpless("0", "compare equal");
//                break;
//
//            case LESS_THAN:
//                stream.op_cmpless("1", "compare less than");
//                break;
//
//            case MORE_THAN:
//                stream.op_cmpless("2", "compare greater than");
//                break;
//
//            case STRING:
//                stream.op_str(uncompiled.data);
//                break;
//
//            case NUMBER:
//                stream.op_psh(uncompiled.data);
//                if (fieldInfo.get() == null)
//                    scope.push(new sfield("int", uncompiled.data, 0));
//                else
//                {
//                    sfield field = new sfield(fieldInfo.get().get(VARTYPE).data, fieldInfo.get().get(NAME).data, 0);
//                    field.setModifiers(fieldInfo.get().modifiers);
//                    scope.push(field);
//                    fieldInfo.set(null);
//                }
//                break;
//            case DECIMAL:
//                stream.op_psh(stream.convertDoubleToStringInt(uncompiled.data), "push a double with value '" + uncompiled.data + "'");
//                break;
////            case IDENTIFIER:
////                if (objectLocal.containsKey(uncompiled.data))
////                {
////                } else if (inscope(scope, uncompiled.data) >= 0)
////                {
////                    if (scope.size() > 1)
////                    {
////                        stream.op_swap(inscope(scope, uncompiled.data), "move '" + uncompiled.data + "' to top");
////
////                        String top = "";
////
//////                        for (String string : scope.keySet())
//////                        {
//////                            System.out.println(string + " " + scope.get(string));
//////                            if (scope.get(string) == (scope.size() - 1))
//////                                top = string;
//////                        }
////
////                        //SWAP
////                        sfield s = scope.get(inscope(scope, uncompiled.data));
////
////                        scope.set(inscope(scope, uncompiled.data), scope.peek());
////                        scope.set(scope.size() - 1, s);
//////                        scope.put(top, scope.get(uncompiled.data));
//////                        scope.put(uncompiled.data, (long) (scope.size() - 1));
////                    }
////
////                    stream.op_dup("duplicate the top '" + uncompiled.data + "' as it will be popped later");
////
////                    //DUPLICATE
////                    scope.push(scope.peek());
////                } else if (local.containsKey(uncompiled.data))
////                {
////                    if (scope.size() > 1)
////                    {
////                        stream.op_swap(inscope(scope, "this"), "move '" + "this" + "' to top");
////
////                        String top = "";
////
////                        //SWAP
////                        sfield s = scope.get(inscope(scope, uncompiled.data));
////
////                        scope.set(inscope(scope, uncompiled.data), scope.pop());
////                        scope.set(scope.size() - 1, s);
////                    }
////
////                    stream.op_dup("duplicate the top '" + "this" + "' as it will be popped later");
////                    //DUPLICATE
//////                    scope.put(uncompiled.data + "_copy" + System.currentTimeMillis(), (long) scope.size());
////
////                    if (local.get(uncompiled.data).location > 0)
////                    {
////                        stream.op_psh(local.get(uncompiled.data).location + "");
////                        stream.op_add(1, "add '" + local.get(uncompiled.data).location + "' to pointer");
////                    }
////
////                    stream.op_mov(pack(local.get(uncompiled.data).mov_from_ebp()), "move '" + uncompiled.data + "' to stack");
////                }
////                else
////                {
////                    System.err.println("compile-err: field '" + uncompiled.data + "' does not exist.");
////                    System.err.println("errstr: line " + uncompiled.line + " offset " + uncompiled.offset);
////                    System.exit(0);
////                }
////                break;
//        }
//    }
//
//    private void leftcompile(Token uncompiled, Map<String, sfield> local, Stack<sfield> scope, Map<String, sfield> objectLocal, OpcodeStream stream, Handler<Token> fieldInfo)
//    {
//        if (!lastStack.equals(scope.toString()))
//        {
//            stream.write("#\t\t\t" + scope.toString());
//            lastStack = scope.toString();
//        }
//
//        switch (uncompiled.getType())
//        {
//            case PROCEDURAL_ACCESS:
//                break;
//            case STATIC_ACCESS:
//                break;
//            case METHOD_CALL:
//                break;
//            case EMPTY_DECLARATION:
//                break;
//            case FULL_DECLARATION:
//                String type = uncompiled.get(VARTYPE).data;
//                String name = uncompiled.get(NAME).data;
//                sfield field = new sfield(type, name, 0);
//                field.setModifiers(uncompiled.modifiers);
//
//                scope.push(field);
//                rightcompile(uncompiled.get(EQUALS), local, scope, objectLocal, stream, fieldInfo);
//                break;
//            case ASSIGNMENT:
//                leftcompile(uncompiled.getChild(1), local, scope, objectLocal, stream, fieldInfo);
//                rightcompile(uncompiled.getChild(0), local, scope, objectLocal, stream, fieldInfo);
//                break;
//
//            case IDENTIFIER:
//                break;
//
//            case NUMBER:
//            case DECIMAL:
//            case STRING:
//                System.err.println("statement ignored.");
//                break;
//        }
//    }
//
//    private void stack_set(Stack<sfield> scope, String item, sfield obj)
//    {
//        scope.set(inscope(scope, item), obj);
//    }
//
//    private void rightcompile(Token uncompiled, Map<String, sfield> local, Stack<sfield> scope, Map<String, sfield> objectLocal, OpcodeStream stream, Handler<Token> fieldInfo)
//    {
//        if (!lastStack.equals(scope.toString()))
//        {
//            stream.write("#\t\t\t" + scope.toString());
//            lastStack = scope.toString();
//        }
//
//        switch (uncompiled.getType())
//        {
//            case EQUALS:
//                for (Token toke : uncompiled)
//                    rightcompile(toke, local, scope, objectLocal, stream, fieldInfo);
//                break;
//            case IDENTIFIER:
//                if (objectLocal.size() > 0)
//                {
//                    if (objectLocal.containsKey(uncompiled.data))
//                    {
//                    }
//                    else {
//                        System.err.println("compile-err: field '" + uncompiled.data + "' does not exist.");
//                        System.err.println("errstr: line " + uncompiled.line + " offset " + uncompiled.offset);
//                        System.exit(0);
//                    }
//                }
//                else if (inscope(scope, uncompiled.data) >= 0)
//                {
//                    stream.op_cpy(inscope(scope, uncompiled.data), uncompiled.data);
//                }
//                else if (local.containsKey(uncompiled.data))
//                {
//                }
//                else
//                {
//                    System.err.println("compile-err: field '" + uncompiled.data + "' does not exist.");
//                    System.err.println("errstr: line " + uncompiled.line + " offset " + uncompiled.offset);
//                    System.exit(0);
//                }
//                break;
//            case NUMBER:
//                stream.op_psh(uncompiled.data);
//                break;
//            case DECIMAL:
//                stream.op_psh(stream.convertDoubleToStringInt(uncompiled.data), "push a double with value '" + uncompiled.data + "'.");
//                break;
//            case METHOD_CALL:
//                Token method = uncompiled;
//                uncompiled = method.get(NAME);
//
//                if (uncompiled.data.equals("copy"))
//                {
//                    if (method.get(PARENTHESIS).children.size() > 1)
//                    {
//                        System.err.println("method 'copy' takes 1 argument.");
//                        System.err.println("errstr: line " + method.line + " offset " + method.offset);
//                        System.exit(0);
//                    }
//
//                    if (inscope(scope, method.get(PARENTHESIS).getChild(0).data) < 0)
//                    {
//                        System.err.println("field '" + method.get(PARENTHESIS).getChild(0).data + "' is not a stack element.");
//                        System.err.println("errstr: line " + method.line + " offset " + method.offset);
//                        System.exit(0);
//                    }
//
//                    long   ifild = inscope(scope, method.get(PARENTHESIS).getChild(0).data);
//                    sfield field = scope.get((int) ifild);
//
//                    if (field.isnativetype())
//                    {
//                        stream.op_cpy((int) ifild, "copy() of '" + field.name + "'.");
//                    }
//                    else
//                    {
//                        stream.op_cpy((int) ifild, "exact copy() of '" + field.name + "*'.");
//                        stream.op_malloc(field.realsize(tokens));
//                        stream.op_memcpy(field.realsize(tokens));
//                    }
//                }
//                else if (uncompiled.data.equals("memcpy"))
//                {
//                }
//                else if (uncompiled.data.equals("malloc"))
//                {
//                }
//                else if (uncompiled.data.equals("calloc"))
//                {
//                }
//                else if (uncompiled.data.equals("assert"))
//                {
//                }
//                else if (uncompiled.data.equals("delete"))
//                {
//                }
//                else if (uncompiled.data.equals("sizeof"))
//                {
//                    if (method.get(PARENTHESIS).children.size() > 1)
//                    {
//                        System.err.println("method 'sizeof' takes 1 argument.");
//                        System.err.println("errstr: line " + method.line + " offset " + method.offset);
//                        System.exit(0);
//                    }
//
//                    if (inscope(scope, method.get(PARENTHESIS).getChild(0).data) >= 0)
//                    {
//                        long   ifild = inscope(scope, method.get(PARENTHESIS).getChild(0).data);
//                        sfield field = scope.get((int) ifild);
//
//                            stream.op_psh(field.realsize(tokens) + "", "exact sizeof() of '" + field.name + "'.");
//                    }
//                    else if (local.containsKey(method.get(PARENTHESIS).getChild(0).data))
//                    {
//                    }
//                    else
//                    {
//                    }
//                }
//                else if (uncompiled.data.equals("typeof"))
//                {
//                }
//
//                else
//                    stream.op_ivk(0, uncompiled.data);
//                break;
//        }
//    }
//
//    private void compile(Token uncompiled, Map<String, sfield> local, Stack<sfield> scope, Map<String, Long> objectLocal, OpcodeStream stream, Handler<Token> fieldInfo)
//    {
//        if (!lastStack.equals(scope.toString()))
//        {
//            stream.write("#\t\t\t" + scope.toString());
//            lastStack = scope.toString();
//        }
//
//        switch (uncompiled.getType())
//        {
//            case ADDITION:
//                stream.op_add(scope.get(scope.size() - 2).alu_op(scope.peek()), "");
//                scope.pop();
//                scope.pop();
//                break;
//            case SUBTRACTION:
//                stream.op_sub(scope.get(scope.size() - 2).alu_op(scope.peek()), "");
//                scope.pop();
//                scope.pop();
//                break;
//            case MULTIPLICATION:
//                stream.op_mul(scope.get(scope.size() - 2).alu_op(scope.peek()), "");
//                scope.pop();
//                scope.pop();
//                break;
//            case SUBDIVISION:
//                stream.op_div(scope.get(scope.size() - 2).alu_op(scope.peek()), "");
//                scope.pop();
//                scope.pop();
//                break;
//
//            case ASSERT:
//                stream.op_cmpless("0", "compare equal");
//                break;
//
//            case LESS_THAN:
//                stream.op_cmpless("1", "compare less than");
//                break;
//
//            case MORE_THAN:
//                stream.op_cmpless("2", "compare greater than");
//                break;
//
//            case STRING:
//                stream.op_str(uncompiled.data);
//                break;
//
//            case NUMBER:
//                stream.op_psh(uncompiled.data);
//                if (fieldInfo.get() == null)
//                    scope.push(new sfield("int", uncompiled.data, 0));
//                else
//                {
//                    sfield field = new sfield(fieldInfo.get().get(VARTYPE).data, fieldInfo.get().get(NAME).data, 0);
//                    field.setModifiers(fieldInfo.get().modifiers);
//                    scope.push(field);
//                    fieldInfo.set(null);
//                }
//                break;
//            case DECIMAL:
//                stream.op_psh(stream.convertDoubleToStringInt(uncompiled.data), "push a double with value '" + uncompiled.data + "'");
//                if (fieldInfo.get() == null)
//                    scope.push(new sfield("double", uncompiled.data, 0));
//                else
//                {
//                    sfield field = new sfield(fieldInfo.get().get(VARTYPE).data, fieldInfo.get().get(NAME).data, 0);
//                    field.setModifiers(fieldInfo.get().modifiers);
//                    scope.push(field);
//                    fieldInfo.set(null);
//                }
//                break;
//
//            case ASSIGNMENT:
//                stream.write("#assignment (line " + uncompiled.line + ")");
//                Token get = uncompiled.getChild(0);
//                Token set = uncompiled.getChild(1);
//                compile(get, local, scope);
//                compile(set, local, scope);
//                break;
//
////                case ASSIGNMENT:
////                    if (scopeFields.containsKey())
////                    break;
//
//            //Object.field | Object.method()
//            case PROCEDURAL_ACCESS:
//                objectLocal.clear();
//                compile(uncompiled.getChild(0), local, scope);
////                compile(tokens, uncompiled.getChild(1));
//                break;
//
//
//            case BRACES:
//            case STATEMENT:
//            case EQUALS:
//                for (Token toke : uncompiled)
//                    compile(toke, local, scope, objectLocal, stream, fieldInfo);
//                break;
//
//            case EMPTY_DECLARATION:
//                break;
//            case FULL_DECLARATION:
//                if (uncompiled.get(EQUALS).children.size() > 1)
//                {
//                    compile(uncompiled.get(EQUALS), local, scope, objectLocal, stream, new Handler<>(null));
//                    sfield field = new sfield(uncompiled.get(VARTYPE).data, uncompiled.get(NAME).data, 0);
//                    field.setModifiers(uncompiled.modifiers);
//                    scope.push(field);
//                }
//                else
//                    compile(uncompiled.get(EQUALS), local, scope, objectLocal, stream, new Handler<>(uncompiled));
//                break;
//            case INCREMENT:
//                stream.op_psh("1");
//                stream.op_add(0, "");
//                break;
//
//            case FOR:
//                Token p0 = uncompiled.get(PARENTHESIS).getChild(0);
//                Token p1 = uncompiled.get(PARENTHESIS).getChild(1);
//                Token p2 = uncompiled.get(PARENTHESIS).getChild(2);
//
//                compile(p0, local, scope, objectLocal, stream);
//                String mark = Base58.encode(applyRipeMD160(uncompiled.humanReadable().getBytes()));
//                stream.write("\n");
//                stream.op_mark(mark);
//                stream.write("#for loop case");
//
//                OpcodeStream newstream = new OpcodeStream();
//                compile(p1, local, scope, objectLocal, stream);
//
//                compile(uncompiled.get(BRACES), local, scope, objectLocal, newstream);
//                compile(p2, local, scope, objectLocal, newstream);
//
//                System.out.println(newstream);
//
//                stream.op_branch(newstream.length());
//
//                stream.write("#for loop body");
//                stream.write(newstream);
//
//                stream.op_goto(mark);
//
//
////                compile(p0, local, scope, objectLocal);
////                compile(p0, local, scope, objectLocal);
//                break;
//            case IDENTIFIER:
//                if (objectLocal.containsKey(uncompiled.data))
//                {
//                } else if (inscope(scope, uncompiled.data) >= 0)
//                {
//                    if (scope.size() > 1)
//                    {
//                        stream.op_swap(inscope(scope, uncompiled.data), "move '" + uncompiled.data + "' to top");
//
//                        String top = "";
//
////                        for (String string : scope.keySet())
////                        {
////                            System.out.println(string + " " + scope.get(string));
////                            if (scope.get(string) == (scope.size() - 1))
////                                top = string;
////                        }
//
//                        //SWAP
//                        sfield s = scope.get(inscope(scope, uncompiled.data));
//
//                        scope.set(inscope(scope, uncompiled.data), scope.peek());
//                        scope.set(scope.size() - 1, s);
////                        scope.put(top, scope.get(uncompiled.data));
////                        scope.put(uncompiled.data, (long) (scope.size() - 1));
//                    }
//
//                    stream.op_dup("duplicate the top '" + uncompiled.data + "' as it will be popped later");
//
//                    //DUPLICATE
//                    scope.push(scope.peek());
//                } else if (local.containsKey(uncompiled.data))
//                {
//                    if (scope.size() > 1)
//                    {
//                        stream.op_swap(inscope(scope, "this"), "move '" + "this" + "' to top");
//
//                        String top = "";
//
//                        //SWAP
//                        sfield s = scope.get(inscope(scope, "this"));
//
//                        scope.set(inscope(scope, "this"), scope.pop());
//                        scope.set(scope.size() - 1, s);
//                    }
//
//                    stream.op_dup("duplicate the top '" + "this" + "' as it will be popped later");
//                    //DUPLICATE
////                    scope.put(uncompiled.data + "_copy" + System.currentTimeMillis(), (long) scope.size());
//
//                    stream.write("");
//                    if (local.get(uncompiled.data).location > 0)
//                    {
//                        stream.op_psh(local.get(uncompiled.data).location + "");
//                        stream.op_add(1, "add '" + local.get(uncompiled.data).location + "' to pointer");
//                    }
//
//                    stream.op_mov(pack(local.get(uncompiled.data).mov_to_ebp()), "move from stack to '" + uncompiled.data + "'");
//                }
//                else
//                {
//                    System.err.println("compile-err: field '" + uncompiled.data + "' does not exist.");
//                    System.err.println("errstr: line " + uncompiled.line + " offset " + uncompiled.offset);
//                    System.exit(0);
//                }
//                break;
//            case POINTER_ACCESS:
//                stream.op_mov(pack(1), "");
//                break;
//        }
//    }
//
//    private int[] pack(int ...i)
//    {
//        return i;
//    }
//
//    private String[] pack(String ...i)
//    {
//        return i;
//    }
//
//    private Tuple<Token, Long> getField(String name, Token global)
//    {
//        if (name.equals("this"))
//        {
//            if (token.isModifier(Modifier.STATIC))
//            {
//                System.err.println("compile-err: using field 'this' in a static function.");
//                System.err.println("errstr: " + this.name + " line " + token.line);
//                System.exit(0);
//            }
//        }
//
//        return null;
//    }
//
//    private Token getTemplate()
//    {
//        if (token.get(Token.Type.TEMPLATE) != null)
//            return token.get(Token.Type.TEMPLATE);
//
//        return new Token(Token.Type.UNDEFINED);
//    }
//
//    private Token getArguments()
//    {
//        return token.get(Token.Type.PARENTHESIS);
//    }
//
//    private List<Token> getConstructors(Token token)
//    {
//        List<Token> arrlist = new ArrayList<>();
//
//        for (Token toke : token)
//            if (toke.equals(Token.Type.CONSTRUCTOR))
//                arrlist.add(toke);
//
//        return arrlist;
//    }
//}
