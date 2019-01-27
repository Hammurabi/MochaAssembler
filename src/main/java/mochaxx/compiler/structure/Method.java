package mochaxx.compiler.structure;

import mochaxx.compiler.Token;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Method extends Field implements Cloneable
{
    private long    index;
    private Token   parenthesis;
    private aPair   apairs[];
    private Token   parsed;
    private Token   template;

    public Method(String name)
    {
        super(name);
    }

    public void setArguments(aPair ...pairs)
    {
        this.apairs = pairs;
    }

    public void setToken(Token body)
    {
        this.parsed = body;
    }

    public void setTemplate(Token token)
    {
        this.template = token;
    }

    public void setParenthesis(Token token)
    {
        this.parenthesis = token;
    }

    private
    class scope{
        Field   stack[] = new Field[1000];
        long    esp = 0;

        public void pop()
        {
            esp --;

            if (esp < 0)
                esp = 0;
        }

        public boolean containsKey(String name)
        {
            return get(name) != null;
        }

        public boolean push(String name, Struct type)
        {
            if (containsKey(name))
                return false;

            long index = esp ++;

            Field field = new Field(name);
            field.setIndex(index);
            field.setType(type);

            stack[(int) index] = field;

            return true;
        }

        public void push()
        {
            esp ++;
        }

        public Field get(String name)
        {
            for (int i = 0; i < esp; i ++)
                if (stack[i].getName().equals(name))
                    return stack[i];

            return null;
        }

        /**
         * @return true if item exists.
         *
         * This function gets the requested item from the stack.
         * If the item is not the topmost stack item it is swapped
         * with the topmost item and returned.
         */
        public boolean move_to_top(String name, OpcodeStream ops)
        {
            Field field = get(name);

            if (field == null) return false;

            if (field.getIndex() != (esp - 1))
            {
                long highestIndex = esp - 1;

                long currentIndex = field.getIndex();

                ops.op_swap(currentIndex, "move '" + name + "' to the top of the stack");

                return true;
            }

            return true;
        }
    }

    protected void getOpcodes(Map<String, Struct> globalTypes, Struct container, Struct self, OpcodeStream stream)
    {
        /**
         * This map will contain all the fields of 'this'
         * if it exists.
         */
        Map<String, Field> localFieldMap    = new HashMap<>();
        scope              mscope           = new scope();

        long               ebpOffset        = 0L;

        for (aPair pair : apairs)
            ebpOffset += pair.type.getSize();

        if (self != null) self.dump(localFieldMap);

        compile(globalTypes, container, localFieldMap, mscope, stream, parsed);
    }

    private Stack<Token> compile(Map<String, Struct> globalTypes, Struct container, Map<String, Field> localFieldMap, scope mscope, OpcodeStream opcodeStream, Token root)
    {
        Stack<Token> tokenStack = new Stack<>();

        for (Token token : root)
        {
            switch (token.getType())
            {
                case EMPTY_DECLARATION:
                    if (!mscope.push(token.getChild(1).data, globalTypes.get(token.getChild(0).data)))
                    {
                        System.err.println("field already declared in scope.");
                        System.err.println("infostr: " + token.infoString());
                        System.exit(0);
                    }
                    break;
                case FULL_DECLARATION:
                    /**
                     * No need to push to stack because compiling the declaration.value will push to stack.
                     */
                    if (!mscope.containsKey(token.getChild(1).data))//mscope.push(token.getChild(1).data, globalTypes.get(token.getChild(0).data)))
                    {
                        System.err.println("field already declared in scope.");
                        System.err.println("infostr: " + token.infoString());
                        System.exit(0);
                    }

                    tokenStack.addAll(compile(globalTypes, container, localFieldMap, mscope, opcodeStream, token.getChild(2)));
                    break;
                case INITIALIZATION:
                    break;
            }
        }

        return tokenStack;
    }

    public boolean isVirtual()
    {
        return false;
    }

    @Override
    public long getSize()
    {
        return 8;
    }

    @Override
    public void setIndex(long index)
    {
        this.index = index;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        Method clone = new Method(getName());

        clone.index = index;
        clone.parenthesis = parenthesis;

        aPair pairs[] = new aPair[apairs.length];
        for (int i = 0; i < apairs.length; i ++)
            pairs[i] = (aPair) apairs[i].clone();

        clone.apairs = pairs;
        clone.parsed = parsed;
        clone.template = template;

        return clone;
    }
}