package com.limpygnome.parrot.lib.urlStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * A {@link URLConnection} class to address a bug or change in behaviour spotted in 1.8.0_151 (Oracle / Linux), whereby
 * {@link sun.net.www.protocol.file.FileURLConnection} (internal) does not have a response code of 200 for xhr / ajax
 * requests.
 *
 * Thus this extends {@link HttpURLConnection}, which is public, and creates a {@link URLConnection} in the usual way
 * from {@link URL#openConnection()}.
 *
 * The actual bug or change in behaviour would cause the front-end to not load, which looks to have been from system.js
 * checking the response code returned by an xhr / ajax request, which would be "0" (as opposed to 200). Rather than
 * patch system.js with a bodgy hack, we've wrapped file url connections within http url connections. Still not ideal,
 * but this avoids e.g. setting up a local web server.
 */
class CustomUrlConnection extends HttpURLConnection
{
    private URLConnection connection;

    CustomUrlConnection(URL url) throws IOException
    {
        super(url);
        connection = url.openConnection();
    }

    @Override
    public void connect() throws IOException
    {
        connection.connect();
    }

    @Override
    public void disconnect()
    {
        // do nothing...
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return connection.getInputStream();
    }

    @Override
    public boolean usingProxy()
    {
        return false;
    }

    @Override
    public int getResponseCode() throws IOException
    {
        return 200;
    }

    @Override
    public int getContentLength()
    {
        return connection.getContentLength();
    }
}
