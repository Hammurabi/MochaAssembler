package mochaxx.compiler.structure;

import mochaxx.compiler.Token;

public class aPair implements Cloneable
{
    public String name;
    public Struct type;
    public Token  defaults;

    public aPair(String name, Struct type, Token defaults)
    {
        this.name = name;
        this.type = type;
        this.defaults = defaults;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        return new aPair(name, (Struct) type.clone(), defaults);
    }
}
