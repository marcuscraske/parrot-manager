package com.limpygnome.parrot.model.remote;

import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS4;
import com.jcraft.jsch.ProxySOCKS5;
import com.limpygnome.parrot.model.db.Database;
import com.limpygnome.parrot.model.db.DatabaseNode;
import org.apache.logging.log4j.util.Strings;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Options for downloading a remote SSH file.
 *
 * TODO: unit test
 */
public class SshOptions
{
    private String randomToken;

    // Mandatory
    private String name;
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

    public SshOptions(String randomToken, String name, String host, int port, String user, String remotePath, String destinationPath)
    {
        this.randomToken = randomToken;
        this.name = name;
        this.host = host;
        this.port = port;
        this.user = user;
        this.remotePath = remotePath;
        this.destinationPath = destinationPath;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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

    /**
     * Deserializes a JSON string into a new instance of this class.
     *
     * @param node a standard child node
     * @return an instance
     * @throws Exception when cannot be deserialized or crypto problem
     */
    public static SshOptions read(DatabaseNode node) throws Exception
    {
        // Fetch value as string
        String value = node.getDecryptedValueString();

        // Deserialize into object
        ObjectMapper mapper = new ObjectMapper();
        SshOptions options =  mapper.readValue(value, SshOptions.class);

        return options;
    }

    /**
     * Persists the current configuration to a database in the standard remote-sync format.
     *
     * TODO: unit test
     * @param database the current database
     * @throws Exception when cannot persist
     */
    public void persist(Database database) throws Exception
    {
        // TODO: need way to ban / reserve names, as multiple nodes can share same name...
        // Should be saved to /remote-sync/<name> - overwrite by default
        DatabaseNode root = database.getRoot();
        DatabaseNode remoteSyncNode = root.getByName("remote-sync");

        // Add node for remote-sync if it does not exist
        if (remoteSyncNode == null)
        {
            remoteSyncNode = new DatabaseNode(database, "remote-sync");
            root.add(remoteSyncNode);
        }

        // Fetch any nodes with same name, remove them
        DatabaseNode similarNode = remoteSyncNode.getByName(name);

        if (similarNode != null)
        {
            similarNode.remove();
        }

        // Serialize as JSON string
        ObjectMapper mapper = new ObjectMapper();
        String rawJson = mapper.writeValueAsString(this);

        // Parse as JSON for sanity
        // TODO: we could just use pretty printer and save to node as string, may be a lot less efficient doing this...
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(rawJson);

        // Store as node
        DatabaseNode newNode = new DatabaseNode(database, name);
        newNode.setValueJson(json);
        remoteSyncNode.add(newNode);
    }

    @Override
    public String toString()
    {
        return "DownloadSshOptions{" +
                "randomToken='" + randomToken + '\'' +
                ", name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", user='" + user + '\'' +
                ", destinationPath='" + destinationPath + '\'' +
                ", remotePath='" + remotePath + '\'' +
                ", pass='" + pass + '\'' +
                ", strictHostKeyChecking=" + strictHostKeyChecking +
                ", privateKeyPath='" + privateKeyPath + '\'' +
                ", privateKeyPass='" + privateKeyPass + '\'' +
                ", proxyHost='" + proxyHost + '\'' +
                ", proxyPort=" + proxyPort +
                ", proxyType='" + proxyType + '\'' +
                '}';
    }

}
