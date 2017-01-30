package com.limpygnome.parrot.service.rest;

/**
 * A service for downloading and uploading remote files using SSH.
 *
 * TODO: unit test
 */
public class RemoteSshFileService
{

    /**
     * Begins downloading a file from an SSH host.
     *
     * @param host
     * @param port
     * @param user
     * @param pass
     * @param keyPath
     * @param destinationPath
     * @return error message, otherwise null if successful
     */
    public String download(String host, int port, String user, String pass, String keyPath, String destinationPath)
    {
    }

    public String upload(String host, int port, String user, String pass, String keyPath, String destinationPath)
    {
    }

}
