package mochaxx.compiler;

import java.util.*;

import static mochaxx.compiler.Token.Type.*;

public class Token implements Iterable<Token>
{
    public Token modErrors()
    {
        for (Modifier modifier : modifiers)
        {
            if (type.equals(Type.CLASS_DECLARATION) && !modifier.equals(Modifier.FINAL))
            {
                System.err.println("classes can not have modifier: " + modifier);
                System.err.println("\tclass: " + children.iterator().next().data);
                System.err.println("\tinfostr: " + infoString());
                System.exit(0);
            }
//            else if (type.equals(Type.METHOD_CALL) && modifier.equals(Modifier.POINTER))
//            {
//                System.err.println("classes can not have modifier: " + modifier);
//                System.err.println("\tclass: " + children.iterator().next().data);
//                System.err.println("\tinfostr: " + infoString());
//                System.exit(0);
//            }
        }

        return this;
    }

    public String infoString()
    {
        return "line: " + line + " offset: " + offset;
    }

    public Token dataFrom(Token returntp)
    {
        line = returntp.line;
        offset = returntp.offset;
        whitespace = returntp.whitespace;

        return this;
    }

    public boolean isModifier(Modifier mod)
    {
        for (Modifier modifier : modifiers)
            if (modifier.equals(mod))
                return true;

        return false;
    }

    @Override
    public Iterator<Token> iterator()
    {
        return children.iterator();
    }

    public Token setData(String data)
    {
        this.data = data;

        return this;
    }

    public Collection<Token> getChildren()
    {
        return children == null ? new ArrayList<>() : children;
    }

    public int sizeofStruct(Set<String> generics, Collection<Token> tokens)
    {
        return 0;
    }

    public Token push_back(Token whatever)
    {
        List<Token> tokens = new ArrayList<>();
        tokens.add(whatever);
        tokens.addAll(children);

        this.children = tokens;

        return this;
    }

    public Token clean()
    {
        List<Token> children = new ArrayList<>();

        for (Token token : this.children)
            if (!token.equals(UNDEFINED))
                children.add(token);

        this.children = children;

        return this;
    }

    public enum Type{
        NAMESPACE,
        KEYWORD,
        CLASS,
        NEGATE,
        UNION,
        GENERIC,
        GENERIC_SPEC,
        CONSTRUCTOR,
        DEREFERENCE,
        ADDRESS,
        VARTYPE,
        RVARTYPE,
        NAME,
        PARENT_CLASS,
        IDENTIFIER,
        REFERENCE,
        SYMBOL,
        SUBSCRIPT,
        SUBSCRIPT_ASSIGNMENT,
        STRING,
        TRYCATCH,
        TRY,
        CATCH,
        STATEMENT,
        NUMBER,
        PARENTHESIS_OPEN,
        PARENTHESIS_CLOSED,
        BRACES_OPEN,
        BRACES_CLOSED,
        BRACKETS_OPEN,
        BRACKETS_CLOSED,
        MATH_OP,
        EQUALS,
        COMMA,
        END,
        ROOT,
        STATIC_ACCESS,
        PROCEDURAL_ACCESS,
        ASSIGN_REFERENCE,
        POINTER_ACCESS,
        INITIALIZATION,
        EMPTY_DECLARATION,
        FULL_DECLARATION,
        METHOD_CALL,
        CONSTRUCTOR_CALL,
        METHOD_DECLARATION,
        METHOD_EMPTY_DECLARATION,
        CLASS_DECLARATION,
        TEMPLATE_CLASS_DECLARATION,
        TEMPLATE_METHOD_DECLARATION,
        EMPTY_CLASS_DECLARATION,
        PARENTHESIS,
        BRACES,
        INPUT,
        ADDITION,
        SUBTRACTION,
        MULTIPLICATION,
        SUBDIVISION,
        TERNARY,
        BOOL_NOT,
        XOR,
        NOT,
        MOD,
        BRACKETS,
        VALUE,
        NEW,
        EXTEND,
        IF,
        ELSEIF,
        ELSE,
        FOR,
        GOTO,
        MARK,
        IN,
        AUTO,
        FOREACH,
        EACH,
        BOOL_OP,
        LESS_THAN,
        MORE_THAN,
        LESSTHAN_EQUAL,
        MORETHAN_EQUAL,
        ASSERT,
        LOGICAL_NOT,
        ASSIGNMENT,
        AND,
        OR,
        LAND,
        LOR,

        LOGICAL_NOTEQUALS,

        PLUSEQUALS,
        MINUSEQUALS,
        MULTIPLYEQUALS,
        DIVIDEEQUALS,

        LEFT_SHIFTEQUALS,
        RIGHT_SHIFTEQUALS,

        AND_EQUALS,
        XOR_EQUALS,

        OR_EQUALS,

        RIGHT_SHIFT,
        LEFT_SHIFT,
        UNARY_MINUS,
        INCREMENT,
        DECREMENT,
        UNARY_PLUSPREFIX,
        UNARY_MINUSPREFIX,
        UNARY_PLUSPOSTFIX,
        UNARY_MINUSPOSTFIX,
        PREUNARY,
        PLUSPLUS,
        MINUSMINUS,
        LIST,
        ARRAY,
        WHILE,
        DECIMAL,
        RETURN,
        TYPEDEF,
        TEMPLATE,
        TYPENAME,
        OPERATOR,
        ALLIGATORMOUTH,
        COLON,
        UNDEFINED,
        CAST,

        ANY,
        ANY_NOT_END,
        ANY_NOT_SUBEND
        ;
    }

    public Type type;
    public String data;
    public int  line = -1, offset = -1, whitespace = -1;
    public List<Token> children;
    public Token parent;
    public Set<Modifier> modifiers;

    public Token(Type type)
    {
        this(type, -1, -1, -1);
    }

    public Token(String data, int line, int offset, int whitespace)
    {
        this.type = Type.UNDEFINED;
        this.data = data;
        this.line = line;
        this.offset = offset;
        this.whitespace = whitespace;
        this.children = new ArrayList<>();
        this.modifiers = new LinkedHashSet<>();
    }

    public Token(Type type, int line, int offset, int whitespace)
    {
        this.data = "";
        this.type = type;
        this.line = line;
        this.offset = offset;
        this.whitespace = whitespace;
        this.children = new ArrayList<>();
        this.modifiers = new LinkedHashSet<>();
    }

    public Token append(String data)
    {
        this.data += data;

        return this;
    }

    public Token append(char data)
    {
        this.data += data;

        return this;
    }

    public Token setParent(Token parent)
    {
        this.parent = parent;

        return this;
    }

    private boolean isKeyword()
    {
        final String keywords[] = {"if", "else", "struct", "try", "catch", "goto", "in", "for", "namespace", "package", "typedef"/**, "operator"**/, "op", "function", "template", "fun", "new", "class", "static", "ref", "reference", "ptr", "native"/**, "pointer"**/, "public", "private", "protected", "const", "final", "extend", "header", "if", "for", "while", "foreach", "then", "namespace", "return"};
        for(String string : keywords) if(toString().equals(string)) return true;
        return false;
    }

    private void getAppropriateKeyword()
    {
        if (data.equals("class") || data.equals("struct"))
            type = Type.CLASS;
        else if (toString().equals("template")) type = Type.TEMPLATE;
        else if (toString().equals("typedef")) type = Type.TYPEDEF;
        else if (toString().equals("union")) type = Type.UNION;
//        else if (toString().equals("operator")) type = Type.OPERATOR;
        else if (toString().equals("namespace") || toString().equals("package")) type = Type.NAMESPACE;
        else if (toString().equals("for")) type = Type.FOR;
        else if (toString().equals("auto")) type = AUTO;
        else if (toString().equals("in")) type = IN;
        else if (toString().equals("goto")) type = GOTO;
        else if (toString().equals("try")) type = TRY;
        else if (toString().equals("catch")) type = CATCH;
        else if (toString().equals("else")) type = ELSE;
        else if (toString().equals("if")) type = IF;
        else if (toString().equals("new")) type = NEW;
        else if (toString().equals("each")) type = EACH;
        else if (toString().equals("foreach")) type = FOREACH;
        else if (toString().equals("return")) type = RETURN;
        else type = Type.KEYWORD;
    }

    public Type getType()
    {
        if (type.equals(Type.UNDEFINED))
        {
            final char separators[] = {'.', '=', '+', '-', '\'', '"', ',', '<', '>', '?', ';', ':', '!', '\\', '/', '[', ']', '{', '}', '(', ')', '*', '&', '^', '%', '$', '#', '@', '~'};

            boolean separator = false;

            if (toString().length() == 1)
            {
                if (toString().charAt(0) == ')')
                {
                    type = Type.PARENTHESIS_CLOSED;
                    return type;
                } else if (toString().charAt(0) == '~')
                {
                    type = Type.NOT;
                    return type;
                } else if (toString().charAt(0) == '^')
                {
                    type = Type.XOR;
                    return type;
                } else if (toString().charAt(0) == '?')
                {
                    type = Type.TERNARY;
                    return type;
                } else if (toString().charAt(0) == '!')
                {
                    type = Type.LOGICAL_NOT;
                    return type;
                } else if (toString().charAt(0) == '(')
                {
                    type = Type.PARENTHESIS_OPEN;
                    return type;
                } else if (toString().charAt(0) == '}')
                {
                    type = Type.BRACES_CLOSED;
                    return type;
                } else if (toString().charAt(0) == '{')
                {
                    type = Type.BRACES_OPEN;
                    return type;
                } else if (toString().charAt(0) == ']')
                {
                    type = Type.BRACKETS_CLOSED;
                    return type;
                } else if (toString().charAt(0) == '[')
                {
                    type = Type.BRACKETS_OPEN;
                    return type;
                } else if (toString().charAt(0) == ';')
                {
                    type = Type.END;
                    return type;
                } else if (toString().charAt(0) == '=')
                {
                    type = Type.EQUALS;
                    return type;
                } else if (toString().charAt(0) == ',')
                {
                    type = Type.COMMA;

                    return type;
                } else if (toString().charAt(0) == '!')
                {
                    type = Type.NEGATE;

                    return type;
                } else if (toString().charAt(0) == '+' || toString().charAt(0) == '-' || toString().charAt(0) == '*'
                        || toString().charAt(0) == '/' || toString().charAt(0) == '%')
                {
                    switch (toString())
                    {
                        case "+":
                            type = Type.ADDITION;
                            return type;
                        case "-":
                            type = Type.SUBTRACTION;
                            return type;
                        case "*":
                            type = Type.MULTIPLICATION;
                            return type;
                        case "/":
                            type = Type.SUBDIVISION;
                            return type;
                        case "%":
                            type = Type.MOD;
                            return type;
                    }
                    type = Type.MATH_OP;
                    return type;
                } else if (toString().charAt(0) == '&')
                {
                    type = Type.AND;
                    return type;
                } else if (toString().charAt(0) == '|')
                {
                    type = Type.OR;
                    return type;
                } else if (toString().charAt(0) == '<')
                {
                    type = Type.LESS_THAN;
                    return type;
                } else if (toString().charAt(0) == '>')
                {
                    type = Type.MORE_THAN;
                    return type;
                }

                for (char s : separators) if (toString().charAt(0) == s) separator = true;
            }

            if (separator)
            {
                if (toString().charAt(0) == '.')
                {
                    type = Type.PROCEDURAL_ACCESS;
                    return type;
                } else if (toString().charAt(0) == ':')
                {
                    type = Type.COLON;
                    return type;
                }
                type = Type.SYMBOL;
            } else
            {
                if (toString().startsWith("\"") || toString().startsWith("'")) type = Type.STRING;
                else if (toString().equals("extends"))
                    type = Type.EXTEND;
                else if (toString().matches("([_]*[A-z]+\\d*)+"))
                {
                    if (isKeyword())
                    {
                        getAppropriateKeyword();
                    } else
                        type = Type.IDENTIFIER;
                } else if (toString().matches("(\\d[_]*\\d*)+")) type = Type.NUMBER;
            }
        }
        return this.type;
    }

    public String smartString()
    {
        return "t{T:" + getType() + " d:" + data + "}";
    }

    public Token add(Token ...tokens)
    {
        for (Token token : tokens)
            add(token);

        return this;
    }

    public Token add(Token token)
    {
        children.add(token.setParent(this));
        if (line == -1)
            dataFrom(token);

        return this;
    }

    public Token add(Collection<Token> tokens)
    {
        for (Token token : tokens)
            add(token);

        return this;
    }

    private String whitespace(int length)
    {
        length*=4;
        String string = "";

        for(int i = 0; i < length; i ++)
            string += ' ';

        return string;
    }

    public String humanReadable()
    {
        return humanReadable(0);
    }

    public String humanReadable(int i)
    {
        String s = (whitespace(i) + type + " " + data + " (" + line + " " + offset + " " + whitespace + ") " + modifiers + "\n");// + " " + getInstructionsAsString()) + "\n";

        for(Token token : children)
            s += token.humanReadable(i + 1) + "\n";
        return s;
    }

    public boolean isModifier()
    {
        for (Modifier modifier : Modifier.values())
            if (modifier.toString().toLowerCase().equals(data))
                return true;

        return false;
    }

    public Modifier asModifier()
    {
        for (Modifier modifier : Modifier.values())
            if (modifier.toString().toLowerCase().equals(data))
                return modifier;

        return null;
    }

    public Token setModifiers(Set<Modifier> mod)
    {
        for (Modifier modifier : mod)
//            if (type.equals(Type.NAME) && modifier.equals(Modifier.POINTER))
//                this.modifiers.add(Modifier.DEREFERENCE);
//            else
                this.modifiers.add(modifier);
        mod.clear();

        return this;
    }

    public Token setType(Type type)
    {
        this.type = type;

        return this;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Type)
            return getType().equals(obj);

        return super.equals(obj);
    }

    public Token getChild(int i)
    {
        return children.get(i);
    }

    public Token get(Type type)
    {
        for (Token token : this)
            if (token.getType().equals(type))
                return token;

        return null;
    }

    @Override
    public int hashCode()
    {
        return data.hashCode();
    }

    @Override
    public String toString()
    {
        return data;
    }
}