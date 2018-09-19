package com.riverssen.core.mpp.compiler.compilation;

import com.riverssen.core.mpp.compiler.AbstractSyntaxTree;
import com.riverssen.core.mpp.compiler.Token;

public class EmptyDeclaration implements CompilationStep
{
    public void execute(Token token, AbstractSyntaxTree.CompileType type, AbstractSyntaxTree tree, Object... objects)
    {
    }
}