package mochaxx.compiler;

import java.util.Collection;
import java.util.LinkedList;

import static mochaxx.compiler.Token.Type.*;

public class TokenStream
{
    public LinkedList<Token> tokens;

    public static Token.Type[] Optional(Token.Type... types)
    {
        return types;
    }

    public TokenStream(Collection<Token> tokens)
    {
        this.tokens = new LinkedList<>(tokens);
    }

    public boolean matches(Token.Type ...types)
    {
        return matches(null, types);
    }

    public boolean matches(Token.Type[] optional, Token.Type ...types)
    {
        if (tokens.size() < types.length)
            return false;

        for (int i = 0; i < types.length; i ++)
            if (types[i].equals(ANY_NOT_END) && !tokens.get(i).getType().equals(END))
                continue;
            else if (types[i].equals(ANY_NOT_SUBEND) && !tokens.get(i).getType().equals(SUBTRACTION) && !tokens.get(i).getType().equals(END))
                continue;
            else if (types[i].equals(MATH_OP) && InfixToPostFixEvalution.isOperator(tokens.get(i)))
                return false;
            else if (!types[i].equals(Token.Type.ANY) && !tokens.get(i).getType().equals(types[i]))
                return false;

        return true;
    }

    public Token poll()
    {
        return tokens.poll();
    }

    public Token peek()
    {
        return tokens.peek();
    }

    public Token skipPoll()
    {
        poll();
        return poll();
    }

    public boolean end()
    {
        return tokens.size() == 0;
    }
}