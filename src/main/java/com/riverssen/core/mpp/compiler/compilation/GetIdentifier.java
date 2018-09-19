package com.riverssen.core.mpp.compiler.compilation;

import com.riverssen.core.mpp.compiler.AbstractSyntaxTree;
import com.riverssen.core.mpp.compiler.Opcode;
import com.riverssen.core.mpp.compiler.Ops;
import com.riverssen.core.mpp.compiler.Token;

public class GetIdentifier implements CompilationStep
{
    @Override
    public void execute(Token token, AbstractSyntaxTree.CompileType type, AbstractSyntaxTree tree, Object... objects)
    {
        String name = token.toString();

        switch (type)
        {
            case STRAIGHT_TO_REGISTER:
            case FOR:
                break;
            case UNARY_M:
                break;
            case UNARY_P:
                break;
            case METHOD_ARGUMENTS:
                break;

            case NONE:
                default:
                    AbstractSyntaxTree.var v = tree.getLocalVariable(name);
                    if (v.isField)
                    {
                        switch (v.type)
                        {
                            default:
                                tree.getOpcode().add(new Opcode(Ops.amov));
                        }
                    } else {
                    }
                    break;
        }
    }
}
