package com.limpygnome.parrot.component.remote;

import com.jcraft.jsch.SftpException;
import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseMerger;
import com.limpygnome.parrot.library.db.MergeLog;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class SshSyncService
{
    private static final Logger LOG = LogManager.getLogger(SshSyncService.class);

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
            sshComponent.download(sshSession, options, null);
        }
        catch (Exception e)
        {
            result = sshComponent.getExceptionMessage(e);
            LOG.error("transfer - {} - exception", options.getRandomToken(), e);
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
            if (!sshComponent.checkRemotePathExists(options, sshSession, null))
            {
                result = "Remote file does not exist - ignore if expected";
            }
        }
        catch (Exception e)
        {
            result = sshComponent.getExceptionMessage(e);
            LOG.error("transfer - {} - exception", options.getRandomToken(), e);
        }
        finally
        {
            cleanup();
        }

        return result;
    }

    synchronized SyncResult sync(SshOptions options, String remotePassword)
    {
        String messages;
        boolean success = true;
        boolean dirty = false;

        Database database = databaseService.getDatabase();

        // alter destination path for this host
        int fullHostNameHash = (options.getHost() + options.getPort()).hashCode();
        String syncPath = options.getDestinationPath();
        syncPath = syncPath + "." + fullHostNameHash + "." + System.currentTimeMillis() + ".sync";

        // begin sync process...
        try
        {
            // connect
            LOG.info("sync - connecting");
            sshSession = sshComponent.connect(options);
            this.options = options;

            // create lock file
            createLock(options);

            // start download...
            LOG.info("sync - downloading");
            boolean exists = sshComponent.download(sshSession, options, syncPath, null);

            MergeLog mergeLog;

            if (exists)
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
                    databaseReaderWriter.save(database, options.getDestinationPath());

                    // reset dirty flag
                    database.setDirty(false);

                    // store dirty for event
                    dirty = true;
                }

                // upload to remote source if database is out of date
                if (mergeLog.isRemoteOutOfDate())
                {
                    LOG.info("sync - uploading to remote host...");
                    sshComponent.upload(sshSession, options, options.getDestinationPath(), null);
                }
                else
                {
                    LOG.info("sync - neither database is dirty");
                }
            }
            else
            {
                LOG.info("sync - uploading current database");

                mergeLog = new MergeLog();
                mergeLog.add("uploading current database, as does not exist remotely");

                String currentPath = databaseService.getPath();
                sshComponent.upload(sshSession, options, currentPath, null);

                mergeLog.add("uploaded successfully");
            }

            // build result
            String hostName = options.getName();
            messages = mergeLog.getMessages(hostName);
        }
        catch (Exception e)
        {
            messages = sshComponent.getExceptionMessage(e);
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
                cleanup();
            }
            catch (Exception e)
            {
                LOG.error("failed cleanup", e);
            }
        }

        // raise event with result
        SyncResult syncResult = new SyncResult(
                messages, success, dirty, options.getName()
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
            // create lock file
            String tmpDir = System.getProperty("java.io.tmpdir");
            File file = new File(tmpDir, "parrot-manager.lock");
            file.createNewFile();

            // check lock file doesn't already exist...
            boolean isLocked;
            int attempts = 0;

            do
            {
                LOG.info("checking for database lock - attempt {}", attempts);

                isLocked = sshComponent.checkRemotePathExists(options, sshSession, "parrot.lock");
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
            sshComponent.upload(sshSession, options, file.getAbsolutePath(), "parrot.lock");
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
            sshComponent.remove(sshSession, options, "parrot.lock");
            LOG.info("remote database lock file removed");
        }
        catch (SftpException | SyncFailureException e)
        {
            LOG.error("failed to cleanup database lock", e);
        }
    }

}
