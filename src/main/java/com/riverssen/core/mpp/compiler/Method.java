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
import com.sun.org.apache.xpath.internal.operations.Mod;

import java.util.*;

public class Method
{
    private Struct          __parenttype__;
    private String          __methodname__;
    private Set<Modifier>   __modifiers__;
    private ArrayList<Byte> __opcodes__;
    private boolean         __undeclared__;
    private String          __returntype__;

    public Method(String name)
    {
        __parenttype__  = null;
        __modifiers__   = new LinkedHashSet<>();
        __opcodes__     = new ArrayList();
        __methodname__  = name;
        __returntype__  = null;
    }

    /**
     * @param space     The Global Space
     * @param parent    The Parent Class/Structure Of This Method
     * @param token     The Method Token
     */
    public Method(GlobalSpace space, Struct parent, Token token)
    {
        __parenttype__  = parent;
        __modifiers__   = new LinkedHashSet<>();
        __opcodes__     = new ArrayList();
        __methodname__  = token.getTokens().get(0).toString();
        __undeclared__  = true;

        int stack = -1;
        Map<String, Integer> referencemap = new HashMap<>();

        if (parent != null)
            referencemap.put("this", ++ stack);

        Executable executable = new Executable();

        String accessor = "null";
        if (__parenttype__ != null) accessor = __parenttype__.getName();

        Set<Field> args = new LinkedHashSet<>();

        for (Token argument : token.getChild(Token.Type.PARENTHESIS).getTokens())
            args.add(new Field(space, argument));

        MethodArgument argument = new MethodArgument((parent == null || isStatic()) ? Struct.VOID : parent, args);

        if (token.getType().equals(Token.Type.METHOD_DECLARATION))
        {
            __opcodes__.addAll(token.getChild(Token.Type.BRACES).getInstruction(argument, space));
            __undeclared__ = false;
        }
    }

    public String getReturnType()
    {
        return __returntype__;
    }

    public String getName()
    {
        return __methodname__;
    }

    public List<Byte> getOpCodes()
    {
        return __opcodes__;
    }

    public Object call(Token token)
    {
        return null;
    }

    public boolean isDeclared()
    {
        return !__undeclared__;
    }

    public boolean isStatic()
    {
        return __parenttype__ == null || containsModifier(Modifier.STATIC);
    }

    public boolean containsModifier(Modifier modifier)
    {
        for (Modifier m : __modifiers__)
            if (m.equals(modifier)) return true;

        return false;
    }
}
