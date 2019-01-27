package mochaxx.compiler.structure.builtin_types;

import mochaxx.compiler.structure.Struct;

public class Pointer extends Struct
{
    private Struct struct;

    public Pointer(Struct of)
    {
        super("pointer_" + of.getName());
        this.struct = of;
    }

    @Override
    public long getSize()
    {
        return 8;
    }
}