package com.limpygnome.parrot.component.sync.thread;

import com.limpygnome.parrot.component.sync.SyncHandler;
import com.limpygnome.parrot.component.sync.SyncOptions;
import com.limpygnome.parrot.component.sync.SyncProfile;
import com.limpygnome.parrot.component.sync.SyncProfileService;
import com.limpygnome.parrot.component.sync.SyncResult;
import com.limpygnome.parrot.component.sync.SyncThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UnlockSyncThread implements SyncThread
{
    @Autowired
    private SyncProfileService syncProfileService;

    @Override
    public SyncResult execute(SyncOptions options, SyncProfile profile)
    {
        SyncHandler handler = syncProfileService.getHandlerForProfile(profile);
        handler.unlock(options, profile);
        return new SyncResult(profile.getName(), true, null);
    }

}
