package com.limpygnome.parrot.component.sync.ssh;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.settings.Settings;
import com.limpygnome.parrot.component.settings.event.SettingsRefreshedEvent;
import com.limpygnome.parrot.component.sync.SyncFailureException;
import com.limpygnome.parrot.component.sync.SyncHandler;
import com.limpygnome.parrot.component.sync.SyncOptions;
import com.limpygnome.parrot.component.sync.SyncProfile;
import com.limpygnome.parrot.component.sync.SyncResult;
import com.limpygnome.parrot.lib.database.EncryptedValueService;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseMerger;
import com.limpygnome.parrot.library.db.DatabaseNode;
import com.limpygnome.parrot.library.db.log.LogItem;
import com.limpygnome.parrot.library.db.log.LogLevel;
import com.limpygnome.parrot.library.db.log.MergeLog;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Currently only supports SSH, but this could be split into multiple services for different remote sync options.
 */
@Service("ssh")
public class SshSyncHandler implements SettingsRefreshedEvent, SyncHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(SshSyncHandler.class);

    private static final long DEFAULT_REMOTE_BACKUPS_RETAINED = 30L;

    @Autowired
    private SshComponent sshComponent;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private DatabaseReaderWriter databaseReaderWriter;
    @Autowired
    private DatabaseMerger databaseMerger;
    @Autowired
    private EncryptedValueService encryptedValueService;

    private SshSession sshSession;

    private long remoteBackupsRetained;

    @Override
    public void eventSettingsRefreshed(Settings settings)
    {
        remoteBackupsRetained = settings.getRemoteBackupsRetained().getSafeLong(DEFAULT_REMOTE_BACKUPS_RETAINED);
    }

    @Override
    public SyncProfile createProfile()
    {
        return new SshSyncProfile();
    }

    @Override
    public DatabaseNode serialize(SyncProfile profile)
    {
        Database database = databaseService.getDatabase();
        SshSyncProfile syncProfile = (SshSyncProfile) profile;

        try
        {
            // Serialize as JSON string
            ObjectMapper mapper = new ObjectMapper();
            String rawJson = mapper.writeValueAsString(syncProfile);

            // Parse as JSON for sanity
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(rawJson).getAsJsonObject();

            // Create encrypted JSON object
            EncryptedValue encryptedValue = encryptedValueService.fromJson(database, json);

            // Store in new node
            DatabaseNode newNode = new DatabaseNode(database, profile.getName());
            newNode.setValue(encryptedValue);
            return newNode;
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to serialize profile", e);
        }
    }

    @Override
    public SyncProfile deserialize(DatabaseNode node)
    {
        Database database = databaseService.getDatabase();

        try
        {
            // Fetch value as string
            String value = encryptedValueService.asString(database, node.getValue());

            // Deserialize into object
            ObjectMapper mapper = new ObjectMapper();
            SshSyncProfile profile =  mapper.readValue(value, SshSyncProfile.class);
            return profile;
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to deserialize database node to profile", e);
        }
    }

    @Override
    public boolean handles(SyncProfile profile)
    {
        return profile instanceof SshSyncProfile;
    }

    @Override
    public synchronized String download(SyncOptions options, SyncProfile profile)
    {
        String result = null;
        sshSession = null;

        SshSyncProfile sshProfile = (SshSyncProfile) profile;

        try
        {
            // Connect
            sshSession = sshComponent.connect(sshProfile);

            // Start download...
            SshFile source = new SshFile(sshSession, sshProfile.getRemotePath());
            String destionation = sshProfile.getDestinationPath();

            sshComponent.download(sshSession, source, destionation);
        }
        catch (Exception e)
        {
            result = sshComponent.getExceptionMessage(e);
            LOG.error("failed to download remote file", e);
        }
        finally
        {
            cleanup();
        }

        return result;
    }

    @Override
    public synchronized String test(SyncOptions options, SyncProfile profile)
    {
        String result = null;
        sshSession = null;
        boolean createdLock = false;

        SshSyncProfile sshProfile = (SshSyncProfile) profile;

        try
        {
            // connect
            sshSession = sshComponent.connect(sshProfile);

            // create lock
            createLock(null, sshProfile);
            createdLock = true;

            // check remote connection works and file exists
            SshFile file = new SshFile(sshSession, sshProfile.getRemotePath());

            if (!sshComponent.checkRemotePathExists(sshSession, file))
            {
                result = "Remote file does not exist - ignore if expected";
            }
        }
        catch (Exception e)
        {
            result = sshComponent.getExceptionMessage(e);
            LOG.error("failed to test if remote file exists", e);
        }
        finally
        {
            if (createdLock)
            {
                cleanupLock(null, sshProfile);
            }
            cleanup();
        }

        return result;
    }

    @Override
    public synchronized SyncResult overwrite(SyncOptions options, SyncProfile profile)
    {
        MergeLog mergeLog = new MergeLog();
        boolean createdLock = false;
        boolean success = true;

        SshSyncProfile sshProfile = (SshSyncProfile) profile;

        try
        {
            // connect
            sshSession = sshComponent.connect(sshProfile);

            // create lock
            createLock(mergeLog, sshProfile);
            createdLock = true;

            String localPath = databaseService.getPath();
            SshFile fileRemote = new SshFile(sshSession, sshProfile.getRemotePath());
            SshFile fileRemoteSyncBackup = fileRemote.clone().postFixFileName(".sync");

            // check if database exists
            if (!sshComponent.checkRemotePathExists(sshSession, fileRemote))
            {
                mergeLog.add(new LogItem(LogLevel.ERROR, "Remote file does not exist - ignore if expected"));
            }
            else
            {
                // move current database as sync backup (in case upload fails)
                sshComponent.rename(sshSession, fileRemote, fileRemoteSyncBackup);

                // upload current database
                sshComponent.upload(sshSession, localPath, fileRemote);

                // delete or convert to backup
                convertToRemoteBackupOrDelete(mergeLog, fileRemote, fileRemoteSyncBackup);
            }
        }
        catch (Exception e)
        {
            success = false;

            String message = sshComponent.getExceptionMessage(e);
            mergeLog.add(new LogItem(LogLevel.ERROR, message));

            LOG.error("failed to overwrite remote database", e);
        }
        finally
        {
            // cleanup lock
            if (createdLock)
            {
                cleanupLock(mergeLog, sshProfile);
            }

            // disconnect
            cleanup();
        }

        SyncResult result = new SyncResult(
            profile.getName(), mergeLog, success, false
        );
        return result;
    }

    @Override
    public synchronized SyncResult unlock(SyncOptions options, SyncProfile profile)
    {
        MergeLog mergeLog = new MergeLog();
        boolean success;

        SshSyncProfile sshProfile = (SshSyncProfile) profile;

        try
        {
            // connect
            sshSession = sshComponent.connect(sshProfile);

            // remove lock file
            SshFile fileLock = new SshFile(sshSession, sshProfile.getRemotePath()).postFixFileName(".lock");
            sshComponent.remove(sshSession, fileLock);
            success = true;

            mergeLog.add(new LogItem(LogLevel.INFO, "Removed remote database lock file"));
        }
        catch (JSchException | SftpException e)
        {
            LOG.error("failed to remove database lock", e);
            success = false;
            mergeLog.add(new LogItem(LogLevel.ERROR, "Failed to remove remote database lock file - " + e.getMessage()));
        }
        finally
        {
            cleanup();
        }

        return new SyncResult(sshProfile.getName(), mergeLog, success, false);
    }

    @Override
    public synchronized SyncResult sync(SyncOptions options, SyncProfile profile)
    {
        MergeLog mergeLog = new MergeLog();
        boolean success = true;
        boolean dirty = false;
        boolean createdLock = false;

        SshSyncProfile sshProfile = (SshSyncProfile) profile;

        Database database = databaseService.getDatabase();

        // alter destination path for this host
        int fullHostNameHash = (sshProfile.getHost() + sshProfile.getPort()).hashCode();
        String syncPath = sshProfile.getDestinationPath();
        syncPath = syncPath + "." + fullHostNameHash + "." + System.currentTimeMillis() + ".sync";

        // fetch current path to database
        String currentPath = databaseService.getPath();

        // begin sync process...
        try
        {
            // connect
            LOG.info("sync - connecting");
            sshSession = sshComponent.connect(sshProfile);

            SshFile source = new SshFile(sshSession, sshProfile.getRemotePath());
            SshFile fileSyncBackup = new SshFile(sshSession, sshProfile.getRemotePath()).postFixFileName(".sync");

            // create lock file
            createLock(mergeLog, sshProfile);
            createdLock = true;

            // check whether an old renamed file exists; if so, restore it
            if (sshComponent.checkRemotePathExists(sshSession, fileSyncBackup))
            {
                mergeLog.add(new LogItem(LogLevel.ERROR, "Old file from previous failed sync found"));

                // move current file to corrupted
                SshFile fileCorrupted = source.clone().postFixFileName(".corrupted." + System.currentTimeMillis());
                sshComponent.rename(sshSession, source, fileCorrupted);
                mergeLog.add(new LogItem(LogLevel.INFO, "Corrupted file moved to " + fileCorrupted.getFileName()));

                // restore backup file
                sshComponent.rename(sshSession, fileSyncBackup, source);
                mergeLog.add(new LogItem(LogLevel.INFO, "Sync backup file restored"));
            }

            // start download...
            LOG.info("sync - downloading");

            String error = sshComponent.download(sshSession, source, syncPath);

            if (error == null)
            {
                String remotePassword = options.getDatabasePassword();

                // load remote database
                LOG.info("sync - loading remote database");
                Database remoteDatabase = databaseReaderWriter.open(syncPath, remotePassword.toCharArray());

                // perform merge and check if any change occurred...
                LOG.info("sync - performing merge...");
                mergeLog = databaseMerger.merge(remoteDatabase, database, remotePassword.toCharArray());

                // save current database if dirty
                if (database.isDirty())
                {
                    LOG.info("sync - database(s) dirty, saving...");
                    databaseReaderWriter.save(database, currentPath);

                    // reset dirty flag
                    database.setDirty(false);

                    // store dirty for event
                    dirty = true;
                }

                // upload to remote source if database is out of date
                if (mergeLog.isRemoteOutOfDate())
                {
                    LOG.info("sync - uploading to remote host...");

                    // move current file as backup in case upload fails
                    sshComponent.rename(sshSession, source, fileSyncBackup);
                    mergeLog.add(new LogItem(LogLevel.DEBUG, "Renamed remote database in case sync fails"));

                    // upload new file
                    sshComponent.upload(sshSession, currentPath, source);
                    mergeLog.add(new LogItem(LogLevel.INFO, "Uploaded database"));

                    // delete or create backup out of sync file
                    convertToRemoteBackupOrDelete(mergeLog, source, fileSyncBackup);
                }
                else
                {
                    LOG.info("sync - neither database is dirty");
                }
            }
            else
            {
                LOG.info("sync - uploading current database");

                mergeLog.add(new LogItem(LogLevel.DEBUG, "Uploading current database, as does not exist remotely"));

                sshComponent.upload(sshSession, currentPath, source);

                mergeLog.add(new LogItem(LogLevel.INFO, "Uploaded database for first time"));
            }
        }
        catch (Exception e)
        {
            if (e instanceof InterruptedException)
            {
                throw new RuntimeException("Sync aborted");
            }

            // Convert to failed merge
            String message = sshComponent.getExceptionMessage(e);
            mergeLog = new MergeLog();
            mergeLog.add(new LogItem(LogLevel.ERROR, e.getClass().getSimpleName() + " - " + message));

            success = false;
            LOG.error("sync - exception", e);
        }
        finally
        {
            // remove remote lock
            if (createdLock)
            {
                cleanupLock(mergeLog, sshProfile);
            }

            // cleanup sync file
            File syncFile = new File(syncPath);

            if (syncFile.exists())
            {
                syncFile.delete();
            }

            // disconnect
            try
            {
                cleanup();
            }
            catch (Exception e)
            {
                LOG.error("failed cleanup", e);
            }
        }

        // raise event with result
        SyncResult syncResult = new SyncResult(
                profile.getName(), mergeLog, success, dirty
        );
        return syncResult;
    }

    @Override
    public boolean canAutoSync(SyncOptions options, SyncProfile profile)
    {
        SshSyncProfile syncProfile = (SshSyncProfile) profile;
        return !syncProfile.isPromptKeyPass() && !syncProfile.isPromptUserPass();
    }

    private synchronized void cleanup()
    {
        if (sshSession != null)
        {
            // destroy session
            sshSession.dispose();
            sshSession = null;

            LOG.info("ssh session cleaned up");
        }
    }

    private void createLock(MergeLog mergeLog, SshSyncProfile options) throws SyncFailureException
    {
        try
        {
            SshFile fileLock = new SshFile(sshSession, options.getRemotePath()).postFixFileName(".lock");

            // create lock file
            String tmpDir = System.getProperty("java.io.tmpdir");
            File fileLocalLock = new File(tmpDir, "parrot-manager.lock");
            fileLocalLock.createNewFile();

            // check lock file doesn't already exist...
            boolean isLocked;
            int attempts = 0;

            do
            {
                LOG.info("checking for database lock - attempt {}", attempts);

                isLocked = sshComponent.checkRemotePathExists(sshSession, fileLock);
                attempts++;

                if (isLocked)
                {
                    if (mergeLog != null)
                    {
                        mergeLog.add(new LogItem(LogLevel.DEBUG, "Remote lock exists - attempt " + attempts));
                    }
                    LOG.info("remote database lock exists, sleeping...");
                    Thread.sleep(1000);
                }
            }
            while (isLocked && attempts < 10);

            if (isLocked)
            {
                throw new RuntimeException("Remote database lock file exists and timed-out waiting for it to disappear. Use unlock database button, or manually remove lock file.");
            }

            // upload
            sshComponent.upload(sshSession, fileLocalLock.getAbsolutePath(), fileLock);

            if (mergeLog != null)
            {
                mergeLog.add(new LogItem(LogLevel.DEBUG, "Created remote lock"));
            }
            LOG.info("remote database lock file created");
        }
        catch (JSchException e)
        {
            String message = sshComponent.getExceptionMessage(e);
            throw new SyncFailureException(message, e);
        }
        catch (IOException e)
        {
            throw new SyncFailureException("Failed to create lock file locally", e);
        }
        catch (SftpException | InterruptedException e)
        {
            throw new SyncFailureException("Failed to create lock file - " + e.getMessage(), e);
        }
    }

    private void cleanupLock(MergeLog mergeLog, SshSyncProfile options)
    {
        try
        {
            SshFile fileLock = new SshFile(sshSession, options.getRemotePath()).postFixFileName(".lock");
            sshComponent.remove(sshSession, fileLock);

            if (mergeLog != null)
            {
                mergeLog.add(new LogItem(LogLevel.DEBUG, "Removed lock file"));
            }
            LOG.info("remote database lock file removed");
        }
        catch (SftpException | JSchException e)
        {
            if (mergeLog != null)
            {
                mergeLog.add(new LogItem(LogLevel.ERROR, "Failed to remove remote lock"));
            }
            LOG.error("failed to cleanup database lock", e);
        }
    }

    private void convertToRemoteBackupOrDelete(MergeLog mergeLog, SshFile databaseFile, SshFile currentFile) throws SftpException, JSchException
    {
        SshFile backupFile = databaseFile.clone().preFixFileName(".").postFixFileName("." + System.currentTimeMillis());

        // convert if backups enabled, otherwise just delete it
        if (remoteBackupsRetained > 0)
        {
            // rename as backup
            sshComponent.rename(sshSession, currentFile, backupFile);
            mergeLog.add(new LogItem(LogLevel.DEBUG, "Created new backup - " + backupFile.getFileName()));

            // fetch list of files
            SshFile parent = backupFile.getParent(sshSession);
            String escapedFileName = Pattern.quote("." + databaseFile.getFileName() + ".");
            List<SshFile> files = sshComponent.list(
                    sshSession, parent, sshFile -> sshFile.getFileName().matches(escapedFileName + ".[0-9]+")
            );

            // drop those outside retained limit
            if (files.size() > remoteBackupsRetained)
            {
                mergeLog.add(new LogItem(LogLevel.DEBUG, "Too many remote backups - " + files.size() + " / " + remoteBackupsRetained));

                int culled = files.size() - (int) remoteBackupsRetained;
                for (int i = 0; i < culled; i++)
                {
                    SshFile file = files.get(i);
                    sshComponent.remove(sshSession, file);
                    mergeLog.add(new LogItem(LogLevel.DEBUG, "Removed file " + file.getFileName()));
                }
            }
            else
            {
                mergeLog.add(new LogItem(LogLevel.DEBUG, "No remote backups culled - " + files.size() + " / " + remoteBackupsRetained));
            }
        }
        else
        {
            // remove file
            sshComponent.remove(sshSession, backupFile);
            mergeLog.add(new LogItem(LogLevel.DEBUG, "Removed file " + backupFile.getFileName()));
        }
    }

}
