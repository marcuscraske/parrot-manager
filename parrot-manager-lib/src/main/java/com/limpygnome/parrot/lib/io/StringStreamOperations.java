package com.limpygnome.parrot.lib.io;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Component
public class StringStreamOperations
{

    public String readString(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1)
        {
            baos.write(buffer, 0, read);
        }

        byte[] raw = baos.toByteArray();
        String result = new String(raw, "UTF-8");
        return result;
    }

    public void writeString(OutputStream outputStream, String text) throws IOException
    {
        byte[] buffer = text.getBytes("UTF-8");
        outputStream.write(buffer);
        outputStream.flush();
    }

}
