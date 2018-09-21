package com.limpygnome.parrot.component.sync.thread;

import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.sync.SyncFileComponent;
import com.limpygnome.parrot.component.sync.SyncHandler;
import com.limpygnome.parrot.component.sync.SyncOptions;
import com.limpygnome.parrot.component.sync.SyncProfile;
import com.limpygnome.parrot.component.sync.SyncProfileService;
import com.limpygnome.parrot.component.sync.SyncResult;
import com.limpygnome.parrot.component.sync.SyncThread;
import com.limpygnome.parrot.component.ui.WebStageInitService;
import com.limpygnome.parrot.library.log.Log;
import com.limpygnome.parrot.library.log.LogItem;
import com.limpygnome.parrot.library.log.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DownloadSyncThread implements SyncThread
{
    @Autowired
    private SyncProfileService syncProfileService;
    @Autowired
    private WebStageInitService webStageInitService;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private SyncFileComponent syncFileComponent;

    @Override
    public SyncResult execute(SyncOptions options, SyncProfile profile)
    {
        SyncResult result;

        // Check destination path for database is valid
        Log log = new Log();
        if (syncFileComponent.isDestinationPathValid(options, log))
        {
            // Start downloading the file...
            SyncHandler handler = syncProfileService.getHandlerForProfile(profile);
            result = handler.download(options, profile);

            if (result.isSuccess())
            {
                // Open the database...
                String path = options.getDestinationPath();
                String password = options.getDatabasePassword();
                String message = databaseService.open(path, password);

                if (message == null)
                {
                    // Persist the profile to the newly opened database
                    syncProfileService.save(profile);
                }
                else
                {
                    result.getLog().add(new LogItem(LogLevel.ERROR, false, message));
                }
            }
        }
        else
        {
            result = new SyncResult(profile.getName(), log, false, false);
        }

        return result;
    }

}
