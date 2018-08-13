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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Row
{
    String value[];

    public Row(Table template, String... value)
    {
        this.value = value;

        for(int i = 0; i < template.names.length; i ++)
        {
            HashMap<String, List<Row>> map = template.table.get(template.names[i]);
            if (map == null) template.table.put(template.names[i], new HashMap<>());

            if (map.get(value[i]) == null) map.put(value[i], new ArrayList<>());

            map.get(value[i]).add(this);
        }
    }

    public String[] getValues()
    {
        return value;
    }

    public String getValue()
    {
        String value = "";
        for(String string : this.value) value += string + " ";

        return value;
    }

    public void remove(Table table) {
        for(int i = 0; i < table.names.length; i ++)
        {
            HashMap<String, List<Row>> map = table.table.get(table.names[i]);
            if (map == null) return;

            if (map.get(value[i]) == null) return;

            map.get(value[i]).remove(this);
        }
    }
}
