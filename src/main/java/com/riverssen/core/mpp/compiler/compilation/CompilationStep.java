package com.riverssen.core.mpp.compiler.compilation;

import com.riverssen.core.mpp.compiler.AbstractSyntaxTree;
import com.riverssen.core.mpp.compiler.Token;

public interface CompilationStep
{
    void execute(Token token, AbstractSyntaxTree.CompileType type, AbstractSyntaxTree tree, Object... objects);
}
