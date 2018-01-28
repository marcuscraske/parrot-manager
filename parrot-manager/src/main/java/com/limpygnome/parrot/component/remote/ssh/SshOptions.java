package com.limpygnome.parrot.component.remote.ssh;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS4;
import com.jcraft.jsch.ProxySOCKS5;
import com.limpygnome.parrot.lib.database.EncryptedValueService;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseNode;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.Serializable;

/**
 * Options for downloading a remote SSH file.
 *
 * WARNING: this class is serialized using Jackson, hence be careful when adding new getters/setters, ensure
 * jsonignore annotation is set appropriately.
 */
public class SshOptions implements Serializable, Cloneable
{
    // Mandatory
    private String name;
    private String host;
    private int port;
    private String user;
    // -- Don't serialize the local/destination path, allow database to be dynamically moved around, as this is
    //    stored in the database archive during runtime
    @JsonIgnore
    private String destinationPath;
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
    private String machineFilter;
    private boolean promptUserPass;
    private boolean promptKeyPass;
    private boolean strictHostChecking;

    public SshOptions() { }

    public SshOptions(String name, String host, int port, String user, String remotePath, String destinationPath)
    {
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
    public String getDestinationPath()
    {
        return destinationPath;
    }

    @JsonIgnore
    public void setDestinationPath(String destinationPath)
    {
        this.destinationPath = destinationPath;
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

    public String getMachineFilter()
    {
        return machineFilter;
    }

    public void setMachineFilter(String machineFilter)
    {
        this.machineFilter = machineFilter;
    }

    /**
     * Deserializes a JSON string into a new instance of this class.
     *
     * @param encryptedValueService used to decrypt value stored in node
     * @param database database
     * @param node a standard child node
     * @return an instance
     * @throws Exception when cannot be deserialized or crypto problem
     */
    public static SshOptions read(EncryptedValueService encryptedValueService, Database database, DatabaseNode node) throws Exception
    {
        // Fetch value as string
        String value = encryptedValueService.asString(database, node.getValue());

        // Deserialize into object
        ObjectMapper mapper = new ObjectMapper();
        SshOptions options =  mapper.readValue(value, SshOptions.class);

        return options;
    }

    /**
     * Persists the current configuration to a database in the standard remote-sync format.
     *
     * @param database the current database
     * @throws Exception when cannot persist
     */
    public void persist(EncryptedValueService encryptedValueService, Database database) throws Exception
    {
        // Should be saved to /remote-sync/<name> - overwrite by default
        DatabaseNode root = database.getRoot();
        DatabaseNode remoteSyncNode = root.getByName("remote-sync");

        // Add node for remote-sync if it does not exist
        if (remoteSyncNode == null)
        {
            remoteSyncNode = new DatabaseNode(database, "remote-sync");
            root.add(remoteSyncNode);
        }

        // Create new instance
        SshOptions clone = (SshOptions) this.clone();

        // Serialize as JSON string
        ObjectMapper mapper = new ObjectMapper();
        String rawJson = mapper.writeValueAsString(clone);

        // Parse as JSON for sanity
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(rawJson).getAsJsonObject();

        // Create encrypted JSON object
        EncryptedValue encryptedValue = encryptedValueService.fromJson(database, json);

        // Store in new node
        DatabaseNode newNode = new DatabaseNode(database, name);
        newNode.setValue(encryptedValue);
        remoteSyncNode.add(newNode);
    }

    @Override
    public String toString()
    {
        return "SshOptions{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", user='" + user + '\'' +
                ", destinationPath='" + destinationPath + '\'' +
                ", remotePath='" + remotePath + '\'' +
                ", userPass='" + userPass + '\'' +
                ", privateKeyPath='" + privateKeyPath + '\'' +
                ", privateKeyPass='" + privateKeyPass + '\'' +
                ", proxyHost='" + proxyHost + '\'' +
                ", proxyPort=" + proxyPort +
                ", proxyType='" + proxyType + '\'' +
                ", machineFilter='" + machineFilter + '\'' +
                ", promptUserPass=" + promptUserPass +
                ", promptKeyPass=" + promptKeyPass +
                ", strictHostChecking=" + strictHostChecking +
                '}';
    }

}
