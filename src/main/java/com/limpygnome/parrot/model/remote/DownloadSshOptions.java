package com.limpygnome.parrot.model.remote;

import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS4;
import com.jcraft.jsch.ProxySOCKS5;

/**
 * Options for downloading a remote SSH file.
 */
public class DownloadSshOptions
{
    private String randomToken;

    // Mandatory
    private String host;
    private int port;
    private String user;
    private String destinationPath;
    private String remotePath;

    // Optional - auth
    private String pass;
    private boolean strictHostKeyChecking;
    private String privateKeyPath;
    private String privateKeyPass;

    // Optional - proxy
    private String proxyHost;
    private int proxyPort;
    private String proxyType;


    public DownloadSshOptions(String randomToken, String host, int port, String user, String remotePath, String destinationPath)
    {
        this.randomToken = randomToken;
        this.host = host;
        this.port = port;
        this.user = user;
        this.remotePath = remotePath;
        this.destinationPath = destinationPath;
    }

    public String getRandomToken()
    {
        return randomToken;
    }

    public void setRandomToken(String randomToken)
    {
        this.randomToken = randomToken;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getDestinationPath()
    {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath)
    {
        this.destinationPath = destinationPath;
    }

    public String getRemotePath()
    {
        return remotePath;
    }

    public void setRemotePath(String remotePath)
    {
        this.remotePath = remotePath;
    }

    public String getPass()
    {
        return pass;
    }

    public void setPass(String pass)
    {
        this.pass = pass;
    }

    public boolean isStrictHostKeyChecking()
    {
        return strictHostKeyChecking;
    }

    public void setStrictHostKeyChecking(boolean strictHostKeyChecking)
    {
        this.strictHostKeyChecking = strictHostKeyChecking;
    }

    public boolean isPrivateKey()
    {
        return privateKeyPath != null && privateKeyPath.length() > 0;
    }

    public String getPrivateKeyPath()
    {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath)
    {
        this.privateKeyPath = privateKeyPath;
    }

    public String getPrivateKeyPass()
    {
        return privateKeyPass;
    }

    public void setPrivateKeyPass(String privateKeyPass)
    {
        this.privateKeyPass = privateKeyPass;
    }

    public String getProxyHost()
    {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost)
    {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort()
    {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort)
    {
        this.proxyPort = proxyPort;
    }

    public String getProxyType()
    {
        return proxyType;
    }

    public void setProxyType(String proxyType)
    {
        this.proxyType = proxyType;
    }

    public Proxy getProxy()
    {
        Proxy result = null;

        if (proxyType != null)
        {
            switch (proxyType)
            {
                case "SOCKS4":
                    result = new ProxySOCKS4(proxyHost, proxyPort);
                    break;
                case "SOCKS5":
                    result = new ProxySOCKS5(proxyHost, proxyPort);
                    break;
                case "HTTP":
                    result = new ProxyHTTP(proxyHost, proxyPort);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid proxy type - " + proxyType);
            }
        }

        return result;
    }

}
