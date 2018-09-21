package com.limpygnome.parrot.component.sync;

import com.limpygnome.parrot.library.db.DatabaseNode;

/**
 * Handles remote synchronizing operations for a specific transfer method e.g. SSH.
 */
public interface SyncHandler
{

    SyncProfile createProfile();

    DatabaseNode serialize(SyncProfile profile);

    SyncProfile deserialize(DatabaseNode node);

    boolean handles(SyncProfile profile);

    SyncResult sync(SyncOptions options, SyncProfile profile);

    boolean canAutoSync(SyncOptions options, SyncProfile profile);

    SyncResult download(SyncOptions options, SyncProfile profile);

    SyncResult test(SyncOptions options, SyncProfile profile);

    SyncResult overwrite(SyncOptions options, SyncProfile profile);

    SyncResult unlock(SyncOptions options, SyncProfile profile);

}
