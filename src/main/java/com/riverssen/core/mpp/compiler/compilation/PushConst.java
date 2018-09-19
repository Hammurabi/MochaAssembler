package com.riverssen.core.mpp.compiler.compilation;

import com.riverssen.core.mpp.compiler.AbstractSyntaxTree;
import com.riverssen.core.mpp.compiler.Token;
import com.riverssen.core.mpp.compiler.compilation.push.*;

public class PushConst implements CompilationStep
{
    @Override
    public void execute(Token token, AbstractSyntaxTree.CompileType type, AbstractSyntaxTree tree, Object... objects)
    {
        switch (tree.resetLastOnStack())
        {
            case "":
                pushDefault(token, type, tree);
                break;
            case "byte":
            case "char":
                new Push1B().execute(token, type, tree, false);
                break;
            case "ubyte":
            case "uchar":
                new Push1B().execute(token, type, tree, true);
                break;

            case "ushort":
                new Push2B().execute(token, type, tree, true);
                break;
            case "short":
                new Push2B().execute(token, type, tree, false);
                break;

            case "uint":
                new Push4B().execute(token, type, tree, true);
                break;
            case "int":
                new Push4B().execute(token, type, tree, false);
                break;

            case "ulong":
                new Push8B().execute(token, type, tree, true);
                break;
            case "long":
                new Push8B().execute(token, type, tree, false);
                break;
            case "uint128":
                new Push16B().execute(token, type, tree, true);
                break;
            case "int128":
                new Push16B().execute(token, type, tree, false);
                break;

            case "uint256":
                new Push32B().execute(token, type, tree, true);
                break;
            case "int256":
                new Push32B().execute(token, type, tree, false);

            case "float":
                new Push4Bf().execute(token, type, tree);
                break;

            case "double":
                new Push8Bf().execute(token, type, tree);
                break;

            case "float128":
                new Push16Bf().execute(token, type, tree);
                break;

            case "float256":
                new Push32Bf().execute(token, type, tree);
                break;
                default:
                    tree.errorCallBack.execute("cannot compile token '" + token.toString() + " " + token.getType() + "'.", token.getStackTrace(), 1);
                    break;
        }
    }

    //Push either a long or a double
    public void pushDefault(Token token, AbstractSyntaxTree.CompileType type, AbstractSyntaxTree tree)
    {
        if (Math.round(Double.parseDouble(token.toString())) != Double.parseDouble(token.toString()))
            new Push8Bf().execute(token, type, tree);
        else new Push8B().execute(token, type, tree);
    }
}