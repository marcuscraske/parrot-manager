package com.limpygnome.parrot.converter.api;

public class MalformedInputException extends Throwable
{

    public MalformedInputException(String message)
    {
        super(message);
    }

    public MalformedInputException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
