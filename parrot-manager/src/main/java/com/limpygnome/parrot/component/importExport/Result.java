package com.limpygnome.parrot.component.importExport;

public class Result
{
    private String text;
    private String error;

    public Result() { }

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

    public String getError()
    {
        return error;
    }

    public void setError(String error)
    {
        this.error = error;
    }

}
