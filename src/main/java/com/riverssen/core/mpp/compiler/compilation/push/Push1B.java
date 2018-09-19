package com.riverssen.core.mpp.compiler.compilation.push;

import com.riverssen.core.mpp.compiler.AbstractSyntaxTree;
import com.riverssen.core.mpp.compiler.Opcode;
import com.riverssen.core.mpp.compiler.Ops;
import com.riverssen.core.mpp.compiler.Token;
import com.riverssen.core.mpp.compiler.compilation.CompilationStep;

public class Push1B implements CompilationStep
{
    @Override
    public void execute(Token token, AbstractSyntaxTree.CompileType type, AbstractSyntaxTree tree, Object... objects)
    {
        boolean SIGNED = !(boolean) objects[0];
        switch (type)
        {
            case METHOD_ARGUMENTS:
                if (!SIGNED)
                    tree.getOpcode().add(new Opcode(Ops.bfld).add(Opcode.convertByte(Math.abs(Long.parseLong(token.toString())))));
                else
                    tree.getOpcode().add(new Opcode(Ops.bfld).add(Opcode.convertByte(Long.parseLong(token.toString()))));
                break;
            case UNARY_P:
                break;
            case UNARY_M:
                break;
            case FOR:
            case STRAIGHT_TO_REGISTER:
                if (SIGNED)
                    tree.getOpcode().add(new Opcode(Ops.bconstrld).add(Opcode.convertByte((Long.parseLong(token.toString())))));
                else
                    tree.getOpcode().add(new Opcode(Ops.bconstrld).add(Opcode.convertByte(Math.abs(Long.parseLong(token.toString())))));
                break;
            case NONE:
                default:
                    switch (token.toString())
                    {
                        case "0":
                            tree.getOpcode().add(new Opcode(Ops.bconst_0));
                            break;
                        case "1":
                            tree.getOpcode().add(new Opcode(Ops.bconst_1));
                            break;
                        case "2":
                            tree.getOpcode().add(new Opcode(Ops.bconst_2));
                            break;
                        case "3":
                            tree.getOpcode().add(new Opcode(Ops.bconst_3));
                            break;
                        default:
                            if (!SIGNED)
                                tree.getOpcode().add(new Opcode(Ops.bconst).add(Opcode.convertByte(Math.abs(Long.parseLong(token.toString())))));
                            else
                                tree.getOpcode().add(new Opcode(Ops.bconst).add(Opcode.convertByte(Long.parseLong(token.toString()))));
                            break;
                    }
                    break;
        }
    }
}