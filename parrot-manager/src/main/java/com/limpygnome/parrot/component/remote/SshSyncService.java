package com.limpygnome.parrot.component.remote;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.remote.ssh.SshComponent;
import com.limpygnome.parrot.component.remote.ssh.SshFile;
import com.limpygnome.parrot.component.remote.ssh.SshOptions;
import com.limpygnome.parrot.component.remote.ssh.SshSession;
import com.limpygnome.parrot.component.settings.Settings;
import com.limpygnome.parrot.component.settings.event.SettingsRefreshedEvent;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseMerger;
import com.limpygnome.parrot.library.db.log.LogItem;
import com.limpygnome.parrot.library.db.log.LogLevel;
import com.limpygnome.parrot.library.db.log.MergeLog;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Currently only supports SSH, but this could be split into multiple services for different remote sync options.
 */
@Service
public class SshSyncService implements SettingsRefreshedEvent
{
    private static final Logger LOG = LoggerFactory.getLogger(SshSyncService.class);

    @Autowired
    private SshComponent sshComponent;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private DatabaseReaderWriter databaseReaderWriter;
    @Autowired
    private DatabaseMerger databaseMerger;

    private SshSession sshSession;
    private SshOptions options;

    private long remoteBackupsRetained;

    @Override
    public void eventSettingsRefreshed(Settings settings)
    {
        remoteBackupsRetained = settings.getRemoteBackupsRetained().getSafeLong(30L);
    }

    synchronized String download(SshOptions options)
    {
        String result = null;
        sshSession = null;

        try
        {
            // Connect
            sshSession = sshComponent.connect(options);
            this.options = options;

            // Start download...
            SshFile source = new SshFile(sshSession, options.getRemotePath());
            String destionation = options.getDestinationPath();

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

    synchronized String test(SshOptions options)
    {
        String result = null;
        sshSession = null;
        boolean createdLock = false;

        try
        {
            // connect
            sshSession = sshComponent.connect(options);
            this.options = options;

            // create lock
            createLock(null, options);
            createdLock = true;

            // check remote connection works and file exists
            SshFile file = new SshFile(sshSession, options.getRemotePath());

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
                cleanupLock(null, options);
            }
            cleanup();
        }

        return result;
    }

    synchronized SyncResult overwrite(SshOptions options)
    {
        MergeLog mergeLog = new MergeLog();
        boolean createdLock = false;
        boolean success = true;

        try
        {
            // connect
            sshSession = sshComponent.connect(options);
            this.options = options;

            // create lock
            createLock(mergeLog, options);
            createdLock = true;

            String localPath = databaseService.getPath();
            SshFile fileRemote = new SshFile(sshSession, options.getRemotePath());
            SshFile fileRemoteSyncBackup = fileRemote.clone().postFixFileName(".sync");
            SshFile fileRemoteBackup = fileRemote.clone().preFixFileName(".").postFixFileName("." + System.currentTimeMillis());

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
                convertToRemoteBackupOrDelete(mergeLog, fileRemoteSyncBackup, fileRemoteBackup);
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
                cleanupLock(mergeLog, options);
            }

            // disconnect
            cleanup();
        }

        SyncResult result = new SyncResult(
            options.getName(), mergeLog, success, false
        );
        return result;
    }

    synchronized SyncResult unlock(SshOptions options)
    {
        MergeLog mergeLog = new MergeLog();
        boolean success;

        try
        {
            // connect
            sshSession = sshComponent.connect(options);

            // remove lock file
            SshFile fileLock = new SshFile(sshSession, options.getRemotePath()).postFixFileName(".lock");
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

        return new SyncResult(options.getName(), mergeLog, success, false);
    }

    synchronized SyncResult sync(SshOptions options, String remotePassword)
    {
        MergeLog mergeLog = new MergeLog();
        boolean success = true;
        boolean dirty = false;
        boolean createdLock = false;

        Database database = databaseService.getDatabase();

        // alter destination path for this host
        int fullHostNameHash = (options.getHost() + options.getPort()).hashCode();
        String syncPath = options.getDestinationPath();
        syncPath = syncPath + "." + fullHostNameHash + "." + System.currentTimeMillis() + ".sync";

        // fetch current path to database
        String currentPath = databaseService.getPath();

        // begin sync process...
        try
        {
            // connect
            LOG.info("sync - connecting");
            sshSession = sshComponent.connect(options);
            this.options = options;

            SshFile source = new SshFile(sshSession, options.getRemotePath());
            SshFile fileSyncBackup = new SshFile(sshSession, options.getRemotePath()).postFixFileName(".sync");

            // create lock file
            createLock(mergeLog, options);
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
                    SshFile fileBackup = source.clone().preFixFileName(".").postFixFileName("." + System.currentTimeMillis());
                    convertToRemoteBackupOrDelete(mergeLog, fileSyncBackup, fileBackup);
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
                cleanupLock(mergeLog, options);
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
                options.getName(), mergeLog, success, dirty
        );
        return syncResult;
    }

    synchronized void cleanup()
    {
        if (sshSession != null)
        {
            // destroy session
            sshSession.dispose();
            sshSession = null;

            LOG.info("ssh session cleaned up");
        }
    }

    private void createLock(MergeLog mergeLog, SshOptions options) throws SyncFailureException
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

    private void cleanupLock(MergeLog mergeLog, SshOptions options)
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

    private void convertToRemoteBackupOrDelete(MergeLog mergeLog, SshFile currentFile, SshFile backupFile) throws SftpException, JSchException
    {
        // convert if backups enabled, otherwise just delete it
        if (remoteBackupsRetained > 0)
        {
            // rename as backup
            sshComponent.rename(sshSession, currentFile, backupFile);
            mergeLog.add(new LogItem(LogLevel.DEBUG, "Created new backup - " + backupFile.getFileName()));

            // fetch list of files
            SshFile parent = backupFile.getParent(sshSession);
            List<SshFile> files = sshComponent.list(sshSession, parent);

            // drop those outside retained limit
            if (files.size() > remoteBackupsRetained)
            {
                mergeLog.add(new LogItem(LogLevel.DEBUG, "Too many remote backups - " + files.size() + " / " + remoteBackupsRetained));

                for (int i = (int) remoteBackupsRetained; i < files.size(); i++)
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
