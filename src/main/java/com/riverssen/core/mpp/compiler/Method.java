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
    private Set<Field>      __arguments__;
    private List<Byte>      __opcodes__;
    private boolean         __undeclared__;
    private String          __returntype__;
    private long            __location__;
    private Opcode          __opcodes2__;
    private Token           __tokens__;

    public Method(String name, GlobalSpace space)
    {
        __parenttype__  = null;
        __modifiers__   = new LinkedHashSet<>();
        __opcodes__     = new ArrayList();
        __methodname__  = name;
        __returntype__  = "void";
        __location__    = space.addMethod(this);
        __arguments__   = new LinkedHashSet<>();
        __undeclared__  = false;
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
        __location__    = space.addMethod(this);
        __tokens__      = token;

        int stack = -1;
        Map<String, Integer> referencemap = new HashMap<>();

        if (parent != null)
            referencemap.put("this", ++ stack);

        Executable executable = new Executable();

        String accessor = "null";
        if (__parenttype__ != null) accessor = __parenttype__.getName();

        __arguments__ = new LinkedHashSet<>();

        for (Token argument : token.getChild(Token.Type.PARENTHESIS).getTokens())
            __arguments__.add(new Field(space, argument, null));

        MethodArgument argument = new MethodArgument((isStatic()) ? null : parent, __arguments__, space);

        __returntype__ = token.getTokens().get(1).toString();

        if (token.getType().equals(Token.Type.METHOD_DECLARATION) || token.getType().equals(Token.Type.OPERATOR))
        {
            __undeclared__ = false;
            AST ast = new AST(token.getChild(Token.Type.BRACES), this, space);
            __opcodes__  = ast.getExecutable().op_codes;
            __opcodes2__ = ast.getOpcode();
//            __opcodes__.addAll(token.getChild(Token.Type.BRACES).getInstruction(argument, space));
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

    public long getLocation()
    {
        return __location__;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    public Struct getReturnType(GlobalSpace space)
    {
        return space.getGlobalTypes().get(__returntype__);
    }

//    public boolean matches(GlobalSpace space, Struct ... arguments)
//    {
//        if ((arguments == null || arguments.length == 0) && __arguments__.size() == 0)
//            return true;
//        else if ((arguments == null || arguments.length == 0) && __arguments__.size() > 0)
//            return false;
//        else if ((arguments == null || arguments.length != __arguments__.size()))
//            return false;
//
//        int i = 0;
//
//        for (Field field : __arguments__)
//            if (!field.getTypeStruct(space).getName().equals(arguments[i ++]))
//                return false;
//
//        return true;
//    }

    public boolean matches(GlobalSpace space, List<Field> arguments)
    {
        if ((arguments == null || arguments.size() == 0) && __arguments__.size() == 0)
            return true;
        else if ((arguments == null || arguments.size() == 0) && __arguments__.size() > 0)
            return false;
        else if ((arguments == null || arguments.size() != __arguments__.size()))
            return false;

        int i = 0;

        for (Field field : __arguments__)
            if (!arguments.get(i ++).getTypeStruct(space).match(field.getTypeStruct(space)))
                return false;

        return true;
    }

    public Opcode getOpcodes()
    {
        return __opcodes2__;
    }

    public Struct getParent()
    {
        return __parenttype__;
    }

    public Set<Field> getArguments()

    {
        return __arguments__;
    }

    public void setOpcodes(Opcode opcodes)
    {
        this.__opcodes2__ = opcodes;
    }

    public Opcode inline(AST ast, GlobalSpace space)
    {
        try {
            return new AST(__tokens__.getChild(Token.Type.BRACES), this, space, ast).getOpcode();
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(__methodname__);
            System.exit(0);
            return null;
        }
    }
}
