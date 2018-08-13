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

package com.riverssen.core.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LinkedList<T>
{
    private Set<T>      set;
    private Element<T>  root;

    public int size()
    {
        return set.size();
    }

    private class Element<T>
    {
        private T           t;
        private Element<T>  next;

        private Element(T t)
        {
            this.t = t;
        }

        private void add(Element<T> element)
        {
            if(next == null) next = element;
            else next.add(element);
        }
    }

    public LinkedList()
    {
        set = Collections.synchronizedSet(new HashSet<>());
    }

    public void add(T element)
    {
        if(set.contains(element)) return;

        if(root == null)
            root = new Element<>(element);
        else root.add(new Element<>(element));

        set.add(element);
    }

    public boolean contains(T element)
    {
        return set.contains(element);
    }

    public void removeEldestEntry()
    {
        set.remove(root.t);
        root = root.next;
    }
}