package com.limpygnome.parrot.library.db.log;

import java.util.LinkedList;
import java.util.List;

/**
 * Used to hold a log of actions performed on the database.
 *
 * THis is currently used for merging, and not intended to be general.
 */
public class MergeLog
{
    private boolean remoteOutOfDate;
    private List<LogItem> logItems;
    private LogItem[] cachedLogItems;

    /**
     * Creates a new instance.
     */
    public MergeLog()
    {
        this.logItems = new LinkedList<>();
        this.remoteOutOfDate = false;
    }

    /**
     * @param item item to be added
     */
    public synchronized void add(LogItem item)
    {
        logItems.add(item);
        cachedLogItems = null;
    }

    public synchronized LogItem[] getLogItems()
    {
        if (cachedLogItems == null)
        {
            cachedLogItems = logItems.toArray(new LogItem[logItems.size()]);
        }
        return cachedLogItems;
    }

    public String asText()
    {
        if (logItems.isEmpty())
        {
            return "no log items";
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            for (LogItem logItem : logItems)
            {
                sb.append(logItem.getLevel()).append(" - ").append(logItem.getText()).append(System.getProperty("line.separator"));
            }
            return sb.toString();
        }
    }

    public void setLogItems(List<LogItem> logItems)
    {
        this.logItems = logItems;
    }

    public boolean isRemoteOutOfDate()
    {
        return remoteOutOfDate;
    }

    public void setRemoteOutOfDate(boolean remoteOutOfDate)
    {
        this.remoteOutOfDate = remoteOutOfDate;
    }

}
