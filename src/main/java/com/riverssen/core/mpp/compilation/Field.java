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
import com.riverssen.core.mpp.compiler.Modifier;
import com.riverssen.core.mpp.compiler.Token;
import com.riverssen.core.mpp.exceptions.CompileException;

import java.util.LinkedHashSet;
import java.util.Set;

public class Field
{
    private String          __typename__;
    private String          __realname__;
    private Set<Modifier>   __modifiers__;
    private long            __position__;

    Executable              __opcodes__;

    public Field(GlobalSpace space, Token token)
    {
        __modifiers__   = new LinkedHashSet<>();
        __opcodes__     = new Executable();
        if (token.getType().equals(Token.Type.EMPTY_DECLARATION))
        {
            __typename__ = token.getTokens().get(0).toString();
            __realname__ = token.getTokens().get(1).toString();
        } else if (token.getType().equals(Token.Type.FULL_DECLARATION))
        {
            __typename__ = token.getTokens().get(0).toString();
            __realname__ = token.getTokens().get(1).toString();
        } else {
            new CompileException("Compilation Error: Token defined as field.", token).printStackTrace();
            System.exit(0);
        }
    }

    public void __new__(Executable executable)
    {
        executable.add(__opcodes__.op_codes);
    }

    public void setLocation(long typesize__)
    {
        this.__position__ = typesize__;
    }

    public long size(GlobalSpace space)
    {
        return space.getGlobalTypes().get(__typename__).size();
    }

    public String getName()
    {
        return __realname__;
    }

    public boolean isPublic()
    {
        return __modifiers__.contains(Modifier.PUBLIC);
    }

    public long getLocation()
    {
        return __position__;
    }
}
