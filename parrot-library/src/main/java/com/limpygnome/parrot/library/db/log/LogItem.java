package com.limpygnome.parrot.library.db.log;

import com.limpygnome.parrot.library.db.DatabaseNode;

/**
 * Represents a merge change, or detail.
 */
public class LogItem
{
    private LogLevel level;
    private String text;

    public LogItem(LogLevel level, String text)
    {
        this.level = level;
        this.text = text;
    }

    public LogItem(LogLevel level, DatabaseNode node, String text)
    {
        this(level, node.getPath() + " - " + text);
    }

    public LogLevel getLevel()
    {
        return level;
    }

    public void setLevel(LogLevel level)
    {
        this.level = level;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    @Override
    public int hashCode()
    {
        int result = level != null ? level.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

}
