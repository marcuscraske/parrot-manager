# Remote Sync

## Hosts & Syncing Process
Remote sync details are stored in the following path in a database:

````
/remote-sync
````

For each remote host, a child key is created, with the name irrelevant and changeable:

````
/remote-sync/example.com:22
````

When performing a remote sync with a host, the local database is merged with the remote copy.

Before merging begins, a lock file is created using the hostname of this machine. If the file already exists, the
sync is skipped.


## Host Key
For every host key, the value is a JSON object with the following configuration:

````
{
    "host"          : "string, mandatory",
    "port"          : "number, optional",
    "user"          : "string, mandatory",
    "pass"          : "string, optional",
    "key"           : "string, optional",
}
````

If a password or key is not stored, but a boolean is present, the user is prompted for either the password or key
when syncing.
