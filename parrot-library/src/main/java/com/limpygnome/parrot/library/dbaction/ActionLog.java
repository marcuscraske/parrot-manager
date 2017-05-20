package com.limpygnome.parrot.library.dbaction;

import com.limpygnome.parrot.library.db.DatabaseNode;

import java.util.LinkedList;
import java.util.List;

/**
 * Used to hold a log of actions performed on the database.
 *
 * THis is currently used for merging, and not intended to be general.
 */
public class ActionLog
{
    private List<String> messages;

    /**
     * Creates a new instance.
     */
    public ActionLog()
    {
        this.messages = new LinkedList<>();
    }

    /**
     * Adds a new message.
     *
     * @param message the message
     */
    public void add(String message)
    {
        add(null, message);
    }

    /**
     * Adds a new message.
     *
     * @param node the node at which an action/event had taken place
     * @param message the message
     */
    public void add(DatabaseNode node, String message)
    {
        if (node != null)
        {
            messages.add(node.getPath() + " : " + message);
        }
        else
        {
            messages.add(message);
        }
    }

    /**
     * @return an instance with all messages joined together, using the system's/native line separator
     */
    public String getMessages(String hostName)
    {
        String separator = System.lineSeparator();
        StringBuilder buffer = new StringBuilder();

        messages.stream().forEach(action -> buffer.append(hostName).append(" : ").append(action).append(separator));
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
