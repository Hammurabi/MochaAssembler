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

package com.riverssen.core.mpp.compiler;

import com.riverssen.core.mpp.exceptions.CompileException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class Field extends Container implements Serializable
{
    private String          fieldType;
    private Set<Modifier>   fieldModifiers;
    private int             offset;

    public Field(String name, String type)
    {
        this(name, type, null);
    }

    public Field(String name, String type, Collection<Modifier> modifiers)
    {
        this.name = name;
        this.fieldType = type;
        this.fieldModifiers = new LinkedHashSet<>();

        if (modifiers != null) this.fieldModifiers.addAll(modifiers);
    }

    public Field(Token tok)
    {
        this.name           = tok.getTokens().get(1).toString();
        this.fieldType      = tok.getTokens().get(0).toString();

        this.fieldModifiers = new LinkedHashSet<>();
        this.fieldModifiers.addAll(tok.getModifiers());
    }

    public Field addModifier(Modifier modifier)
    {
        this.fieldModifiers.add(modifier);
        return this;
    }

    @Override
    public Container call(Container self, Container... args)
    {
        return super.call(self, args);
    }

    @Override
    public void write(DataOutputStream stream)
    {
    }

    @Override
    public void read(DataInputStream stream)
    {
    }

    @Override
    public int hashCode()
    {
        return getName().hashCode();
    }

    protected void setOffset(int offset)
    {
        this.offset = offset;
    }

    public int getOffset()
    {
        return this.offset;
    }

    public String getType()
    {
        return this.fieldType;
    }

    @Override
    public String toString()
    {
        return super.toString();
    }

    public Collection<Modifier> getModifiers()
    {
        return this.fieldModifiers;
    }

    public String getFieldType()
    {
        return fieldType;
    }
}