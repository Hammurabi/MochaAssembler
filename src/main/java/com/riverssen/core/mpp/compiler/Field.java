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

import com.riverssen.core.mpp.Executable;
import com.riverssen.core.mpp.exceptions.CompileException;
import com.riverssen.core.mpp.type;

import java.util.LinkedHashSet;
import java.util.Set;

public class Field
{
    private String          __typename__;
    private String          __realname__;
    private Set<Modifier>   __modifiers__;
    private long            __position__;
    private boolean         __inherent__;
    private Token           __size__;
    private Token           __value__;

    Executable              __opcodes__;

    public Field(GlobalSpace space, Token token)
    {
        __modifiers__   = new LinkedHashSet<>();
        __opcodes__     = new Executable();
        __value__       = null;

        if (token.getTokens().get(0).getType().equals(Token.Type.ARRAY))
        {
            __inherent__ = true;
            __size__     = token.getTokens().get(0).getTokens().get(1).getTokens().get(0);
            __typename__ = token.getTokens().get(0).getTokens().get(0).toString();
        }
        if (token.getType().equals(Token.Type.EMPTY_DECLARATION))
        {
            if (!__inherent__)
                __typename__ = token.getTokens().get(0).toString();
            __realname__ = token.getTokens().get(1).toString();
        } else if (token.getType().equals(Token.Type.FULL_DECLARATION))
        {
            if (!__inherent__)
                __typename__ = token.getTokens().get(0).toString();
            __realname__ = token.getTokens().get(1).toString();
            __value__    = token.getTokens().get(2);
        } else {
            new CompileException("Compilation Error: Token defined as field.", token).printStackTrace();
            System.exit(0);
        }
    }

    public void __new__(Executable executable, GlobalSpace space)
    {
        executable.add(__opcodes__.op_codes);
    }

    public void __newonstack__(Executable executable, StackTrace trace)
    {
        executable.push_(this, trace);
    }

    public void setLocation(long typesize__)
    {
        this.__position__ = typesize__;
    }

    public long size(GlobalSpace space)
    {
        if (__inherent__)
            return __size__.getSizeAsLong(space);
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

    public int getValidType()
    {
        switch (__typename__)
        {
            case "char": return type.char_;
            case "uchar": return type.uchar_;
            case "short": return type.short_;
            case "ushort": return type.ushort_;
            case "int": return type.int_;
            case "uint": return type.uint_;
            case "float": return type.float32_;
            case "long": return type.long_;
            case "ulong": return type.ulong_;
            case "double": return type.float64_;
            case "int128": return type.int128_;
            case "uint128": return type.uint128_;
            case "float128": return type.float128_;
            case "int256": return type.int256_;
            case "uint256": return type.uint256_;
            case "float256": return type.float256_;
            case "string": return type.c_string;
            case "pointer": return type.pointer_;
        }

        return 255;
    }

    public String getTypeName()
    {
        return __typename__;
    }

    public void instantiate(Executable executable, StackTrace trace)
    {
        if (__value__ != null)
        {
            if (__value__.getTokens().get(0).getType().equals(Token.Type.NEW))
            {
                if (__value__.getTokens().get(0).toString().equals("METHOD_CALL"))
                {
                } else {
                }
            }
        } else {
        }
    }

    public boolean isStatic()
    {
        return containsModifier(Modifier.STATIC);
    }

    public boolean containsModifier(Modifier modifier)
    {
        for (Modifier m : __modifiers__)
            if (m.equals(modifier)) return true;

        return false;
    }
}
