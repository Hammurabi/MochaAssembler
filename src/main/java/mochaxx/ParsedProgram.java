package mochaxx;

import mochaxx.compiler.*;

import java.util.*;

import static mochaxx.compiler.InfixToPostFixEvalution.convertToPostfix;
import static mochaxx.compiler.Token.Type.*;

public class ParsedProgram
{
    private Queue<Token> mTokens;

    public ParsedProgram(LexedProgram lexedProgram)
    {
        mTokens = new LinkedList<>();

        mTokens.addAll(packClasses(new TokenStream(lexedProgram.getCleanTokens())));

        for (Token struct : mTokens)
            packClassFields(struct);

        /**
         * struct sizes are available at this point.
         */
        for (Token struct : mTokens)
            organizeMethodBodies(struct);

        for (Token token : mTokens)
            System.out.println(token.humanReadable());
    }

    public static void organizeMethodBodies(Token tk)
    {
        for (Token token : tk.getChild(3))
        {
            if (token.equals(CONSTRUCTOR) || token.equals(METHOD_EMPTY_DECLARATION) || token.equals(METHOD_DECLARATION))
            {
                Token method = token;

                organizeMethodBody(method.get(BRACES), method);
            }
        }
    }

    private static void organizeMethodBody(Token method, Token meta)
    {
        Queue<Token> out    = organize(new TokenStream(method.children), false);
//        out                 = mul_recursive(new TokenStream(out), false);
//        out                 = add_recursive(new TokenStream(out), false);
//        out                 = recursiveDecompose(new TokenStream(out), false);

        out = fixMath(out);
        out = lastStep(new TokenStream(out));
        out = optimize(new TokenStream(out));

        method.children.clear();
        method.children.addAll(out);
    }

    private static final Queue<Token> lastStep(TokenStream in)
    {
        Queue<Token>    out     = new LinkedList<>();
        Set<Modifier>   mod     = new LinkedHashSet<>();

        while (in.tokens.size() > 0)
        {
            while (in.peek() != null && in.peek().isModifier())
                mod.add(in.poll().asModifier());

            if (in.matches(EMPTY_DECLARATION, ASSIGNMENT))
                ((LinkedList<Token>) out).add(in.poll().add(in.poll().setType(EQUALS)).setType(FULL_DECLARATION));
            else if (in.matches(SUBSCRIPT, ASSIGNMENT))
                ((LinkedList<Token>) out).add(in.poll().setType(SUBSCRIPT_ASSIGNMENT).add(in.poll().setType(EQUALS)));
            else if (in.matches(ANY, ASSIGNMENT))
                ((LinkedList<Token>) out).add(in.poll().add(in.poll().setType(EQUALS)).setType(ASSIGNMENT));
            else
                ((LinkedList<Token>) out).add(in.poll());
        }

        return out;
    }

    private static final Queue<Token> optimize(TokenStream in)
    {
        Queue<Token>    out     = new LinkedList<>();
        Set<Modifier>   mod     = new LinkedHashSet<>();

        while (in.tokens.size() > 0)
        {
            while (in.peek() != null && in.peek().isModifier())
                mod.add(in.poll().asModifier());

            if (in.matches(STATEMENT))
                System.err.println("statement erased: " + in.poll());
            else
                ((LinkedList<Token>) out).add(in.poll());
        }

        return out;
    }

    private static final Queue<Token> fixMath(Collection<Token> tokens)
    {

        for (Token token : tokens)
        {
            Queue<Token> t = fixMath(token.children);
            token.children.clear();
            token.add(t);
        }

//        Queue<Token> out = ;


        return new LinkedList<>(convertToPostfix(tokens.toArray(new Token[tokens.size()])));
    }

    private static Queue<Token> add_recursive(TokenStream in, boolean once)
    {
        for (Token token : in.tokens)
        {
            Queue<Token> tokens = add_recursive(new TokenStream(token.children), once);
            token.children.clear();

            token.add(tokens);
        }

        return add(in, once);
    }

    private static Queue<Token> recursiveDecompose(TokenStream in, boolean once)
    {
        for (Token token : in.tokens)
        {
            Queue<Token> tokens = recursiveDecompose(new TokenStream(token.children), once);
            token.children.clear();

            token.add(tokens);
        }

        Queue<Token> tokens = mul(in, once);
        tokens              = dec_sub(new TokenStream(tokens), once);

        return tokens;
    }

    private static Queue<Token> mul_recursive(TokenStream in, boolean once)
    {
        for (Token token : in.tokens)
        {
            Queue<Token> tokens = mul_recursive(new TokenStream(token.children), once);
            token.children.clear();

            token.add(tokens);
        }

//        Queue<Token> tokens = mul(in, once);

        return mul(in, once);//math_mul(new TokenStream(mul(new TokenStream(tokens), once)), once);
    }

    private static Queue<Token> math_mul(TokenStream in, boolean once)
    {
        Queue<Token>    out     = new LinkedList<>();
        Set<Modifier>   mod     = new LinkedHashSet<>();

        int round = 0;

        while (in.tokens.size() > 0)
        {
            if (round ++ > 0 && once)
                return out;

            while (in.peek() != null && in.peek().isModifier())
                mod.add(in.poll().asModifier());

            /**
             * Standard field declaration (must be on one line).
             */

            if (in.matches(END))
                in.poll();

            /**
             * a * b
             */
            else if (in.matches(ANY, MULTIPLICATION, ANY_NOT_END))
            {
                Token a = in.poll();
                in.poll();
                Token b = in.poll().setType(STATEMENT);

                ((LinkedList<Token>) out).add(new Token(MULTIPLICATION).add(a).add(b));
            }

            else if (in.matches(MULTIPLICATION, ANY_NOT_END))
            {
                in.poll();
                Token a = in.poll();

                try{
                    Token b = ((LinkedList<Token>) out).getLast();

                    ((LinkedList<Token>) out).add(new Token(SUBTRACTION).add(a).add(b));
                } catch (NoSuchElementException e)
                {
//                    ((LinkedList<Token>) out).add(a);
                    errstr(a, "statement started with multiplication");
                }
            }
            else if (in.peek() != null)
                ((LinkedList<Token>) out).add(in.poll());
            else
                errstr(in, "errors in stream");
        }

        return out;
    }

    private static Queue<Token> dec_sub(TokenStream in, boolean once)
    {
        Queue<Token>    out     = new LinkedList<>();
        Set<Modifier>   mod     = new LinkedHashSet<>();

        int round = 0;

        while (in.tokens.size() > 0)
        {
            if (round ++ > 0 && once)
                return out;

            while (in.peek() != null && in.peek().isModifier())
                mod.add(in.poll().asModifier());

            if (in.matches(END))
                in.poll();

            else if (in.matches(UNARY_MINUS, UNARY_MINUS))
            {
                while (in.matches(UNARY_MINUS, UNARY_MINUS))
                {
                    ((LinkedList<Token>) out).add(in.poll());
                    ((LinkedList<Token>) out).add(new Token(SUBTRACTION));
                    ((LinkedList<Token>) out).addAll(in.poll().children);
                }
            }

            else if (in.peek() != null)
                ((LinkedList<Token>) out).add(in.poll());
            else
                errstr(in, "errors in stream");
        }

        return out;
    }

    private static Queue<Token> add(TokenStream in, boolean once)
    {
        Queue<Token>    out     = new LinkedList<>();
        Set<Modifier>   mod     = new LinkedHashSet<>();

        int round = 0;

        while (in.tokens.size() > 0)
        {
            if (round ++ > 0 && once)
                return out;

            while (in.peek() != null && in.peek().isModifier())
                mod.add(in.poll().asModifier());

            if (in.matches(END))
                in.poll();

            else if (in.matches(ANY_NOT_END, ADDITION, ADDITION))
            {
                ((LinkedList<Token>) out).add(new Token(UNARY_PLUSPREFIX).add(in.poll()));

                in.poll();
                in.poll();
            }

            else if (in.matches(SUBTRACTION, SUBTRACTION, ANY_NOT_END))
            {
                Token unaryOp = new Token(UNARY_MINUSPOSTFIX);

                in.poll();
                in.poll();

                ((LinkedList<Token>) out).add(unaryOp.add(in.poll()));
            }

            else if (in.matches(ANY_NOT_END, SUBTRACTION, SUBTRACTION))
            {
                ((LinkedList<Token>) out).add(new Token(UNARY_MINUSPREFIX).add(in.poll()));

                in.poll();
                in.poll();
            }

            else if (in.matches(ADDITION, ADDITION, ANY_NOT_END))
            {
                Token unaryOp = new Token(UNARY_PLUSPOSTFIX);

                in.poll();
                in.poll();

                ((LinkedList<Token>) out).add(unaryOp.add(in.poll()));
            }

            /**
             * + ANYTHING
             */
            else if (in.matches(ADDITION, ANY_NOT_END))
            {
                in.poll();
                Token a = in.poll();

                try{
                    Token b = ((LinkedList<Token>) out).getLast();

                    ((LinkedList<Token>) out).add(new Token(ADDITION).add(a).add(b));
                } catch (NoSuchElementException e)
                {
                    errstr(a, "cannot start a statement with addition.");
                }
            }

            /**
             * - ANYTHING
             */
            else if (in.matches(SUBTRACTION, ANY_NOT_END))
            {
                in.poll();
                Token a = in.poll();

                try{
                    Token b = ((LinkedList<Token>) out).getLast();

                    ((LinkedList<Token>) out).add(new Token(SUBTRACTION).add(a).add(b));
                } catch (NoSuchElementException e)
                {
                    ((LinkedList<Token>) out).add(new Token(UNARY_MINUS).add(a));
                }
            }


            /**
             * ANYTHING - ANYTHING
             */
            else if (in.matches(ANY_NOT_END, SUBTRACTION, ANY_NOT_END))
            {
                Token a = in.poll();
                in.poll();
                Token b = in.poll();

                ((LinkedList<Token>) out).add(new Token(SUBTRACTION).add(a).add(b));
            }

            /**
             * ANYTHING + ANYTHING
             */
            else if (in.matches(ANY_NOT_END, ADDITION, ANY_NOT_END))
            {
                Token a = in.poll();
                in.poll();
                Token b = in.poll();

                ((LinkedList<Token>) out).add(new Token(ADDITION).add(a).add(b));
            }
            else if (in.peek() != null)
                ((LinkedList<Token>) out).add(in.poll());
            else
                errstr(in, "errors in stream");
        }

        return out;
    }

    private static Queue<Token> mul(TokenStream in, boolean once)
    {
        Queue<Token>    out     = new LinkedList<>();
        Set<Modifier>   mod     = new LinkedHashSet<>();

        int round = 0;

        while (in.tokens.size() > 0)
        {
            if (round ++ > 0 && once)
                return out;

            while (in.peek() != null && in.peek().isModifier())
                mod.add(in.poll().asModifier());

            /**
             * Standard field declaration (must be on one line).
             */

            if (in.matches(END))
                in.poll();

            /**
             * Dereference * Token
             */
            else if (in.matches(DEREFERENCE, DEREFERENCE))
            {
                Token a = in.poll();
                Token b = in.poll().setType(STATEMENT);

                ((LinkedList<Token>) out).add(new Token(MULTIPLICATION).add(a).add(b.children));
//
//
//                ((LinkedList<Token>) out).add(a);
//                ((LinkedList<Token>) out).add(new Token(MULTIPLICATION));
//                ((LinkedList<Token>) out).addAll(b.children);
            }

//            if (sub_match(in, DEREFERENCE))
//            {
//                in.poll();
//                ((LinkedList<Token>) out).add(new Token(UNARY_MINUS).add(in.poll()));
//            }
//
//            else if (sub_match(in) && in.matches(ANY, ANY, DEREFERENCE))
//            {
//                in.poll();
//                ((LinkedList<Token>) out).add(new Token(UNARY_MINUS).add(in.poll()));
//
//                Token a = in.poll();
//
////                ((LinkedList<Token>) out).add(new Token(MULTIPLICATION).add(a).add(b.children));
//                ((LinkedList<Token>) out).add(new Token(MULTIPLICATION));
//
//                ((LinkedList<Token>) out).add(a);
//            }

            else if (in.matches(DEREFERENCE, ANY_NOT_END) && !in.matches(DEREFERENCE, SUBTRACTION))
            {
                Token a = in.poll();
                Token b = in.poll().setType(STATEMENT);

                ((LinkedList<Token>) out).add(new Token(MULTIPLICATION).add(a).add(b.children));
//
//                ((LinkedList<Token>) out).add(a);
//                ((LinkedList<Token>) out).add(new Token(MULTIPLICATION));
//                ((LinkedList<Token>) out).addAll(b.children);
            }
            else if (in.matches(ANY, DEREFERENCE))
            {
                Token a = in.poll();
                Token b = in.poll().setType(STATEMENT);

                ((LinkedList<Token>) out).add(new Token(MULTIPLICATION).add(a).add(b.children));
//
//                ((LinkedList<Token>) out).add(a);
//                ((LinkedList<Token>) out).add(new Token(MULTIPLICATION));
//                ((LinkedList<Token>) out).addAll(b.children);
            }
            else if (in.peek() != null)
                ((LinkedList<Token>) out).add(in.poll());
            else
                errstr(in, "errors in stream");
        }

        return out;
    }

    private static boolean sub_match(TokenStream in, Token.Type type)
    {
        return in.matches(SUBTRACTION, type);
    }

    private static boolean math_match(Token.Type type, TokenStream in)
    {
        return in.matches(type, ADDITION) || in.matches(type, SUBTRACTION) || in.matches(type, SUBDIVISION);
    }

    private static boolean math_match(TokenStream in)
    {
        return InfixToPostFixEvalution.isOperator(in.peek());
    }

    private static boolean sub_match(Token.Type type, TokenStream in)
    {
        return in.matches(type, SUBTRACTION);
    }

    private static boolean sub_match(TokenStream in)
    {
        return in.matches(SUBTRACTION);
    }

    private static Queue<Token> organize(TokenStream in, boolean once)
    {
        return organize(in, once, false);
    }

    private static Queue<Token> organize(TokenStream in, boolean once, boolean tilLignsEnd)
    {
        return organize(in, once, tilLignsEnd, false);
    }

    private static Queue<Token> organize(TokenStream in, boolean once, boolean tilLignsEnd, boolean dereference)
    {
        return organize(in, once, tilLignsEnd, dereference, false);
    }

    private static Queue<Token> organize(TokenStream in, boolean once, boolean tilLignsEnd, boolean dereference, boolean address)
    {
        Queue<Token>    out     = new LinkedList<>();
        Set<Modifier>   mod     = new LinkedHashSet<>();

        int round = 0;

        while (in.tokens.size() > 0)
        {
            if (round ++ == 1 && once)
                return out;

            while (in.peek() != null && in.peek().isModifier())
                mod.add(in.poll().asModifier());

            if (mod.size() > 0)
                errstr(in.peek(), "modifiers not allowed inside method bodies");

            /**
             * Standard field declaration (must be on one line).
             */

            if (in.matches(END))
            {
                in.poll();
                if (tilLignsEnd)
                    return out;
            }

            else if (in.matches(TRY))
            {
                ws(in);

                Token trie = in.poll();

                if (in.matches(BRACES_OPEN))
                {
                    Token body = closeBrace(in.tokens);

                    Queue<Token> q = organize(new TokenStream(body.children), false);

                    body.children.clear();
                    body.add(q);

                    trie.add(body);
                } else if (in.matches(BRACES))
                {
                    Token body = in.poll();

                    Queue<Token> q = organize(new TokenStream(body.children), false);

                    body.children.clear();
                    body.add(q);

                    trie.add(body);
                }
                else
                    errstr(trie, "try body missing");

                ws(in);

                if (!in.matches(CATCH))
                    errstr(trie, "try 'catch' is missing");

                Token catche = in.poll();

                ws(in);

                if (in.matches(PARENTHESIS_OPEN))
                {
                    Token body = closeParenthesis(in.tokens);

                    Queue<Token> q = organize(new TokenStream(body.children), false);

                    body.children.clear();
                    body.add(q);

                    catche.add(body);
                } else if (in.matches(PARENTHESIS))
                {
                    Token body = in.poll();

                    Queue<Token> q = organize(new TokenStream(body.children), false);

                    body.children.clear();
                    body.add(q);

                    catche.add(body);
                }
                else
                    errstr(catche, "catch clause missing");

                ws(in);

                if (in.matches(BRACES_OPEN))
                {
                    Token body = closeBrace(in.tokens);

                    Queue<Token> q = organize(new TokenStream(body.children), false);

                    body.children.clear();
                    body.add(q);

                    catche.add(body);
                } else if (in.matches(BRACES))
                    catche.add(in.poll());
                else
                    errstr(catche, "catch body missing");

                ((LinkedList<Token>) out).add(new Token(TRYCATCH).add(trie).add(catche));
            }

            else if (in.matches(IDENTIFIER, BRACKETS_OPEN) || in.matches(IDENTIFIER, BRACKETS))
            {
                Token subscript = new Token(SUBSCRIPT);

                subscript.add(in.poll());

                Token brackets = null;

                if (in.peek().equals(BRACKETS_OPEN))
                    brackets = closeBrackets(in.tokens);
                else if (in.peek().equals(BRACKETS))
                    brackets = in.poll();

                Queue<Token> q = organize(new TokenStream(brackets.children), false);

                brackets.children.clear();
                brackets.add(q);

                ((LinkedList<Token>) out).add(subscript.add(brackets));
            }

            else if (in.matches(IDENTIFIER, COLON) && !in.matches(IDENTIFIER, COLON, COLON))
            {
                Token mark = new Token(MARK);

                ((LinkedList<Token>) out).add(mark.setData(in.poll().data));
                in.poll();
            }

            else if (in.matches(GOTO, IDENTIFIER))
                ((LinkedList<Token>) out).add(in.poll().setData(in.poll().data));

            else if (in.matches(TYPEDEF, ANY_NOT_END, ANY_NOT_END))
            {
                Token typedef   = in.poll();
                if (in.peek().data.equals("unsigned"))
                    mod.add(in.poll().asModifier());

                Token type      = new Token(IDENTIFIER).setModifiers(mod);

                while (in.peek() != null && !in.peek().equals(END))
                    type.add(in.poll());

                Token name      = type.children.get(type.children.size() - 1);
                type.children.remove(type.children.size() - 1);

                ((LinkedList<Token>) out).add(typedef.add(type.setType(TYPENAME)).add(name.setType(NAME)));
            }

            else if (in.matches(PARENTHESIS_CLOSED) || in.matches(BRACES_CLOSED) || in.matches(BRACKETS_CLOSED))
            {
                ((LinkedList<Token>) out).add(in.poll());
                if (tilLignsEnd)
                    return out;
            }

            else if (in.matches(ADDITION, EQUALS))
            {
                in.poll();
                in.poll();

                ((LinkedList<Token>) out).add(new Token(ASSIGNMENT).add(new Token(PLUSEQUALS).add(organize(in, true))));
            }

            else if (in.matches(SUBTRACTION, EQUALS))
            {
                in.poll();
                in.poll();

                ((LinkedList<Token>) out).add(new Token(ASSIGNMENT).add(new Token(MINUSEQUALS).add(organize(in, true))));
            }

            else if (in.matches(NUMBER))
            {
                Token t = null;
                String toke = (t = in.poll()).data;
                while (in.matches(NUMBER) || in.matches(PROCEDURAL_ACCESS))
                    toke += in.poll().data;

                if (toke.contains("."))
                    ((LinkedList<Token>) out).add(new Token(DECIMAL).setData(toke).dataFrom(t));
                else
                    ((LinkedList<Token>) out).add(new Token(NUMBER).setData(toke).dataFrom(t));
            }

            else if (in.matches(FOR))
            {
                Token floop = in.poll();

                ws(in);
                if (!in.matches(PARENTHESIS_OPEN))
                    errstr(floop, "for loop syntax incorrect (must be followed by parenthesis)");

                Token parenthesis = closeParenthesis(in.tokens);

                Queue<Token> t = organize(new TokenStream(parenthesis.children), false);

                parenthesis.children.clear();
                parenthesis.add(t);

                ws(in);
                if (!in.matches(BRACES_OPEN) && !in.matches(BRACES))
                    errstr(floop, "for loop syntax incorrect (must be followed by braces)");

                Token body        = in.matches(BRACES_OPEN) ? closeBrace(in.tokens) : in.poll();

                t = organize(new TokenStream(body.children), false);
                body.children.clear();
                body.add(t);

                ((LinkedList<Token>) out).add(floop.add(parenthesis).add(body));
            }

            else if (in.matches(TEMPLATE))
                errstr(in, "templates not allowed inside method bodies");

            else if (in.matches(CLASS))
                errstr(in, "class declarations not allowed inside method bodies");

            else if (in.matches(UNION))
                errstr(in, "union declarations not allowed inside method bodies");

            else if (in.matches(PARENTHESIS_OPEN, IDENTIFIER, PARENTHESIS_CLOSED))
            {
                in.poll();
                ((LinkedList<Token>) out).add(new Token(CAST).add(in.poll()));
                in.poll();

                if (in.peek() == null)
                    errstr(((LinkedList<Token>) out).peekLast(), "casting at the end of a statement");

                if (in.peek().equals(END))
                    errstr(((LinkedList<Token>) out).peekLast(), "casting at the end of a statement");

                ((LinkedList<Token>) out).peekLast().add(organize(in, true, true));
            }

            else if (in.matches(ADDITION, ADDITION))
            {
                in.poll();
                in.poll();
                ((LinkedList<Token>) out).add(new Token(INCREMENT));
            }

            else if (in.matches(SUBTRACTION, SUBTRACTION))
            {
                in.poll();
                in.poll();
                ((LinkedList<Token>) out).add(new Token(DECREMENT));
            }

            else if (in.matches(LESS_THAN, LESS_THAN, EQUALS))
            {
                in.poll();
                in.poll();
                ((LinkedList<Token>) out).add(new Token(LEFT_SHIFTEQUALS));
            }

            else if (in.matches(LESS_THAN, LESS_THAN))
            {
                in.poll();
                in.poll();
                ((LinkedList<Token>) out).add(new Token(LEFT_SHIFT));
            }

            else if (in.matches(MORE_THAN, MORE_THAN, EQUALS))
            {
                in.poll();
                in.poll();
                ((LinkedList<Token>) out).add(new Token(RIGHT_SHIFTEQUALS));
            }

            else if (in.matches(MORE_THAN, MORE_THAN))
            {
                in.poll();
                in.poll();
                ((LinkedList<Token>) out).add(new Token(RIGHT_SHIFT));
            }

            else if (in.matches(OR, OR))
            {
                in.poll();
                in.poll();
                ((LinkedList<Token>) out).add(new Token(LOR));
            }

            else if (in.matches(OR, EQUALS))
            {
                in.poll();
                in.poll();
                ((LinkedList<Token>) out).add(new Token(OR_EQUALS));
            }

            else if (in.matches(XOR, EQUALS))
            {
                in.poll();
                in.poll();
                ((LinkedList<Token>) out).add(new Token(XOR_EQUALS));
            }

            else if (in.matches(AND, EQUALS))
            {
                in.poll();
                in.poll();
                ((LinkedList<Token>) out).add(new Token(AND_EQUALS));
            }

            /**
             * This is a dereference
             */
            else if (in.matches(MULTIPLICATION, MULTIPLICATION))
            {
                if (dereference)
                {
                    in.poll();
                    in.poll();
                    Token D     = new Token(DEREFERENCE);
                    Token deref = new Token(DEREFERENCE);
                    D.add(deref.add(organize(in, true, false, true)));
                    ((LinkedList<Token>) out).add(D);
                }
                else {
                    if (out.size() > 0)
                    {
                        ((LinkedList<Token>) out).add(in.poll());


                        in.poll();
                        Token deref = new Token(DEREFERENCE);

                        ((LinkedList<Token>) out).add(deref.add(organize(in, true, false, true)));
                    }
                    else
                    {
                        in.poll();


                        in.poll();
                        Token d     = new Token(DEREFERENCE);
                        Token deref = new Token(DEREFERENCE);

                        ((LinkedList<Token>) out).add(d.add(deref.add(organize(in, true, false, true))));
                    }
                }
            }

            else if (out.size() == 0 && in.matches(MULTIPLICATION, ANY_NOT_END))
            {
                Token dereferens = new Token(DEREFERENCE);
                in.poll();
                ((LinkedList<Token>) out).add(dereferens.add(organize(in, true, false, true)));
            }

            else if (out.size() == 0 && in.matches(AND, ANY_NOT_END) && !in.matches(AND, AND))
            {
                Token dereferens = new Token(ADDRESS);
                in.poll();
                ((LinkedList<Token>) out).add(dereferens.add(organize(in, true, false, false, true)));
            }

            else if (out.size() == 0 && in.matches(AND, AND))
            {
                in.poll();
                in.poll();
                ((LinkedList<Token>) out).add(new Token(LAND));
            }

//            else if (in.matches(AND, ANY_NOT_END))
//            {
//                in.poll();
//
//                ((LinkedList<Token>) out).add(new Token(ADDRESS).add(organize(in, true, false, false, true)));
//            }

//            else if (out.size() == 0 && in.matches(SUBTRACTION, ANY_NOT_END))
//            {
//                in.poll();
//                ((LinkedList<Token>) out).add(new Token(LAND).add(organize(in, true)));
//            }

            else if (math_match(in) && in.matches(ANY, SUBTRACTION, ANY_NOT_END))
            {
                ((LinkedList<Token>) out).add(in.poll());
                in.poll();
                ((LinkedList<Token>) out).add(new Token(UNARY_MINUS).add(organize(in, true)));
            }

            //TODO: removed as a test.

//            else if (in.matches(MULTIPLICATION))
//            {
//                while (in.matches(MULTIPLICATION))
//                {
//                    Token dereference = new Token(DEREFERENCE);
//                    in.poll();
//
//                    dereference.add(organize(in, true));
//
//                    ((LinkedList<Token>) out).add(dereference);
//                }
//            }

//            else if (in.matches(SUBTRACTION))
//            {
//                Token dereference = new Token(UNARY_MINUS);
//                in.poll();
//
//                dereference.add(organize(in, true));
//
//                ((LinkedList<Token>) out).add(dereference);
//            }

            else if (in.matches(EQUALS, EQUALS))
            {
//                ((LinkedList<Token>) out).add(in.poll());
//                ((LinkedList<Token>) out).add(in.poll());
                in.poll();
                in.poll();

                ((LinkedList<Token>) out).add(new Token(ASSERT));
            }

            else if (in.matches(EQUALS))
            {
                Token assignment = new Token(ASSIGNMENT);

                in.poll();

                ((LinkedList<Token>) out).add(assignment.add(organize(in, false, true)));
            }

            //TODO: removed as a test.
//            else if (in.matches(AND))
//            {
//                Token address = new Token(ADDRESS);
//                in.poll();
//
//                address.add(organize(in, true));
//
//                ((LinkedList<Token>) out).add(address);
//            }

            else if (in.matches(PARENTHESIS_OPEN))
            {
                Token token = closeParenthesis(in.tokens);

                debug = true;
                Queue<Token> tokes = organize(new TokenStream(token.children), false);
                debug = false;
                token.children.clear();
                token.add(tokes);

                ((LinkedList<Token>) out).add(token);
            }

            else if (in.matches(BRACES_OPEN))
            {
                Token token = closeBrace(in.tokens);

                Queue<Token> tokes = organize(new TokenStream(token.children), false);
                token.children.clear();
                token.add(tokes);
                ((LinkedList<Token>) out).add(token);
            }

            else if (in.matches(IDENTIFIER))
            {
                Token statement = new Token(STATEMENT).setModifiers(mod);
                Token typenamet = in.poll();
                Token generictt = new Token(UNDEFINED);

                /**
                 * A template exists
                 */
                if (in.matches(LESS_THAN))
                {
                    generictt = new Token(GENERIC_SPEC);
                    in.poll();

                    while (in.matches(IDENTIFIER))
                    {
                        Token generic = in.poll().setType(NAME);
                        generictt.add(generic);
                        if (in.matches(COMMA))
                            in.poll();
                        else if (in.matches(MORE_THAN))
                        {
                            in.poll();
                            break;
                        } else
                            errstr(in, "invalid generic specifier (never closed)");
                    }
                }

//                while (in.matches(MULTIPLICATION) || in.matches(AND))
//                    mod.add(in.poll().asModifier());

//                if (mod.contains(Modifier.POINTER) && mod.contains(Modifier.REFERENCE))
//                    errstr(in, "fields cannot be both pointers and references");

                /**
                 * This is a declaration of <?></?></?>
                 */
                if (in.matches(IDENTIFIER))
                {
                    typenamet.setType(VARTYPE);
                    Token name = in.poll().setType(NAME);

                    /**
                     * Method declaration
                     */
                    if (in.matches(PARENTHESIS_OPEN))
                        errstr(typenamet, "function declarations not allowed inside function bodies");
                    /**
                     * Field declaration
                     */
                    else if (in.matches(ANY) || in.end())
                        ((LinkedList<Token>) out).add(statement.setType(EMPTY_DECLARATION).add(typenamet.setModifiers(mod).modErrors()).add(generictt).add(name));
                }

                /**
                 * This is a function call.
                 */
                else if (in.matches(PARENTHESIS_OPEN))
                {
                    Token parenthesis = closeParenthesis(in.tokens);
                    Queue<Token> o = organize(new TokenStream(parenthesis.children), false);
                    parenthesis.children.clear();
                    parenthesis.add(o);

                    Token body = new Token(UNDEFINED);

                    if (in.matches(BRACES_OPEN))
                        body = closeBrace(in.tokens);
                    else if (in.matches(BRACES))
                        body = in.poll();

                    if (body.getType().equals(UNDEFINED))
                        statement.setType(METHOD_EMPTY_DECLARATION);
                    else
                        statement.setType(METHOD_DECLARATION);

                    if (!body.getType().equals(UNDEFINED))
                        errstr(typenamet, "constructors are not allowed in functions");

                    Token name = typenamet.setType(NAME);

                    ((LinkedList<Token>) out).add(checkAccess(in, statement.setType(METHOD_CALL).add(name, parenthesis)));
                }
                else
                    ((LinkedList<Token>) out).add(checkAccess(in, statement.add(typenamet, generictt)));
            }
            else if (in.peek() != null)
                ((LinkedList<Token>) out).add(in.poll());
            else
                errstr(in, "errors in stream");
        }

        return out;
    }

    private static Token closeBrace(Queue<Token> in)
    {
        Token token = in.poll().setType(BRACES);

        while (in.size() > 0)
        {
            if (in.peek().getType().equals(Token.Type.BRACES_CLOSED))
            {
                in.poll();
                return token;
            } else if (in.peek().getType().equals(Token.Type.BRACES_OPEN))
                token.add(closeBrace(in));
            else
                token.add(in.poll());//token.add(normalize(new TokenStream(in), true));
        }

        return token;
    }

    private static Token closeParenthesis(Queue<Token> in)
    {
        Token token = in.poll().setType(PARENTHESIS);

        while (in.size() > 0)
        {
            if (in.peek().getType().equals(Token.Type.PARENTHESIS_CLOSED))
            {
                in.poll();
                return token;
            } else if (in.peek().getType().equals(Token.Type.PARENTHESIS_OPEN))
                token.add(closeParenthesis(in));
            else
                token.add(in.poll());//token.add(normalize(new TokenStream(in), true));
        }

        return token;
    }

    private static Token closeBrackets(Queue<Token> in)
    {
        Token token = in.poll().setType(BRACKETS);

        while (in.size() > 0)
        {
            if (in.peek().getType().equals(Token.Type.BRACKETS_CLOSED))
            {
                in.poll();
                return token;
            } else if (in.peek().getType().equals(Token.Type.BRACKETS_OPEN))
                token.add(closeBrackets(in));
            else
                token.add(in.poll());//token.add(normalize(new TokenStream(in), true));
        }

        return token;
    }

    public static Queue<Token> packClassFields(Token struct)
    {
        TokenStream     in      = new TokenStream(struct.getChild(3).children);
        Queue<Token>    out     = new LinkedList<>();
        Set<Modifier>   mod     = new LinkedHashSet<>();
        Stack<Token>    tlt     = new Stack<>();

        while (in.tokens.size() > 0)
        {
            while (in.peek() != null && in.peek().isModifier())
                mod.add(in.poll().asModifier());

            /**
             * Standard field declaration (must be on one line).
             */

            if (in.matches(END))
                in.poll();


            else if (in.matches(EQUALS))
                errstr(in, "instantiation outside of function body");


            else if (in.matches(TEMPLATE))
            {
                Token templateToken = in.poll();
                ws(in);
                if (in.matches(LESS_THAN))
                {
                    in.poll();
                    ws(in);

                    while (in.matches(IDENTIFIER))
                    {
                        ws(in);
                        Token Tn = in.poll();
                        ws(in);

                        if (!in.matches(IDENTIFIER))
                            errstr(in, "incorrect template syntax (no type-specifier)");
                        Token Gt = in.poll().setType(NAME);

                        templateToken.add(new Token(GENERIC).add(Tn).add(Gt));

                        ws(in);
                        if (!in.matches(COMMA) && !in.matches(MORE_THAN))
                            errstr(in, "incorrect template syntax (no comma)");

                        if (in.matches(COMMA))
                            in.poll();
                        else
                            break;
                    }

                    ws(in);

                    if (!in.matches(MORE_THAN))
                        errstr(in, "incorrect template syntax (template never closed)");

                    in.poll();
                    ws(in);
                    tlt.add(templateToken);
                } else
                    errstr(in, "incorrect template syntax.");
            }

            else if (in.matches(IDENTIFIER))
            {
                Token statement = new Token(STATEMENT).setModifiers(mod);
                Token typenamet = in.poll();
                Token templatet = new Token(UNDEFINED);
                if (tlt.size() > 0)
                    templatet = tlt.pop();

                Token generictt = new Token(UNDEFINED);

                /**
                 * A template exists
                 */
                if (in.matches(LESS_THAN))
                {
                    generictt = new Token(GENERIC_SPEC);
                    in.poll();

                    while (in.matches(IDENTIFIER))
                    {
                        Token generic = in.poll().setType(NAME);
                        generictt.add(generic);
                        if (in.matches(COMMA))
                            in.poll();
                        else if (in.matches(MORE_THAN))
                        {
                            in.poll();
                            break;
                        } else
                            errstr(in, "invalid generic specifier (never closed)");
                    }
                }

                if (!templatet.getType().equals(UNDEFINED) && !generictt.getType().equals(UNDEFINED))
                    errstr(typenamet, "fields cannot have both a template and a generic spec");

                while (in.matches(MULTIPLICATION) || in.matches(AND))
                    mod.add(in.poll().asModifier());

                if (mod.contains(Modifier.POINTER) && mod.contains(Modifier.REFERENCE))
                    errstr(in, "fields cannot be both pointers and references");

                /**
                 * This is a declaration of <?></?></?>
                 */
                if (in.matches(IDENTIFIER))
                {
                    typenamet.setType(VARTYPE);
                    Token name = in.poll().setType(NAME);
                    ws(in);

                    /**
                     * Method declaration
                     */
                    if (in.matches(PARENTHESIS_OPEN))
                    {
                        Token parenthesis = closeParenthesis(in.tokens);
                        Token body        = new Token(UNDEFINED);

                        ws(in);

                        if (in.matches(BRACES_OPEN))
                            body = closeBrace(in.tokens);
                        else if (in.matches(BRACES))
                            body = in.poll();

                        if (body.getType().equals(UNDEFINED))
                            statement.setType(METHOD_EMPTY_DECLARATION);
                        else
                            statement.setType(METHOD_DECLARATION);

                        ((LinkedList<Token>) out).add(statement.add(templatet, typenamet.setModifiers(mod).modErrors(), generictt, name, parenthesis, body));
                    }
                    /**
                     * Field declaration
                     */
                    else if (in.matches(ANY) || in.end())
                    {
                        if (mod.contains(Modifier.REFERENCE))
                            errstr(in, "local fields cannot be references");

                        if (!templatet.getType().equals(UNDEFINED))
                            errstr(in, "fields cannot have templates");

                        ((LinkedList<Token>) out).add(statement.setType(EMPTY_DECLARATION).add(typenamet.setModifiers(mod).modErrors()).add(generictt).add(name));
                    }
                }

                /**
                 * This is a function call.
                 */
                else if (in.matches(PARENTHESIS_OPEN))
                {
                    boolean isConstructor = struct.get(NAME).data.equals(typenamet.data);

                    if (!isConstructor)
                    {
                        if (mod.size() > 0)
                            errstr(in, "function calls cannot also be pointers");
                        if (!templatet.equals(UNDEFINED))
                            errstr(in, "function calls cannot have templates");

                        errstr(typenamet, "general statements (function call) not allowed in class bodies");
                    } else
                    {
                        if (mod.size() > 0)
                            errstr(in, "constructors cannot also be pointers");

                        Token parenthesis = closeParenthesis(in.tokens);
                        Token body = new Token(UNDEFINED);

                        ws(in);

                        if (in.matches(BRACES_OPEN))
                            body = closeBrace(in.tokens);
                        else if (in.matches(BRACES))
                            body = in.poll();

                        if (body.getType().equals(UNDEFINED))
                            statement.setType(METHOD_EMPTY_DECLARATION);
                        else
                            statement.setType(METHOD_DECLARATION);

                        if (body.getType().equals(UNDEFINED))
                            errstr(typenamet, "constructors must have bodies");

                        Token name = typenamet.setType(NAME);

                        ((LinkedList<Token>) out).add(statement.setType(CONSTRUCTOR).add(name, parenthesis, body));
                    }
                }
                else
                    errstr(typenamet, "general statements not allowed in class bodies");
            }

            else
                errstr(in, "class-parser: unknown syntax, ");
        }

        struct.getChild(3).children.clear();
        struct.getChild(3).children.addAll(out);

        return out;
    }

    public static Queue<Token> packClasses(TokenStream inout)
    {
        Queue<Token>    out     = new LinkedList<>();
        Set<Modifier>   mod     = new LinkedHashSet<>();
        Stack<Token>    tlt     = new Stack<>();

        while (inout.tokens.size() > 0)
        {
            if (inout.peek().isModifier())
                mod.add(inout.poll().asModifier());

            else if (inout.matches(END))
            {
                inout.poll();
            }



            else if (inout.matches(TEMPLATE))
            {
                Token templateToken = inout.poll();
                ws(inout);
                if (inout.matches(LESS_THAN))
                {
                    inout.poll();
                    ws(inout);

                    while (inout.matches(IDENTIFIER))
                    {
                        ws(inout);
                        Token Tn = inout.poll();
                        ws(inout);

                        if (!inout.matches(IDENTIFIER))
                            errstr(inout, "incorrect template syntax (no type-specifier)");
                        Token Gt = inout.poll().setType(NAME);

                        templateToken.add(new Token(GENERIC).add(Tn).add(Gt));

                        ws(inout);
                        if (!inout.matches(COMMA) && !inout.matches(MORE_THAN))
                            errstr(inout, "incorrect template syntax (no comma)");

                        if (inout.matches(COMMA))
                            inout.poll();
                        else
                            break;
                    }

                    ws(inout);

                    if (!inout.matches(MORE_THAN))
                        errstr(inout, "incorrect template syntax (template never closed)");

                    inout.poll();

                    ws(inout);

                    tlt.add(templateToken);
                } else
                    errstr(inout, "incorrect template syntax.");
            }

            /**
             * Standard class declaration
             */
            else if (inout.matches(Token.Type.CLASS))
            {
                Token tClass = new Token(CLASS_DECLARATION).setModifiers(mod);
                Token tClssi = inout.poll();
                tClass.dataFrom(tClssi);
                    ws(inout);

                if (!inout.matches(IDENTIFIER))
                    errstr(inout, "incorrect class declaration syntax");
                Token name   = inout.poll();
                    ws(inout);

                name.setType(NAME);

                ws(inout);

                /**
                 * Has inheritance.
                 */

                Token parent = new Token(Token.Type.UNDEFINED);

                if (inout.matches(COLON))
                {
                    inout.poll();
                    ws(inout);

                    while (inout.peek().isModifier())
                        mod.add(inout.poll().asModifier());

                    if (inout.matches(IDENTIFIER))
                        parent = (inout.poll());
                    else
                        errstr(inout, "incorrect class inheritance syntax");

                    ws(inout);

                    parent.setType(PARENT_CLASS).setModifiers(mod).modErrors();
                }
                ws(inout);

                Token body = new Token(UNDEFINED);

                Token template = new Token(UNDEFINED);

                if (tlt.size() > 0)
                    template = tlt.pop();

                if (inout.matches(BRACES_OPEN))
                {
                    body   = closeBrace(inout.tokens);

                    ws(inout);

                    tClass.add(template).add(name).add(parent).add(body).setType(Token.Type.CLASS_DECLARATION);
                    tClass.modErrors();
                }
                else
                {
                    ws(inout);
                    tClass.add(template).add(name).add(parent).add(body).setType(Token.Type.EMPTY_CLASS_DECLARATION);
                    tClass.modErrors();
                }

                ((LinkedList<Token>) out).add(tClass);
            }
            else
            {
                errstr(inout, "outside of class is not a valid class declaration");
            }
        }

        return out;
    }

    private static void errstr(TokenStream inout, String err)
    {
        Token token = inout.peek();
        if (token == null)
            token = new Token(UNDEFINED);
        String tokens = "'";
        for (int i = 0; i < 5; i ++)
            if (inout.peek() != null && !inout.peek().equals(END))
                tokens += " " + inout.poll();//.smartString();
        System.err.println("no match for tokens (" + err + "): " + tokens + "'");
        System.err.println("infostr: " + token.infoString());
        System.exit(-1);
    }

    public static void errstr(Token inout, String err)
    {
        System.err.println("err at token: " + inout + " (" + err + ")");
        System.err.println("infostr: " + inout.infoString());
        System.exit(-1);
    }

    private static void ws(TokenStream inout)
    {
        while (inout.peek() != null && inout.peek().equals(END))
            inout.poll();
    }

    /**
     * This function will arrange the tokens in classes.
     */
    public static Queue<Token> normalize(TokenStream inout)
    {
        return normalize(inout, false);
    }

    public static void normalize(Token token)
    {
        Set<Token> old = new LinkedHashSet<>(token.children);
        token.children.clear();
        token.children.addAll(normalize(new TokenStream(old)));
    }

    /**
     * A non recursive linear-prediction parser.
     */
    public static Queue<Token> normalize(TokenStream inout, boolean onlyOnce)
    {
        return normalize(inout, onlyOnce, false);
    }

    static boolean debug = false;

    private static Token checkAccess(TokenStream inout, Token in)
    {
        if (inout.matches(PROCEDURAL_ACCESS))
        {
            Token procAccess = inout.poll();

            procAccess.add(in);

            procAccess.add(organize(inout, true));

            return procAccess;
        }
        else if (inout.matches(COLON, COLON))
        {
            Token statAccess = inout.poll().setType(STATIC_ACCESS);
            inout.poll();

            statAccess.add(in);

            statAccess.add(organize(inout, true));

            return statAccess;
        }
        else return in;
    }

    public static Queue<Token> normalize(TokenStream inout, boolean onlyOnce, boolean nextEndLine)
    {
        Queue<Token>    out     = new LinkedList<>();
        Set<Modifier>   mod     = new LinkedHashSet<>();

        int             numRnd  = 0;

        while (inout.tokens.size() > 0)
        {
            if (numRnd ++ > 0 && onlyOnce)
                break;

            if (debug)
                for (int i = 0; i < Math.min(5, inout.tokens.size()); i ++)
                    System.err.println(inout.tokens.get(i).smartString());

            if (inout.peek().isModifier())
                mod.add(inout.poll().asModifier());

            /**
             * Initialization
             */
            else if (inout.matches(EQUALS))
            {
                try{
                    Token last = ((LinkedList<Token>) out).removeLast();
                    inout.poll();

                    Token initialization = new Token(INITIALIZATION);

                    ((LinkedList<Token>) out).add(initialization.add(last).add(normalize(inout, true)));
                } catch (NoSuchElementException e)
                {
                    Token token = inout.peek();
                    if (token == null)
                        token = new Token(UNDEFINED);

                    String tokens = "";
                    for (int i = 0; i < 5; i ++)
                        if (inout.peek() != null)
                            tokens += " " + inout.poll().smartString();
                    System.err.println("no match for tokens: " + tokens);
                    System.err.println("equals '=' used inappropriately.");
                    System.err.println("infostr: " + token.infoString());

                    e.printStackTrace();
                    System.exit(-1);
                }
            }


//            else if (inout.matches(ANY, MULTIPLICATION))
//            {
//            }

            /**
             * cast
             */
            else if (inout.matches(Token.Type.PARENTHESIS_OPEN, Token.Type.IDENTIFIER, Token.Type.PARENTHESIS_CLOSED))
            {
                Token cast = new Token(Token.Type.CAST);
                ((LinkedList<Token>) out).add(cast.add(inout.poll()));
            }

            /**
             * The right hand side is a method call
             */
            else if (inout.matches(Token.Type.IDENTIFIER, Token.Type.PARENTHESIS_OPEN))
            {
                Token invocation = new Token(Token.Type.METHOD_CALL);

                Token methodName = inout.poll();
                Token parenthesis = closeParenthesis(inout.tokens);

                normalize(parenthesis);

                while (inout.peek().equals(END))
                    inout.poll();

                invocation = checkAccess(inout, invocation);

                ((LinkedList<Token>) out).add(invocation.add(methodName).add(parenthesis));
            }



            /**
             * The right hand is parsed differently
             * Possible multiplication syntax.
             */
            else if (inout.matches(Token.Type.MULTIPLICATION, Token.Type.IDENTIFIER, Token.Type.ANY_NOT_END))
            {
                inout.poll();
                mod.add(Modifier.DEREFERENCE);
                Token identifier = inout.poll().setModifiers(mod);

                Token rest = new Token(VALUE);

                rest.children.addAll(normalize(inout, false, true));

                ((LinkedList<Token>) out).add(checkAccess(inout, identifier));
                ((LinkedList<Token>) out).addAll(rest.children);
            }



            /**
             * The right hand side is an *identifier.
             */
            else if (inout.matches(Token.Type.MULTIPLICATION, Token.Type.IDENTIFIER))
            {
                inout.poll();
                mod.add(Modifier.DEREFERENCE);
                Token identifier = inout.poll().setModifiers(mod);

                ((LinkedList<Token>) out).add(identifier);
            }



            else if (inout.matches(MULTIPLICATION))
            {
                ((LinkedList<Token>) out).add(inout.poll());
            }

//            /**
//             * The right hand side is a &method call
//             */
//            else if (inout.matches(Token.Type.AND, Token.Type.IDENTIFIER, Token.Type.PARENTHESIS_OPEN))
//            {
//                mod.add(Modifier.REFERENCE);
//                Token invocation = new Token(Token.Type.METHOD_CALL).setModifiers(mod);
//
//                inout.poll();
//                Token methodName = inout.poll();
//                Token parenthesis = closeParenthesis(inout.tokens);
//
//                normalize(parenthesis);
//
//                ((LinkedList<Token>) out).add(invocation.add(methodName).add(parenthesis));
//            }


            /**
             * Standard method declaration
             *
             * type name () {}?
             */
            else if (inout.matches(Token.Type.IDENTIFIER, Token.Type.IDENTIFIER, Token.Type.PARENTHESIS_OPEN))
            {
                Token funcshin = new Token(METHOD_DECLARATION);
                Token returntp = inout.poll();
                funcshin.dataFrom(returntp);

                Token funcname = inout.poll();
                Token parenths = closeParenthesis(inout.tokens);
                Token body     = new Token(UNDEFINED);

                while (inout.peek().equals(END))
                    inout.poll();

                if (inout.peek().equals(BRACES_OPEN))
                    body       = closeBrace(inout.tokens);
                else if (inout.peek().equals(BRACES))
                    body       = inout.poll();

                if (body.equals(UNDEFINED))
                    funcshin.setType(METHOD_EMPTY_DECLARATION);

                normalize(body);

                funcshin.add(returntp).add(funcname).add(parenths).add(body).setModifiers(mod);
                funcshin.modErrors();

                ((LinkedList<Token>) out).add(funcshin);
            }

            /**
             * Standard field declaration
             */
            else if (inout.matches(Token.Type.IDENTIFIER, Token.Type.IDENTIFIER))
            {
                Token fieldtpe = new Token(EMPTY_DECLARATION);
                Token typename = inout.poll();
                fieldtpe.dataFrom(typename);
                Token defdname = inout.poll();

                fieldtpe.add(typename).add(defdname).setType(Token.Type.EMPTY_DECLARATION).setModifiers(mod);
                typename.modErrors();

                ((LinkedList<Token>) out).add(fieldtpe);
            }


            /**
             * Standard field declaration (pointer)
             * type * name
             */
            else if (inout.matches(Token.Type.IDENTIFIER, Token.Type.MULTIPLICATION, Token.Type.IDENTIFIER))
            {
                Token fieldtpe = new Token(EMPTY_DECLARATION);
                Token typename = inout.poll();
                fieldtpe.dataFrom(typename);
                    Token pointer  = inout.poll();
                Token defdname = inout.poll();

                mod.add(Modifier.POINTER);

                fieldtpe.add(typename).add(defdname).setType(Token.Type.EMPTY_DECLARATION).setModifiers(mod);
                typename.modErrors();

                ((LinkedList<Token>) out).add(fieldtpe);
            }


//            /**
//             * Standard full declaration
//             */
//            else if (inout.matches(Token.Type.IDENTIFIER, Token.Type.IDENTIFIER, Token.Type.EQUALS))
//            {
//                Token declaration = new Token(FULL_DECLARATION);
//                Token typename = inout.poll();
//                Token defdname = inout.poll();
//                Token dfequals = inout.poll();
//                Token value    = new Token(Token.Type.VALUE);
//
//                value.children.addAll(normalize(inout, true));
//
//                declaration.add(typename).add(defdname).add(value).setType(Token.Type.FULL_DECLARATION).setModifiers(mod);
//                declaration.modErrors();
//
//                ((LinkedList<Token>) out).add(declaration);
//            }




            /**
             * All that is left is an identifier (right hand side argument)
             */
            else if (inout.matches(Token.Type.IDENTIFIER))
            {
                Token identifier = inout.poll();

                ((LinkedList<Token>) out).add(checkAccess(inout, identifier));
            }

            /**
             * Double
             */
            else if(inout.matches(Token.Type.NUMBER, Token.Type.PROCEDURAL_ACCESS, Token.Type.NUMBER) || inout.matches(Token.Type.NUMBER, Token.Type.PROCEDURAL_ACCESS))
            {
                Token n0 = inout.poll().setType(DECIMAL);
                inout.poll();

                if (inout.peek().equals(NUMBER))
                    n0.append("." + inout.poll().data);

                ((LinkedList<Token>) out).add(n0);
            }

            /**
             * Int
             */
            else if(inout.matches(Token.Type.NUMBER))
            {
                ((LinkedList<Token>) out).add(inout.poll());
            }




            /**
             * Standard class declaration
             */
            else if (inout.matches(Token.Type.KEYWORD, Token.Type.IDENTIFIER, Token.Type.BRACES_OPEN))
            {
                Token tClass = inout.poll();
                Token name   = inout.poll();
                Token body   = closeBrace(inout.tokens);
                normalize(body);

                Token parent = new Token(Token.Type.UNDEFINED);

                tClass.add(name).add(parent).add(body).setType(Token.Type.CLASS_DECLARATION).setModifiers(mod);
                tClass.modErrors();

                ((LinkedList<Token>) out).add(tClass);
            }

            /**
             * Class declaration with inheritance
             */
            else if (inout.matches(Token.Type.KEYWORD, Token.Type.IDENTIFIER, Token.Type.COLON, Token.Type.KEYWORD, Token.Type.IDENTIFIER , Token.Type.BRACES_OPEN))
            {
                Token tClass = inout.poll();
                Token name   = inout.poll();
                Token parent = inout.skipPoll();

                Token body   = closeBrace(inout.tokens);
                normalize(body);

                tClass.add(name).add(parent).add(body).setType(Token.Type.CLASS_DECLARATION).setModifiers(mod);
                tClass.modErrors();

                ((LinkedList<Token>) out).add(tClass);
            } else if (inout.peek().equals(Token.Type.BRACES_CLOSED) || inout.peek().equals(Token.Type.PARENTHESIS_CLOSED))
            {
                return out;
            } else if (inout.peek().getType().equals(Token.Type.END))
            {
                inout.poll();
                if (nextEndLine)
                    return out;
            } else if (inout.peek().equals(BRACES) || inout.peek().equals(BRACKETS) || inout.peek().equals(PARENTHESIS))
            {
                Token token = inout.poll();
                normalize(token);
                ((LinkedList<Token>) out).add(token);
            } else if(inout.peek().equals(PARENTHESIS_OPEN))
            {
                Token tok = closeParenthesis(inout.tokens);
                normalize(tok);
                ((LinkedList<Token>) out).add(tok);
            } else if(inout.peek().equals(BRACES_OPEN))
            {
                Token tok = closeBrace(inout.tokens);
                normalize(tok);
                ((LinkedList<Token>) out).add(tok);
            } else
            {
                Token token = inout.peek();
                if (token == null)
                    token = new Token(UNDEFINED);
                String tokens = "";
                for (int i = 0; i < 5; i ++)
                    if (inout.peek() != null)
                        tokens += " " + inout.poll().smartString();
                System.err.println("no match for tokens: " + tokens);
                System.err.println("infostr: " + token.infoString());
                System.exit(-1);
            }

//            switch (token.type)
//            {
//                case IDENTIFIER:
//                    Token tClass = inout.poll();
//
//                    Token name   = inout.poll();
//                    tClass.add(name);
//
//                    if (inout.peek().getType().equals(Token.Type.BRACES_OPEN))
//                        tClass.add(closeBrace(inout));
//
//                    ((LinkedList<Token>) out).add(tClass);
//                    break;
//            }
        }

        return out;
    }

    public Queue<Token> getTokens()
    {
        return mTokens;
    }
}
