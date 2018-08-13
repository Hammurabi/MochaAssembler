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

package com.riverssen.core.system;

import com.riverssen.core.headers.ContextI;
import com.riverssen.core.security.Wallet;

public class CommandLine
{
    interface Command{
        Command parse(String text, ContextI context);
    }

    static final void logHelp()
    {
        Logger.prt(Logger.COLOUR_YELLOW, "----------------------------------");
        Logger.prt(Logger.COLOUR_YELLOW, "Wallet Command:");
        Logger.prt(Logger.COLOUR_YELLOW, "\twallet -g <name> <seed> <password but not required>");
        Logger.prt(Logger.COLOUR_YELLOW, "\twallet -b <name> <id but not required>");
        Logger.prt(Logger.COLOUR_YELLOW, "\twallet -s <recipient> <amount>");
        Logger.prt(Logger.COLOUR_YELLOW, "----------------------------------");
    }

    static class WalletCommand implements Command
    {
        @Override
        public Command parse(String text, ContextI context) {
            System.out.println(text);
            if(text == null)
            {
                Logger.prt(Logger.COLOUR_YELLOW, "incorrect arguments.");
                logHelp();

                return this;
            }
            switch (text)
            {
                case "-balance":
                    return (t, context1)->{return this;};
                case "-send":
                case "-s":
                    return this;
                case "-generate":
                case "-g":
                    return (name, context2)->{
                        if(name == null)
                        {
                            Logger.prt(Logger.COLOUR_YELLOW, "incorrect arguments.");
                            logHelp();

                            return this;
                        }
                        return (seed, context3)->{
                            if(seed == null)
                            {
                                Logger.prt(Logger.COLOUR_YELLOW, "incorrect arguments.");
                                logHelp();

                                return this;
                            }
                            return (password, context4)->{
                                if(password == null)
                                    password = "0000000000000000";


                                Wallet wallet = new Wallet(name, seed);

                                wallet.export(password, context4);

                                Logger.alert("wallet generation successful!");
                                return this;
                            };
                        };
                    };
            }

            return this;
        }
    }

    public static Command newCommand()
    {
        return (text, context)->{
            switch (text)
            {
                case "wallet":
                    return new WalletCommand();
                default:
                    return newCommand();
            }
        };
    }
}