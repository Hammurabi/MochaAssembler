package com.riverssen.core.exceptions;

public class AddressInvalidException extends Exception
{
    public AddressInvalidException(String address)
    {
        super("Address '" + address + "' is an invalid Rivercoin address.");
    }
}
