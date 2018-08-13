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

import java.util.*;

public class Method
{
    private Struct          __parenttype__;
    private String          __methodname__;
    private Set<Modifier>   __modifiers__;
    private ArrayList<Byte> __opcodes__;

    public Method(GlobalSpace space, Struct parent, Token token)
    {
        __parenttype__  = parent;
        __modifiers__   = new LinkedHashSet<>();
        __opcodes__     = new ArrayList();
        __methodname__  = token.getTokens().get(0).toString();

        int stack = -1;
        Map<String, Integer> referencemap = new HashMap<>();

        if (parent != null)
            referencemap.put("this", ++ stack);

        Executable executable = new Executable();

        String accessor = "null";
        if (__parenttype__ != null) accessor = __parenttype__.getName();

        for (Token t : token.getTokens().get(2).getTokens())
        {
            switch (t.getType())
            {
                case IF:
                    break;
                case INITIALIZATION:
                    String reference = t.getTokens().get(0).toString();

                    if (referencemap.containsKey(reference))
                    {
                    } else if (parent != null && parent.containsField(reference, accessor))
                    {
                        parent.accessField(reference, executable, accessor);
                    } else {
                        System.out.println("variable '" + reference + "' doesn't exist.");
                        System.exit(0);
                    }
                    break;
                case EMPTY_DECLARATION:
                    break;
                case FULL_DECLARATION:
                    break;
                case PROCEDURAL_ACCESS:
                    break;
            }
        }
    }

    public String getName()
    {
        return __methodname__;
    }
}
