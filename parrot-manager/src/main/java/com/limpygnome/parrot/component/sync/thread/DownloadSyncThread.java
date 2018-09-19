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
        SyncResult syncResult;

        // Check destination path for database is valid
        String result = syncFileComponent.checkDestinationPath(options);

        if (result == null)
        {
            // Start downloading the file...
            SyncHandler handler = syncProfileService.getHandlerForProfile(profile);
            result = handler.download(options, profile);

            if (result == null)
            {
                // Open the database...
                String path = options.getDestinationPath();
                String password = options.getDatabasePassword();
                result = databaseService.open(path, password);

                if (result == null)
                {
                    // Persist the profile to the newly opened database
                    syncProfileService.save(profile);
                }
            }
        }

        // Wrap up the result
        if (result != null)
        {
            syncResult = new SyncResult(profile.getName(), false, result);
        }
        else
        {
            syncResult = new SyncResult(profile.getName(), true, null);
        }

        return syncResult;
    }

}
