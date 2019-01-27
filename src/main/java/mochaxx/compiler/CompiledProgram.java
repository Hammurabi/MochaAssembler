package mochaxx.compiler;

//import mochaxx.compiler.structure.Function;
import mochaxx.compiler.structure.Struct;
import mochaxx.compiler.structure.builtin_types.builtin_types;
import mochaxx.compiler.structure.Struct;

import java.util.*;

import static mochaxx.compiler.Token.Type.*;

public class CompiledProgram
{
    protected Map<String, Struct> mGlobalMap;

    public CompiledProgram(OptimizedProgram program) throws CloneNotSupportedException
    {
        this(program, false);
    }

    public CompiledProgram(OptimizedProgram program, boolean pre) throws CloneNotSupportedException
    {
        mGlobalMap = new HashMap<>();

        mGlobalMap.put("byte", new builtin_types.int_8(true));
        mGlobalMap.put("bool", new builtin_types.bool());
        mGlobalMap.put("char", new builtin_types.int_8(true));
        mGlobalMap.put("short", new builtin_types.int_16(true));
        mGlobalMap.put("int", new builtin_types.int_32(true));
        mGlobalMap.put("long", new builtin_types.int_64(true));
        mGlobalMap.put("float", new builtin_types.float_32());
        mGlobalMap.put("double", new builtin_types.float_64());

        Queue<Token> tokens = program.getTokens();

        for (Token token : tokens)
        {
            if (token.getType().equals(Token.Type.TYPEDEF))
            {
            }
            else
            {
                Struct struct = new Struct(token);
                if (pre)
                    continue;
                if (mGlobalMap.containsKey(struct.getName()))
                {
                    System.err.println("compile-err: redeclaration of '" + struct.getName() + "'.");
                    System.exit(0);
                }
                mGlobalMap.put(struct.getName(), struct);
                struct.setTemplate(token.get(TEMPLATE));

                Token body      = token.get(BRACES);
                if (body == null)
                    continue;

                for (Token field : body)
                    if (field.equals(EMPTY_DECLARATION))
                    {
                        if(!struct.addField(field.get(NAME).data))
                        {
                            if (pre) continue;
                            System.err.println("compile-err: redeclaration of '" + field.get(NAME).data + "' in struct '" + struct.getName() + "'.");
                            System.exit(0);
                        }
                    }
                    else if (field.equals(METHOD_DECLARATION) || field.equals(METHOD_EMPTY_DECLARATION) || field.equals(CONSTRUCTOR))
                    {
                        if (field.equals(CONSTRUCTOR))
                        {
                        }
                        else if (!struct.addMethod(field.get(NAME).data))
                        {
                            if (pre) continue;
                            System.err.println("compile-err: redeclaration of '" + field.get(NAME).data + "' in struct '" + struct.getName() + "'.");
                            System.exit(0);
                        }
                    }
            }
        }

        for (Token token : tokens)
        {
            if (token.getType().equals(Token.Type.TYPEDEF))
            {
                boolean signed = !token.get(TYPENAME).modifiers.contains(Modifier.UNSIGNED);
//                boolean constn = token.get(TYPENAME).modifiers.contains(Modifier.CONST) || token.get(TYPENAME).modifiers.contains(Modifier.FINAL);

                if (token.get(TYPENAME).get(STATEMENT) != null)
                    mGlobalMap.put(token.get(NAME).data, mGlobalMap.get(token.get(TYPENAME).get(STATEMENT).getChild(0).data).getSign(signed));
                else if (token.get(TYPENAME).get(SUBSCRIPT) != null)
                    mGlobalMap.put(token.get(NAME).data, new builtin_types.array(mGlobalMap.get(token.get(TYPENAME).get(SUBSCRIPT).get(IDENTIFIER).data).getSign(signed), Long.parseLong(token.get(TYPENAME).get(SUBSCRIPT).get(BRACKETS).get(NUMBER).data)));
            }
        }

        if (pre)
            return;
        else {
            Map<String, byte[]> nativeFunctions = new HashMap<>();
            Map<String, Long>   functions = new HashMap<>();
//            List<Function> funcs = new ArrayList<>();

            for (Token token : tokens)
            {
                if (token.equals(CLASS_DECLARATION))
                {
                    for (Token field : token.get(BRACES))
                    {
                        if (field.equals(METHOD_DECLARATION) || field.equals(METHOD_EMPTY_DECLARATION) || field.equals(CONSTRUCTOR))
                        {
                            String funcName = token.get(NAME).data + "::" + field.get(NAME) + "";

                            /**
                             * A ripemd(sha256(funcNode)) unique signature is generated (20) bytes
                             * the vm will find this native function by it's signature in a big
                             * native function map.
                             */
                            if (field.isModifier(Modifier.NATIVE))
                            {
                                if (!field.isModifier(Modifier.STATIC))
                                {
                                    System.err.println("compile-err: native functions can only be static.");
                                    System.err.println("errstr: line" + field.line + " offset" + field.offset);

                                    System.exit(0);
                                }

//                                nativeFunctions.put(funcName, HashUtil.applyRipeMD160(HashUtil.applySha256(field.humanReadable().getBytes())));
                            }
                            else
                            {
                                functions.put(funcName, (long) functions.size());
//                                funcs.add(new Function(funcName, functions.get(funcName), field, token, tokens));
                            }
                        }
                    }
                }
            }

//            for (Function function : funcs)
//                function.compile();

            return;
        }

//        for (Token token : tokens)
//        {
//            if (token.getType().equals(CLASS_DECLARATION))
//            {
//                Struct struct = mGlobalMap.get(token.get(Token.Type.NAME).data);
//                Token template = struct.getTemplate();
//                if (template == null)
//                    continue;
//
//                Struct[] generics = new Struct[template.children.size()];
//                for (int i = 0; i < template.children.size(); i ++)
//                {
//                    Struct gen = struct.getType(template.getChild(i).get(NAME).data, mGlobalMap);
//                    if (gen == null)
//                    {
//                        System.err.println("compile-err: generic type set '" + template.getChild(i).get(NAME).data + "' does not exist.");
//                        System.exit(0);
//                    }
//                    generics[i] = gen;
//                }
//
//                struct = struct.setGenerics(generics);
//
//                mGlobalMap.put(token.get(Token.Type.NAME).data, struct);
//            }
//        }
//
//        for (Token token : tokens)
//        {
//            if (token.getType().equals(Token.Type.TYPEDEF))
//            {
//            }
//            else
//            {
//                Struct struct = mGlobalMap.get(token.get(Token.Type.NAME).data);
//                Token body      = token.get(BRACES);
//
//                if (body == null)
//                    continue;
//
//                for (Token field : body)
//                    if (field.equals(EMPTY_DECLARATION))
//                    {
//                        Field f = struct.getField(field.get(NAME).data);
//                        Struct type = mGlobalMap.get(field.get(VARTYPE).data);
//                        if (type == null && !struct.isTemplateType(field.get(VARTYPE).data))
//                        {
//                            System.err.println(struct.isTemplateType(field.get(VARTYPE).data) + " aspkdpakp");
//                            System.err.println("compile-err: type '" + field.get(VARTYPE) + "' does not exist.");
//                            System.exit(0);
//                        }
//
//                        f.setModifiers(token.modifiers);
//                        f.setType(type);
//                        f.setGenerics(field.get(GENERIC_SPEC), mGlobalMap, struct);
//                    }
//            }
//        }
//
//        for (Token token : tokens)
//        {
//            if (token.getType().equals(Token.Type.TYPEDEF))
//            {
//            }
//            else
//            {
//                Struct struct = mGlobalMap.get(token.get(Token.Type.NAME).data);
//                Token body      = token.get(BRACES);
//
//                if (body == null)
//                    continue;
//
//                for (Token field : body)
//                    if (field.equals(METHOD_DECLARATION) || field.equals(METHOD_EMPTY_DECLARATION) || field.equals(CONSTRUCTOR))
//                    {
//                        Method f = struct.getMethod(field.get(NAME).data);
//                        f.setModifiers(field.modifiers);
//
//                        if (field.get(VARTYPE).data.equals("void"))
//                            f.setType(new builtin_types.Void());
//                        else if (!struct.isTemplateType(field.get(VARTYPE).data) && mGlobalMap.get(field.get(VARTYPE).data) != null)
//                            f.setType(mGlobalMap.get(field.get(VARTYPE).data));
//                        else
//                        {
//                            System.err.println("compile-err: return type '" + field.get(VARTYPE) + "' is not a valid type.");
//                            System.exit(0);
//                        }
//
//                        f.setGenerics(field.get(GENERIC_SPEC), mGlobalMap, struct);
//                        f.setTemplate(field.get(TEMPLATE));
//                        f.setParenthesis(field.get(PARENTHESIS));
//                    }
//            }
//        }
    }
}