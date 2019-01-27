package mochaxx.compiler.structure;

import mochaxx.compiler.Modifier;
import mochaxx.compiler.Token;

import java.util.*;

public class Field implements Cloneable
{
    private String name;
    private Struct type;
    private List<Struct> gSpec;
    private long            index;
    private Set<Modifier> modifiers;

    public Field(String name)
    {
        this.name = name;
        this.gSpec = new ArrayList<>();
        this.modifiers = new LinkedHashSet<>();
    }

    public void setModifiers(Collection<Modifier> modifiers)
    {
        this.modifiers.addAll(modifiers);
    }

    public boolean isModifier(Modifier modifier)
    {
        for (Modifier mod : modifiers)
            if (mod.equals(modifier))
                return true;

        return false;
    }

    public void setType(Struct type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setIndex(long index)
    {
        this.index = index;
    }

    public long getIndex()
    {
        return index;
    }

    public long getSize()
    {
        return type.getSize();
    }

    public boolean isMethod()
    {
        return false;
    }

    public Method asFunction()
    {
        return (Method) this;
    }

    public void setGenerics(Token token, Map<String, Struct> globalMap, Struct struct)
    {
        if (token == null)
            return;

        for (Token g : token)
            if (globalMap.containsKey(g))
                gSpec.add(globalMap.get(g.data));
            else if (struct.isTemplateType(g.data))
            {
            }
            else
            {
                System.err.println("compile-err: type '" + g.data + "' does not exist.");
                System.exit(0);
            }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        Field clone = new Field(name);
        clone.type = (Struct) this.type.clone();
        for (Struct struct : gSpec)
            clone.gSpec.add((Struct) struct.clone());
        clone.index = index;
        modifiers.addAll(modifiers);

        return clone;
    }
}