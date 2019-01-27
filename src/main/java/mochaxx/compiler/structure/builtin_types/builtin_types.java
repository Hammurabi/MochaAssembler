package mochaxx.compiler.structure.builtin_types;

import mochaxx.compiler.structure.Struct;

public class builtin_types
{
    public static class int_8 extends Struct
    {
        public int_8(boolean signed)
        {
            super((signed ? "" : "u") + "int_8");
            this.signed = signed;
        }

        @Override
        public long getSize()
        {
            return 1;
        }

        boolean signed;

        @Override
        public Struct getSign(boolean signed)
        {
            return new int_8(signed);
        }
    }
    public static class bool extends Struct
    {
        public bool()
        {
            super("bool");
        }

        @Override
        public long getSize()
        {
            return 1;
        }
    }

    public static class Void extends Struct
    {
        public Void()
        {
            super("void");
        }

        @Override
        public long getSize()
        {
            return 0;
        }
    }

    public static class int_16 extends Struct
    {
        public int_16(boolean signed)
        {
            super((signed ? "" : "u") + "int_16");
            this.signed = signed;
        }

        @Override
        public long getSize()
        {
            return 2;
        }


        boolean signed;

        @Override
        public Struct getSign(boolean signed)
        {
            return new int_16(signed);
        }
    }

    public static class int_32 extends Struct
    {
        public int_32(boolean signed)
        {
            super((signed ? "" : "u") + "int_32");
            this.signed = signed;
        }

        @Override
        public long getSize()
        {
            return 4;
        }


        boolean signed;

        @Override
        public Struct getSign(boolean signed)
        {
            return new int_32(signed);
        }
    }

    public static class int_64 extends Struct
    {
        public int_64(boolean signed)
        {
            super((signed ? "" : "u") + "int_64");
            this.signed = signed;
        }

        @Override
        public long getSize()
        {
            return 8;
        }


        boolean signed;

        @Override
        public Struct getSign(boolean signed)
        {
            return new int_64(signed);
        }
    }

    public static class float_32 extends Struct
    {
        public float_32()
        {
            super("float_32");
        }

        @Override
        public long getSize()
        {
            return 4;
        }
    }

    public static class float_64 extends Struct
    {
        public float_64()
        {
            super("float_64");
        }

        @Override
        public long getSize()
        {
            return 8;
        }
    }

    public static class generic extends Struct
    {
        public generic(String name)
        {
            super(name);
        }

        @Override
        public long getSize()
        {
            return 0;
        }
    }

    public static class generic_spec extends Struct
    {
        private Struct type;

        public generic_spec(String name, Struct type, long size)
        {
            super(name);
            this.size = size;
            this.type = type;
        }

        @Override
        public Struct getSign(boolean signed)
        {
            return type.getSign(signed);
        }

        @Override
        public long getSize()
        {
            return size;
        }

        @Override
        public Struct getType()
        {
            return type;
        }
    }

    public static class array extends Struct{
        Struct type;
        long   length;

        public array(Struct type, long length)
        {
            super("array_" + type.getName() + "_" + length);
            this.type = type;
            this.length = length;
        }

        @Override
        public Struct getSign(boolean signed)
        {
            return new array(type.getSign(signed), length);
        }

        @Override
        public long getSize()
        {
            return type.getSize() * length;
        }

        @Override
        public String toString()
        {
            return getName();
        }
    }
}