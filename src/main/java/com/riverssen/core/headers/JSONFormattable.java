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

package com.riverssen.core.headers;

public interface JSONFormattable
{
    default String comma()
    {
        return ",";
    }

    default String jsonLine(String name, String data)
    {
        return "\""+name+"\":\""+data+"\"";
    }

    String toJSON();

    class JSON{
        static final String quote     = "\"";
        static final String colon     = ":";
        StringBuilder json;
        boolean       added;
        boolean       array;

        public JSON(String name)
        {
            this.json = new StringBuilder().append(quote).append(name).append(quote).append(colon).append("{");
        }

        public JSON(String name, boolean array)
        {
            this.array  = true;
            this.json   = new StringBuilder().append(quote).append(name).append(quote).append(colon).append("[");
        }

        public JSON()
        {
            this.json = new StringBuilder().append("{");
        }

        public JSON add(String name, String value)
        {
            this.json.append(added ? ", " : "").append(quote).append(name).append(quote).append(colon).append(quote).append(value).append(quote);
            this.added = true;

            return this;
        }

        public JSON addJSONString(String name, String value)
        {
            this.json.append(added ? ", " : "").append(quote).append(name).append(quote).append(colon).append(value);
            this.added = true;

            return this;
        }
        public JSON add(String name, JSON json)
        {
            this.json.append(added ? ", " : "").append(quote).append(name).append(quote).append(colon).append(quote).append(json.json.toString()).append(quote);
            this.added = true;

            return this;
        }
        public JSON add(String data)
        {
            this.json.append(added ? ", " : "").append(data);
            this.added = true;

            return this;
        }

        @Override
        public String toString() {
            return json.toString() + (array ? "]" : "}");
        }
    }
}
