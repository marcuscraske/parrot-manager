package com.limpygnome.parrot.library.log;

import com.limpygnome.parrot.library.db.DatabaseNode;

/**
 * Represents a merge change, or detail.
 *
 * TODO text should be replaced by enum with actions
 */
public class LogItem
{
    private LogLevel level;
    private boolean local;
    private String text;

    public LogItem(LogLevel level, boolean local, String text)
    {
        this.level = level;
        this.text = text;
        this.local = local;
    }

    public LogItem(LogLevel level, boolean local, DatabaseNode node, String text)
    {
        this(level, local, node.getPath() + " - " + text);
    }

    /**
     * @return the severity / level of message; used for filtering details
     */
    public LogLevel getLevel()
    {
        return level;
    }

    /**
     * @return true = local database node, false = remote database node
     */
    public boolean isLocal()
    {
        return local;
    }

    /**
     * @return log message / text; not currently a localisation string
     */
    public String getText()
    {
        return text;
    }

    @Override
    public int hashCode()
    {
        int result = level != null ? level.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

}
