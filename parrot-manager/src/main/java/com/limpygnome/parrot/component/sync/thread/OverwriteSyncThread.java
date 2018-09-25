package com.limpygnome.parrot.component.sync.thread;

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
public class OverwriteSyncThread implements SyncThread
{
    @Autowired
    private SyncProfileService syncProfileService;
    @Autowired
    private WebStageInitService webStageInitService;

    @Override
    public SyncResult execute(SyncOptions options, SyncProfile profile)
    {
        SyncHandler handler = syncProfileService.getHandlerForProfile(profile);
        handler.overwrite(options, profile);
        Log log = new Log();
        log.add(new LogItem(LogLevel.INFO, false, "Remote database overwritten"));
        return new SyncResult(profile, log, true, false);
    }

}
