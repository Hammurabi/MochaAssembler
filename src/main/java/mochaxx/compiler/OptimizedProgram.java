package mochaxx.compiler;

import java.util.Queue;

public class OptimizedProgram
{
    private Queue<Token> mTokens;

    public OptimizedProgram(ParsedProgram program)
    {
        this.mTokens = program.getTokens();
    }

    public Queue<Token> getTokens()
    {
        return mTokens;
    }
}
