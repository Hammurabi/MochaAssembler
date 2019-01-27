package mochaxx.compiler;

import java.util.*;

import static mochaxx.compiler.Token.Type.*;

public class InfixToPostFixEvalution
{

    public static boolean isOperator(Token c)
    {
        if (c == null)
            return false;

        return precedence.containsKey(c.getType());
    }

    private static final Map<Token.Type, Integer>
            precedence = new HashMap<>();

    static
    {
        precedence.put(PLUSEQUALS, 1);
        precedence.put(MINUSEQUALS, 1);

        precedence.put(LEFT_SHIFTEQUALS, 1);
        precedence.put(RIGHT_SHIFTEQUALS, 1);

        precedence.put(AND_EQUALS, 1);
        precedence.put(XOR_EQUALS, 1);

        precedence.put(OR_EQUALS, 1);


        precedence.put(LOR, 2);
        precedence.put(LAND, 3);
        precedence.put(OR, 4);
        precedence.put(XOR, 5);
        precedence.put(AND, 6);

        precedence.put(LESS_THAN, 8);
        precedence.put(MORE_THAN, 8);

        precedence.put(LESSTHAN_EQUAL, 8);
        precedence.put(MORETHAN_EQUAL, 8);

        precedence.put(LEFT_SHIFT, 10);
        precedence.put(RIGHT_SHIFT, 10);

        precedence.put(ADDITION, 11);
        precedence.put(SUBTRACTION, 11);

        precedence.put(MULTIPLICATION, 12);
        precedence.put(SUBDIVISION, 12);
        precedence.put(MOD, 12);

        precedence.put(ADDITION, 11);
        precedence.put(SUBTRACTION, 11);

        precedence.put(MULTIPLICATION, 12);
        precedence.put(SUBTRACTION, 12);

        precedence.put(INCREMENT, 14);
        precedence.put(DECREMENT, 14);


        precedence.put(UNARY_MINUSPREFIX, 14);
        precedence.put(UNARY_PLUSPREFIX, 14);


        precedence.put(UNARY_MINUSPOSTFIX, 15);
        precedence.put(UNARY_PLUSPOSTFIX, 15);

//        precedence.put(EQUALS, 16);
//        precedence.put(ASSIGN_REFERENCE, 16);
//
//        precedence.put(PROCEDURAL_ACCESS, 0);
//        precedence.put(STATIC_ACCESS, 0);
//        precedence.put(POINTER_ACCESS, 0);
    }

    private static int getPrecedence(Token ch)
    {
        Integer p = precedence.get(ch);
        if (p != null)
            return 17 - (int) p;
        return -1;
    }

    private static boolean isOperand(Token ch)
    {
        return !isOperator(ch);
    }

    public static List<Token> convertToPostfix(Token[] infix)
    {
        Stack<Token> stack = new Stack<>();
        List<Token> postfix = new ArrayList<>();
        Token c;

        for (int i = 0; i < infix.length; i++)
        {
            c = infix[i];

            if (isOperand(c))
                postfix.add(c);
            else if (isOperator(c))
            {
                if (!stack.isEmpty() && getPrecedence(c) <= getPrecedence(stack.peek()))
                    postfix.add(stack.pop());
                stack.push(c);
            }
        }

        while (!stack.isEmpty())
            postfix.add(stack.pop());

        return postfix;
    }
}