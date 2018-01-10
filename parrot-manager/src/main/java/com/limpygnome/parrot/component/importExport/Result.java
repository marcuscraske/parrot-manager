package com.limpygnome.parrot.component.importExport;

public class Result
{
    // Text from exporting the database (if supported)
    private String text;
    // Array of merge messages from importing data
    private String[] messages;

    private String error;

    public Result() { }

    public Result(String[] messages)
    {
        this.messages = messages;
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

    public String[] getMessages()
    {
        return messages;
    }

    public void setMessages(String[] messages)
    {
        this.messages = messages;
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
