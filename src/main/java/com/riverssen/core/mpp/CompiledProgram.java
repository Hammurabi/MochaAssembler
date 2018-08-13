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

package com.riverssen.core.mpp;

import com.riverssen.core.mpp.compilation.Field;
import com.riverssen.core.mpp.compilation.GlobalSpace;
import com.riverssen.core.mpp.compilation.Method;
import com.riverssen.core.mpp.compilation.Struct;
import com.riverssen.core.mpp.compiler.ParsedProgram;
import com.riverssen.core.mpp.compiler.Token;
import com.riverssen.core.utils.Tuple;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class CompiledProgram
{
    private Executable executable;

    public CompiledProgram(ParsedProgram parsedProgram)
    {
        Token root = parsedProgram.getRoot();
        executable = new Executable();

        simulate_contract_b(root);
    }

    private void simulate_contract_b(Token root)
    {
        GlobalSpace space = new GlobalSpace();

        for (Token t : root.getTokens())
        {
            switch (t.getType())
            {
                case EMPTY_DECLARATION:
                    if (space.getGlobalFields().containsKey(t.getTokens().get(1).toString()))
                    {
                        System.err.println("field __" + t.getTokens().get(1).toString() + "__ already exists in __global__.");
                        System.exit(0);
                    }
                    Field field = new Field(space, t);
                    space.getGlobalFields().put(t.getTokens().get(1).toString(), field);
                    break;
                case FULL_DECLARATION:
                    if (space.getGlobalFields().containsKey(t.getTokens().get(1).toString()))
                    {
                        System.err.println("field __" + t.getTokens().get(1).toString() + "__ already exists in __global__.");
                        System.exit(0);
                    }
                    Field _field_ = new Field(space, t);
                    space.getGlobalFields().put(t.getTokens().get(1).toString(), _field_);
                    break;
                case METHOD_DECLARATION:
                    Method method = new Method(space, null, t);

                    if (space.getGlobalMethods().containsKey(method.getName()))
                    {
                        System.err.println("method __" + t.getTokens().get(0).toString() + "__ already exists in __global__.");
                        System.exit(0);
                    }

                    space.getGlobalMethods().put(method.getName(), method);
                    break;
                case CLASS_DECLARATION:
                    Struct struct = new Struct(space, t);

                    if (space.getGlobalMethods().containsKey(struct.getName()))
                    {
                        System.err.println("struct __" + struct.getName() + "__ already exists in __global__.");
                        System.exit(0);
                    }

                    space.getGlobalTypes().put(struct.getName(), struct);
                default:
                    System.exit(0);
            }
        }
    }

    private void simulate_contract_a(Token root)
    {
        Set<Token>              methods_ = new LinkedHashSet<>();
        Set<Token>              structs_ = new LinkedHashSet<>();
        Map<String, Integer>    sizeof_ = new HashMap();
        Map<String, Integer>    tablef_ = new HashMap<>();
        Map<String, Integer>    tablet_ = new HashMap<>();

        tablet_.put("char", 1);
        tablet_.put("uchar", 1);
        tablet_.put("short", 2);
        tablet_.put("ushort", 2);
        tablet_.put("int", 4);
        tablet_.put("uint", 4);
        tablet_.put("float", 4);
        tablet_.put("long", 8);
        tablet_.put("ulong", 8);
        tablet_.put("double", 8);
        tablet_.put("int128", 16);
        tablet_.put("uint128", 16);
        tablet_.put("float128", 16);
        tablet_.put("int256", 32);
        tablet_.put("uint256", 32);
        tablet_.put("float256", 32);
        tablet_.put("string", 8);
        tablet_.put("pointer", 8);
        tablet_.put("PublicAddress", 25);
        tablet_.put("ContractAddress", 35);
        tablet_.put("InvokeAddress", 39);

        int funcs_  = 0;
        int _stack_ = 0;

        Map<String, Tuple<Integer, Object>> map = new HashMap();

        for (Token token : root.getTokens())
        {
            if (token.getType().equals(Token.Type.METHOD_DECLARATION))
            {
                tablef_.put(token.getTokens().get(0).toString(), methods_.size());
                methods_.add(token);
            }
            else if (token.getType().equals(Token.Type.CLASS_DECLARATION))
            {
                tablet_.put(token.getTokens().get(0).toString(), structs_.size());
                structs_.add(token);

                for (Token method : token.getTokens().get(1).getTokens())
                {
                    if (method.getType().equals(Token.Type.METHOD_DECLARATION)) {
                        tablef_.put(method.getTokens().get(0).toString(), methods_.size());
                        methods_.add(method);
                    }
                }
            }
        }

        class _object_
        {
            String  _typename_;
            int     _typesize_;

            protected _object_ clone()
            {
                _object_ object_ = new _object_();
                object_._typename_ = _typename_;
                object_._typesize_ = _typesize_;

                return object_;
            }
        }

        class _struct_
        {
            Token   _tokenidn_;
            String  _typename_;
            int     _typesize_;

            Map<String, Integer> ___fields___ = new HashMap<>();

            int sizeof(String name)
            {
                if (tablet_.containsKey(name)) return tablet_.get(name);
                System.err.println("type '" + name + "' doesn't exist.");
                System.exit(0);
                return -1;
            }

            _struct_(Token token, int size)
            {
                int index  = 0;
                _tokenidn_ = token;
                _typename_ = token.getTokens().get(0).toString();
                _typesize_ = size;

                for (Token field : token.getTokens().get(1).getTokens())
                {
                    if (field.getType().equals(Token.Type.EMPTY_DECLARATION) || field.getType().equals(Token.Type.FULL_DECLARATION))
                    {
                        String type = field.getTokens().get(0).toString();
                        String name = field.getTokens().get(1).toString();

                        ___fields___.put(name, index);
                        index += sizeof(type);
                    }
                }
            }

            int __indexof__(String name)
            {
                if (___fields___.containsKey(name)) return ___fields___.get(name);
                System.err.println("field '" + name + "' doesn't exist in scope of '" + _typename_ + "'.");
                System.exit(0);
                return -1;
            }

            void _fetch_(String name)
            {
                executable.add(instructions.memory_load);
                executable.add(executable.convertInt(__indexof__(name)));
            }

            void _call_(String name, Token ...arguments)
            {
               List<Token> body = _tokenidn_.getTokens().get(1).getTokens();

               for (Token token : body)
                   if (token.getType().equals(Token.Type.METHOD_DECLARATION) && token.getTokens().get(0).toString().equals(name))
                   {
                       Token args[] = arguments;
                       if (arguments == null)
                           args = new Token[0];

                       List<Token> parenthesis = token.getTokens().get(2).getTokens();

                       if (parenthesis.size() != args.length)
                       {
                           System.err.println("function '" + name + "' with '" + args.length + "' arguments doesn't exist in the scope of '" + _typename_ + "'.");
                           System.exit(0);
                       }

                       for (Token arg : args)
                           __interpret__(arg);

                       executable.add(instructions.call_);
                       executable.add(executable.convertInt(tablef_.get(name)));
                   }

               System.err.println("function '" + name + "' doesn't exist in the scope of '" + _typename_ + "'.");
               System.exit(0);
            }

            void __interpret__(Token token)
            {
                switch (token.getType())
                {
                    case INITIALIZATION:
                        break;
                    case PROCEDURAL_ACCESS:
                        break;
                    case MATH_OP:
                        break;
                }
            }
        }

        for (Token method : methods_)
        {
            executable.add(instructions.start_func);

            List<Token> args = method.getTokens().get(2).getTokens();

            String accessPoint = "null";

            if (method.inClass())
            {
                Token empty_declaration = new Token(Token.Type.EMPTY_DECLARATION);
                empty_declaration.add(new Token(Token.Type.IDENTIFIER).setName(method.getContainingClass().getTokens().get(0).toString()));
                empty_declaration.add(new Token(Token.Type.IDENTIFIER).setName("this"));
                args.add(0, empty_declaration);
                accessPoint = method.getContainingClass().getTokens().get(0).toString();
            }

            Token       body = method.getTokens().get(3);

            for (Token statement : body.getTokens())
                statement.compile(args, executable, accessPoint);

            executable.add(instructions.end_func);
        }
    }

    /** for none contract type compilation **/
    private void simulate(Token root)
    {
        Set<Token>              methods_ = new LinkedHashSet<>();
        Set<Token>              structs_ = new LinkedHashSet<>();
        Map<String, Integer>    sizeof_ = new HashMap();
        Map<String, Integer>    tablef_ = new HashMap<>();

        int funcs_  = 0;
        int _stack_ = 0;

        Map<String, Tuple<Integer, Object>> map = new HashMap();

        class _struct_{
            Token   _this_;
            String  _typename_;
            int     _typesize_;
            int     _stackidx_;
            int     _memryidx_;

            _struct_(Token token)
            {
                this._this_ = token;
            }

            Token find_method(String name, _struct_ ...args)
            {
                for (Token token : _this_.getTokens().get(2).getTokens())
                    if (token.getType().equals(Token.Type.METHOD_DECLARATION))
                    {
                    }

                    String args_ = "";

                    for (_struct_ arg : args)
                        args_ += (arg._typename_ + " ");

                    System.err.println("compilation error: no function '" + name + "' with args '" + args_.substring(0, args_.length() - 1) + "'.");

                return null;
            }

            void _empty_constructor_()
            {
            }

            void _argtd_constructor_(_struct_ ...args)
            {
            }

            void _new_(_struct_ ...args)
            {
                /** push a reference to pointer **/
                executable.add(instructions.push_a);
                /** make the size of the pointer the same as _typesize_ **/
                executable.addAll(executable.convertInt(_typesize_));

                if (args == null || args.length == 0)
                    _empty_constructor_();
                else _argtd_constructor_(args);
            }

            void _simulate_method_(_struct_ ...methods)
            {
            }

            void _simulate_fetch_()
            {
                executable.add(instructions.memory_load);
                executable.add(executable.convertInt(_memryidx_));
            }

            void simulate()
            {
            }
        }

        _struct_ __struct__ = new _struct_(root);

        for (Token token : root.getTokens())
        {
            if (token.getType().equals(Token.Type.METHOD_DECLARATION))
            {
                tablef_.put(token.toString(), methods_.size());
                methods_.add(token);
            }
            else if (token.getType().equals(Token.Type.CLASS_DECLARATION))
                structs_.add(token);
        }
    }

    public boolean spit(File file)
            throws IOException
    {
        FileOutputStream io = new FileOutputStream(file);

        for (Byte byt : executable.op_codes)
            io.write(Byte.toUnsignedInt(byt.byteValue()));

        io.flush();
        io.close();

        return true;
    }
}
