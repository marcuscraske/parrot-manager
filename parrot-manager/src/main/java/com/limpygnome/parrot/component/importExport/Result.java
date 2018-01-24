package com.limpygnome.parrot.component.importExport;

import com.limpygnome.parrot.library.db.log.MergeLog;

public class Result
{
    // Text from exporting the database (if supported)
    private String text;
    // Result of merging imported data
    private MergeLog mergeLog;

    private String error;

    public Result() { }

    public Result(MergeLog mergeLog)
    {
        this.mergeLog = mergeLog;
    }

    public Result(String text, String error)
    {
        this.text = text;
        this.error = error;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public MergeLog getMergeLog()
    {
        return mergeLog;
    }

    public void setMergeLog(MergeLog mergeLog)
    {
        this.mergeLog = mergeLog;
    }

    public String getError()
    {
        return error;
    }

    public void setError(String error)
    {
        this.error = error;
    }

}
