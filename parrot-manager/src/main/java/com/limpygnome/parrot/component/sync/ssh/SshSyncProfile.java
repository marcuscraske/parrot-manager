package com.limpygnome.parrot.component.sync.ssh;

import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS4;
import com.jcraft.jsch.ProxySOCKS5;
import com.limpygnome.parrot.component.sync.SyncProfile;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;

/**
 * Options for downloading a remote SSH file.
 *
 * WARNING: this class is serialized using Jackson, hence be careful when adding new getters/setters, ensure
 * jsonignore annotation is set appropriately.
 */
public class SshSyncProfile extends SyncProfile implements Serializable, Cloneable
{
    // Mandatory
    private String name;
    private String host;
    private int port;
    private String user;
    private String remotePath;

    // Optional - auth
    private String userPass;
    private String privateKeyPath;
    private String privateKeyPass;

    // Optional - proxy
    private String proxyHost;
    private int proxyPort;
    private String proxyType;

    // Options
    private boolean promptUserPass;
    private boolean promptKeyPass;
    private boolean strictHostChecking;

    public SshSyncProfile() { }

    @Override
    public String getType()
    {
        return "ssh";
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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

    public String getRemotePath()
    {
        return remotePath;
    }

    public void setRemotePath(String remotePath)
    {
        this.remotePath = remotePath;
    }

    public String getUserPass()
    {
        return userPass;
    }

    public void setUserPass(String userPass)
    {
        this.userPass = userPass;
    }

    public boolean isStrictHostChecking()
    {
        return strictHostChecking;
    }

    public void setStrictHostChecking(boolean strictHostChecking)
    {
        this.strictHostChecking = strictHostChecking;
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

    public boolean isPromptUserPass()
    {
        return promptUserPass;
    }

    public void setPromptUserPass(boolean promptUserPass)
    {
        this.promptUserPass = promptUserPass;
    }

    public boolean isPromptKeyPass()
    {
        return promptKeyPass;
    }

    public void setPromptKeyPass(boolean promptKeyPass)
    {
        this.promptKeyPass = promptKeyPass;
    }

    @JsonIgnore
    public boolean isPrivateKey()
    {
        return privateKeyPath != null && privateKeyPath.length() > 0;
    }

    @JsonIgnore
    public Proxy getProxy()
    {
        Proxy result = null;

        if (proxyType != null && proxyType.length() > 0)
        {
            switch (proxyType)
            {
                case "None":
                    break;
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

    @Override
    public String toString()
    {
        return "SshSyncProfile{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", user='" + user + '\'' +
                ", remotePath='" + remotePath + '\'' +
                ", userPass='" + userPass + '\'' +
                ", privateKeyPath='" + privateKeyPath + '\'' +
                ", privateKeyPass='" + privateKeyPass + '\'' +
                ", proxyHost='" + proxyHost + '\'' +
                ", proxyPort=" + proxyPort +
                ", proxyType='" + proxyType + '\'' +
                ", promptUserPass=" + promptUserPass +
                ", promptKeyPass=" + promptKeyPass +
                ", strictHostChecking=" + strictHostChecking +
                '}';
    }

}
