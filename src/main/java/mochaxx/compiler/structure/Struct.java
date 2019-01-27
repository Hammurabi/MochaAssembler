package mochaxx.compiler.structure;

import mochaxx.compiler.Token;
import mochaxx.compiler.structure.builtin_types.builtin_types;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static mochaxx.compiler.Token.Type.NAME;

public class Struct implements Cloneable
{
    protected String        name;
    protected Struct        parent;
    protected Set<Field>    fields;
    protected Set<Method>   methods;
    protected Set<Method>   constructors;
    protected Token         token;
    protected long          size;

    protected Token         template;
    protected Struct        generics[];

    protected boolean       isGeneric;

    public Struct(Token token)
    {
        this.name   = token.get(Token.Type.NAME).data;
        this.token  = token;
        this.fields = new LinkedHashSet<>();
        this.methods= new LinkedHashSet<>();
        this.generics= new Struct[0];

        this.isGeneric  = token.get(Token.Type.TEMPLATE) != null;
    }

    public Struct(String name)
    {
        this.name = name;
        this.size = -1;
    }

    public void setTemplate(Token token)
    {
        this.template = token;
        if (template == null)
            this.generics = new Struct[0];
        else
            this.generics = new Struct[token.children.size()];
    }

    public Struct getType()
    {
        return this;
    }

    public void setParent(Struct parent)
    {
        this.parent = parent;
    }

    public boolean addField(String name)
    {
        if (contains(name)) return false;

        return fields.add(new Field(name));
    }

    public boolean addMethod(String name)
    {
        if (contains(name)) return false;

        return methods.add(new Method(name));
    }

    public Field getField(String name)
    {
        for (Field field : fields)
            if (field.getName().equals(name))
                return field;

        return null;
    }

    public Method getMethod(String name)
    {
        for (Method field : methods)
            if (field.getName().equals(name))
                return field;

        return null;
    }

    public boolean contains(String name)
    {
        for (Field field : fields)
            if (field.getName().equals(name))
                return true;
        for (Method method : methods)
            if (method.getName().equals(name))
                return true;

        return false;
    }

    public long getSize()
    {
        if (size == -1)
        {
            this.size = 0L;

            for (Field field : fields)
            {
                field.setIndex(size);
                size += field.getSize();
            }

            for (Method method : methods)
                if (method.isVirtual())
                {
                    method.setIndex(size);
                    size += method.getSize();
                }
        }
        return size;
    }

    public long getSize(builtin_types.generic ...generics)
    {
        if (size == -1)
        {
            this.size = 0L;

            for (Field field : fields)
            {
                field.setIndex(size);
                size += field.getSize();
            }

            for (Method method : methods)
                if (method.isVirtual())
                {
                    method.setIndex(size);
                    size += method.getSize();
                }
        }
        return size;
    }

    public String getName()
    {
        return name;
    }

    public void dump(Map<String, Field> localFieldMap)
    {
        for (Field field : fields)
            localFieldMap.put(field.getName(), field);
        for (Method method : methods)
            localFieldMap.put(method.getName(), method);
    }

    public Struct getSign(boolean signed)
    {
        return this;
    }

    public boolean match()
    {
        return false;
    }

    public boolean isTemplateType(String type)
    {
        if (template == null)
            return false;

        for (Token token : template)
            if (token.getChild(1).data.equals(type))
                return true;

        return false;
    }

    public Struct setGenerics(Struct ...generics) throws CloneNotSupportedException
    {
        if (generics.length != template.children.size())
        {
            System.err.println("compile-err: cannot instantiate class type '" + name + "' with '" + generics.length + "' generic types.");
            System.exit(0);
        }

        Struct struct = (Struct) clone();
        struct.generics = generics;

        return struct;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        Struct clone = new Struct(name);
        clone.parent = this.parent;
        for (Field field : fields)
            clone.fields.add((Field) field.clone());
        for (Method method : methods)
            clone.methods.add((Method) method.clone());

        return super.clone();
    }

    public Struct getType(String name, Map<String, Struct> globals)
    {
        if (globals.containsKey(name))
            return globals.get(name);
        else
        {
            try
            {
                for (int i = 0; i < template.children.size(); i ++)
                    if (template.getChild(i).get(NAME).data.equals(name))
                        return generics[i];
            } catch (NullPointerException e)
            {
            }

            System.err.println("compile-err: type '" + name + "' does not exist.");
            System.exit(0);

            return null;
        }
    }

    public Token getTemplate()
    {
        return template;
    }
}