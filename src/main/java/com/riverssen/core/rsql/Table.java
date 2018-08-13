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

package com.riverssen.core.rsql;

import com.riverssen.core.system.Logger;
import com.riverssen.core.headers.SQLCommand;
import com.riverssen.core.utils.ByteUtil;

import java.util.HashMap;
import java.util.List;

public class Table {
    HashMap<String, HashMap<String, List<Row>>> table = new HashMap<>();
    String names[];

    public Table(String... values)
    {
        for(String string : values)
            table.put(string, new HashMap<>());

        names = values;
    }

    private void remove(Row[] row)
    {
        for(Row r : row)
            r.remove(this);
    }

    @Override
    public String toString() {
        String s = ("-----------------------\n");


        for(List<Row> l : table.get(names[0]).values())
            for(Row n : l)
                s += n.getValue() + "\n";


        s+="-----------------------";

        return s;
    }

    public Row[] find(String arg, String value)
    {
        List<Row> ret = table.get(value).get(arg);
        if(ret == null) return new Row[] {null};
        Row array[]   = new Row[ret.size()];
        array = ret.toArray(array);
        return array;
    }

    public Row[] query(String...raw)
    {
        SQLCommand command[] = new SQLCommand[raw.length];

        for(int i = 0; i < raw.length; i ++)
            command[i] = build(raw[i]);
        return (Row[]) query(command);
    }
    public Row[] query(String unparsed)
    {
        String commands[] = unparsed.split("\\s");

        return query(commands);
    }
    public Row[] query(SQLCommand...commands)
    {
        Commands cmd = new Commands(commands);

        Object ret = null;

        while(cmd.hasNext()) ret = cmd.get().execute(this, cmd);

        return (Row[]) ret;
    }

    private static SQLCommand build(String token)
    {
        if(token.startsWith("'") && token.endsWith("'"))
            return (table, cmd, args)->{
                if(args != null && args.length > 0)
                {
                    if(cmd.hasNext()) return cmd.get().execute(table, cmd, ByteUtil.concatenate(args, new String[] {token}));
                    else              return ByteUtil.concatenate(args, new String[] {token});
                }
                return token.substring(1, token.length() - 1);
            };
        else if(token.startsWith("("))
            return (table, cmd, args)->{
                String strings[] = new String[1];

                if(token.length() > 1) strings[0] = token.substring(1);

                strings = (String[]) cmd.get().execute(table, cmd, strings);

                return strings;
            };
        else if(token.endsWith(")"))
            return (table, cmd, args)-> {
                return ByteUtil.concatenate(args, new String[]{token.substring(0, token.length() - 1)});
            };
        else if(token.equals("insert"))
            return ((table, cmd, args) -> {
                String values[] = (String[]) cmd.get().execute(table, cmd);

                return table.insert(values);
            });
        else if(token.equals("where"))
            return ((table, cmd, args) -> {
                String where = (String)cmd.get().execute(table, cmd);

                SQLCommand definite = cmd.get();

                return definite.execute(table, cmd, where);
            });
        else if(token.equals("="))
            return ((table, cmd, args) -> {
                return table.find((String)cmd.get().execute(table, cmd), args[0]);
            });
        else if(token.equals("delete"))
            return ((table, cmd, args) -> {
                if(!cmd.hasNext())
                {
                    Logger.err("rSQL error 'DELETE' must be followed by a token.");
                    return null;
                }

                SQLCommand command = cmd.get();
                Row[] row = (Row[]) command.execute(table, cmd, args);
                table.remove(row);
                return row;
            });
        else if(token.equals("select"))
            return ((table, cmd, args) -> {
                if(!cmd.hasNext())
                {
                    Logger.err("rSQL error 'SELECT' must be followed by a token.");
                    return null;
                }

                SQLCommand command = cmd.get();
                Row[] row = (Row[]) command.execute(table, cmd, args);
                return row;
            });
        else
        return (table, cmd, args)->{
            if(args != null)
                return cmd.get().execute(table, cmd, ByteUtil.concatenate(args, new String[] {token}));
            return token;
        };
    }

    private Row[] insert(String... values) {
        Row row = new Row(this, values);
        return new Row[] {row};
    }

    /** tests **/
//    public static void main(String...args)
//    {
//        Table table = new Table("a", "b", "c", "d");
//
//        table.query("insert (hello good say yo)");
//        table.query("insert (merkle good say yo)");
//        table.query("insert (tree good say yo)");
//        table.query("insert (hello good btc yo)");
//        table.query("insert (boi good say yo)");
//
//        Logger.alert(table.toString());
//
//        Row[] r = table.query("select where 'c' = 'btc'");
//
//        if(r.length > 0)
//            for(Row v : r)
//                System.out.println(v.getValue());
//    }
}
