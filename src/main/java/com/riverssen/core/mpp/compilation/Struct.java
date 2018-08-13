/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Riverssen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.riverssen.core.mpp.compilation;

import com.riverssen.core.mpp.Executable;
import com.riverssen.core.mpp.compiler.Token;
import com.riverssen.core.mpp.instructions;

import java.util.LinkedHashSet;
import java.util.Set;

public class Struct
{
    public static long NULL = 0;

    private String      __typename__;
    private long        __typesize__;
    private Set<Field>  __fields__;
    private Set<Method> __methods__;

    public Struct(GlobalSpace space, Token token)
    {
        this.__fields__ = new LinkedHashSet<>();
        this.__methods__= new LinkedHashSet<>();
        this.__typesize__   = 0;
        this.__typename__ = token.getTokens().get(0).toString();
        Set<String> fields = new LinkedHashSet<>();

        for (Token t : token.getTokens().get(1).getTokens())
        {
            switch (t.getType())
            {
                case EMPTY_DECLARATION:
                    if (fields.contains(t.getTokens().get(1).toString()))
                    {
                        System.err.println("field __" + t.getTokens().get(1).toString() + "__ already exists in __" + __typename__ + "__.");
                        System.exit(0);
                    }
                    Field field = new Field(space, t);
                    field.setLocation(__typesize__);
                    __typesize__ += field.size(space);
                    __fields__.add(field);
                    fields.add(t.getTokens().get(1).toString());
                    break;
                case FULL_DECLARATION:
                    if (fields.contains(t.getTokens().get(1).toString()))
                    {
                        System.err.println("field __" + t.getTokens().get(1).toString() + "__ already exists in __" + __typename__ + "__.");
                        System.exit(0);
                    }
                    Field _field_ = new Field(space, t);
                    _field_.setLocation(__typesize__);
                    __typesize__ += _field_.size(space);
                    __fields__.add(_field_);
                    fields.add(t.getTokens().get(1).toString());
                    break;
                case METHOD_DECLARATION:
                    Method method = new Method(space, this, t);

                    if (__methods__.contains(method))
                    {
                        System.err.println("method __" + t.getTokens().get(0).toString() + "__ already exists in __" + __typename__ + "__.");
                        System.exit(0);
                    }

                    __methods__.add(method);
                    break;
                case CLASS_DECLARATION:
                    System.err.println("class declaration not allowed inside of a class __" + __typename__ + "__.");
                    default:
                        System.exit(0);
            }
        }

        space.getGlobalTypes().put(__typename__, this);
    }

    public Method func_call(String name, boolean is_static)
    {
        for (Method method : __methods__)
            if (method.getName().equals(name));

        return null;
    }

    public long size()
    {
        return __typesize__;
    }

    public String getName()
    {
        return __typename__;
    }

    public boolean containsField(String reference, String accessor)
    {
        boolean must_be_public = accessor.equals(__typename__);

        for (Field field : __fields__)
            if (field.getName().equals(reference) && (must_be_public ? field.isPublic() : true)) return true;
        return false;
    }

    public Field getField(String reference, String accessor)
    {
        boolean must_be_public = accessor.equals(__typename__);

        for (Field field : __fields__)
            if (field.getName().equals(reference) && (must_be_public ? field.isPublic() : true)) return field;
        return null;
    }

    public void accessField(String name, Executable executable, String accessor)
    {
        Field field = getField(name, accessor);

        if (field != null)
        {
            executable.add(instructions.stack_read);
            executable.add(executable.convertLong(field.getLocation()));
        } else {
            System.err.println(name + " unaccessible.");
            System.exit(0);
        }
    }
}
