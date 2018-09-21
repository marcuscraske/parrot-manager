package com.limpygnome.parrot.component.sync.thread;

import com.limpygnome.parrot.component.sync.SyncFileComponent;
import com.limpygnome.parrot.component.sync.SyncHandler;
import com.limpygnome.parrot.component.sync.SyncOptions;
import com.limpygnome.parrot.component.sync.SyncProfile;
import com.limpygnome.parrot.component.sync.SyncProfileService;
import com.limpygnome.parrot.component.sync.SyncResult;
import com.limpygnome.parrot.component.sync.SyncThread;
import com.limpygnome.parrot.library.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestSyncThread implements SyncThread
{
    @Autowired
    private SyncProfileService syncProfileService;
    @Autowired
    private SyncFileComponent syncFileComponent;

    @Override
    public SyncResult execute(SyncOptions options, SyncProfile profile)
    {
        SyncResult result;

        SyncHandler handler = syncProfileService.getHandlerForProfile(profile);

        Log log = new Log();
        if (syncFileComponent.isDestinationPathValid(options, log))
        {
            result = handler.test(options, profile);
        }
        else
        {
            result = new SyncResult(profile.getName(), log, false, false);
        }

        return result;
    }

}
