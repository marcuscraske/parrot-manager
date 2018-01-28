package com.limpygnome.parrot.library.io.json;

/**
 * Generic incorrect password exception.
 */
public class IncorrectPasswordOrCorruptedException extends Exception
{

    public IncorrectPasswordOrCorruptedException()
    {
        super("incorrect password or database file is corrupted");
    }

    public IncorrectPasswordOrCorruptedException(Throwable e)
    {
        super("incorrect password or database file is corrupted", e);
    }

}
