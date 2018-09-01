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

    void sync(SyncProfile profile, SyncOptions options);

    void download(SyncProfile profile);

    void test(SyncProfile profile);

    void overwrite(SyncProfile profile);

    void unlock(SyncProfile profile);

}
