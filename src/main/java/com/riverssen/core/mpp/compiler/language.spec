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

function declaration:
    full    declaration: [function] [name] [parenthesis] [:] [type] [braces]
    empty   declaration: [function] [name] [parenthesis] [:] [type]

if statements and loops:
    [if]    [parenthesis] [braces] or [statement]
    [for]   [for] [parenthesis] [braces] or [statement]
    [while] [for] [parenthesis] [braces] or [statement]

classes, structures and headers:
    [class] or [header] or [struct] [name] <extends .,.,...> [braces]
    [class] or [header] or [struct] [name] [braces]
    [class] or [header] or [struct] [name]

fields:
    [Typename] [Variablename]
    [Typename] [Variablename] [equals] [value]