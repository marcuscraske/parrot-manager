package com.limpygnome.parrot.library.dbaction;

import java.util.LinkedList;
import java.util.List;

/**
 * Used to hold a log of actions performed on the database.
 *
 * THis is currently used for merging, and not intended to be general.
 */
public class ActionsLog
{
    private List<String> messages;

    public ActionsLog()
    {
        this.messages = new LinkedList<>();
    }

    public void add(String message)
    {
        messages.add(message);
    }

    public String getMessages()
    {
        String separator = System.lineSeparator();
        StringBuilder buffer = new StringBuilder();

        messages.stream().forEach(action -> buffer.append(action).append(separator));
        if (buffer.length() > 0)
        {
            int separatorLen = separator.length();
            int len = buffer.length();
            buffer.delete(len - separatorLen, len);
        }

        String result = buffer.toString();
        return result;
    }

}
