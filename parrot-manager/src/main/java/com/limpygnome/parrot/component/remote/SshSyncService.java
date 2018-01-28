package com.limpygnome.parrot.component.remote;

import com.jcraft.jsch.SftpException;
import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.remote.ssh.SshComponent;
import com.limpygnome.parrot.component.remote.ssh.SshFile;
import com.limpygnome.parrot.component.remote.ssh.SshOptions;
import com.limpygnome.parrot.component.remote.ssh.SshSession;
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

/**
 * Currently only supports SSH, but this could be split into multiple services for different remote sync options.
 */
@Service
public class SshSyncService
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

        try
        {
            // connect
            sshSession = sshComponent.connect(options);
            this.options = options;

            // create lock
            createLock(options);

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
            cleanup();
        }

        return result;
    }

    synchronized SyncResult sync(SshOptions options, String remotePassword)
    {
        MergeLog mergeLog = new MergeLog();
        boolean success = true;
        boolean dirty = false;

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
            createLock(options);

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

                    // delete old renamed file
                    sshComponent.remove(sshSession, fileSyncBackup);
                    mergeLog.add(new LogItem(LogLevel.DEBUG, "Removed sync backup file"));
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
            // cleanup sync file
            File syncFile = new File(syncPath);

            if (syncFile.exists())
            {
                syncFile.delete();
            }

            // disconnect
            try
            {
                // destroys lock, even if DB currently locked; means slow connections could write at the same time
                // TODO whether this should be changed
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
            // destroy lock file (if it exists)
            if (options != null)
            {
                cleanupLock(options);
            }
            else
            {
                LOG.warn("unable to cleanup database lock file as options is null");
            }

            // destroy session
            sshSession.dispose();
            sshSession = null;

            LOG.info("ssh session cleaned up");
        }
    }

    private void createLock(SshOptions options) throws SyncFailureException
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
                    LOG.info("remote database lock exists, sleeping...");
                    Thread.sleep(1000);
                }
            }
            while (isLocked && attempts < 10);

            if (isLocked)
            {
                throw new RuntimeException("Remote database lock file exists and timed-out waiting for it to disappear. You may need to manually removve it.");
            }

            // upload
            sshComponent.upload(sshSession, fileLocalLock.getAbsolutePath(), fileLock);
            LOG.info("remote database lock file created");
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

    private void cleanupLock(SshOptions options)
    {
        try
        {
            SshFile fileLock = new SshFile(sshSession, options.getRemotePath()).postFixFileName(".lock");
            sshComponent.remove(sshSession, fileLock);
            LOG.info("remote database lock file removed");
        }
        catch (SftpException | SyncFailureException e)
        {
            LOG.error("failed to cleanup database lock", e);
        }
    }

}
