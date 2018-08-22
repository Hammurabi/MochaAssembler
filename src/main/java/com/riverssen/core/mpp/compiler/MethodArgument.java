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
import com.riverssen.core.utils.Tuple;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class MethodArgument
{
    public Struct _this_;
    public Set<Field> _arguments_;
    public Map<String, Long> stack;

    public MethodArgument(Struct _this_, Set<Field> _arguments_, GlobalSpace space)
    {
        this._this_ = _this_;
        this._arguments_ = new LinkedHashSet<>(_arguments_);
        this.stack = new HashMap<>();

        if (_this_ != null && !_this_.getName().equals("VOID"))
            stack.put("this", (long)stack.size());//new Tuple<>(stack.size(), 8));
        for (Field field : _arguments_)
            stack.put(field.getName(), (long)stack.size());//new Tuple(stack.size(), field.size(space)));
    }

    public boolean addArgument(Field field, GlobalSpace space)
    {
        boolean b = _arguments_.add(field);

        if (b)
            stack.put(field.getName(), (long)stack.size());//new Tuple(stack.size(), field.size(space)));

        return b;
    }

    public void loadVariable(String variable_name, Executable executable, GlobalSpace space)
    {
        if (stack.containsKey(variable_name))
        {
            executable.add(instructions.stack_load);
//            executable.add(type.reference);
            executable.add(executable.convertLong(stack.get(variable_name)));
        }
//        if (variable_name.equals("this"))
//        {
//            if (_this_ != null)
//            {
//                executable.add(instructions.stack_load);
//                executable.add(type.pointer_);
//                executable.add(executable.convertLong(0));
//            } else {
//                new CompileException("'this' is not a member of a static function method.", new Token(Token.Type.ROOT)).printStackTrace();
//                System.exit(0);
//            }
//        }
//        else if (_this_ != null && _this_.containsField(variable_name, _this_.getName()))
//        {
//            Field field = _this_.getField(variable_name, _this_.getName());
//            /** load _this_ **/
//            executable.add(instructions.stack_load);
//            executable.add(type.pointer_);
//            executable.add(executable.convertLong(0));
//            /** load object from memory **/
//            executable.add(instructions.memory_load);
//            /** size of object **/
//            executable.add(executable.convertLong(field.size(space)));
//
//            /** position of object in relation to _this_ pointer **/
//            executable.add(executable.convertLong(_this_.getFieldOffset(variable_name)));
//        } else {
//            int position = _this_ != null ? 1 : 0;
//            Field field_ = null;
//
//            for (Field field : _arguments_)
//            {
//                if (field.getName().equals(variable_name))
//                {
//                    executable.add(instructions.stack_load);
//                    executable.add(executable.convertLong(position));
//                    return;
//                }
//
//                position ++;
//            }
//
//            new CompileException(variable_name + " not found.", new Token(Token.Type.ROOT)).printStackTrace();
//            System.exit(0);
//        }
    }

    public boolean contains(String argument)
    {
        if (_this_ != null && _this_.containsField(argument, _this_.getName())) return true;

        for (Field field : _arguments_)
            if (field.getName().equals(argument)) return true;

        return false;
    }

    public int fetchType(String s)
    {
        //TODO: Check _this_

        for (Field field : _arguments_)
            if (field.getName().equals(s)) return field.getValidType();
        return 0;
    }

    public String fetchTypeName(String s)
    {
        if (_this_.containsField(s, _this_.getName()))
            return _this_.getField(s, _this_.getName()).getTypeName();

        for (Field field : _arguments_)
            if (field.getName().equals(s)) return field.getTypeName();
        return "null";
    }

    public Struct getVariable(String variable_name, GlobalSpace space)
    {
        if (variable_name.equals("this"))
        {
            if (_this_ != null)
            {
                return _this_;
            } else {
                new CompileException("'this' is not a member of a static function method.", new Token(Token.Type.ROOT)).printStackTrace();
                System.exit(0);
            }
        }
        else if (_this_ != null && _this_.containsField(variable_name, _this_.getName()))
        {
            Field field = _this_.getField(variable_name, _this_.getName());

            return field.getTypeStruct(space);
        } else {
            for (Field field : _arguments_)
                if (field.getName().equals(variable_name))
                    return field.getTypeStruct(space);

            new CompileException(variable_name + " not found.", new Token(Token.Type.ROOT)).printStackTrace();
            System.exit(0);
        }

        return null;
    }

    public void pullVariable(String variable_name, Executable executable, GlobalSpace space)
    {
        if (stack.containsKey(variable_name))
        {
            executable.add(instructions.stack_load);
//            Tuple<Long, Long> s = stack.get(variable_name);
//            executable.add(executable.convertLong());
        } else {
        }
    }

    public long getstack(String name)
    {
        return stack.containsKey(name) ? stack.get(name) : -1;
    }
}
