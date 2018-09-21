package com.limpygnome.parrot.component.importExport;

import com.limpygnome.parrot.library.log.Log;

public class Result
{
    // Text from exporting the database (if supported)
    private String text;
    // Result of merging imported data
    private Log log;

    private String error;

    public Result() { }

    public Result(Log log)
    {
        this.log = log;
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

    public Log getLog()
    {
        return log;
    }

    public void setLog(Log log)
    {
        this.log = log;
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
