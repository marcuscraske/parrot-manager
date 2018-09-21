package com.limpygnome.parrot.component.sync.thread;

import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.sync.SyncFileComponent;
import com.limpygnome.parrot.component.sync.SyncHandler;
import com.limpygnome.parrot.component.sync.SyncOptions;
import com.limpygnome.parrot.component.sync.SyncProfile;
import com.limpygnome.parrot.component.sync.SyncProfileService;
import com.limpygnome.parrot.component.sync.SyncResult;
import com.limpygnome.parrot.component.sync.SyncResultService;
import com.limpygnome.parrot.component.sync.SyncThread;
import com.limpygnome.parrot.component.ui.WebStageInitService;
import com.limpygnome.parrot.library.log.Log;
import com.limpygnome.parrot.library.log.LogItem;
import com.limpygnome.parrot.library.log.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AsyncSyncThread implements SyncThread
{
    private static final Logger LOG = LoggerFactory.getLogger(AsyncSyncThread.class);

    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private SyncProfileService syncProfileService;
    @Autowired
    private SyncResultService syncResultService;
    @Autowired
    private WebStageInitService webStageInitService;
    @Autowired
    private SyncFileComponent syncFileComponent;

    @Override
    public SyncResult execute(SyncOptions options, SyncProfile profile)
    {
        SyncResult syncResult;
        Log log = new Log();

        // check there isn't unsaved database changes
        if (databaseService.isDirty())
        {
            LOG.warn("skipped sync due to unsaved database changes");
            log.add(new LogItem(LogLevel.ERROR, true, "Skipped sync due to unsaved database changes"));
            syncResult = new SyncResult(profile.getName(), log, false, false);
        }
        else
        {
            // validate destination path
            if (!syncFileComponent.isDestinationPathValid(options, log))
            {
                syncResult = new SyncResult(profile.getName(), log, false, false);
            }
            else
            {
                // sync...
                SyncHandler handler = syncProfileService.getHandlerForProfile(profile);
                syncResult = handler.sync(options, profile);
            }
        }

        return syncResult;
    }

}
