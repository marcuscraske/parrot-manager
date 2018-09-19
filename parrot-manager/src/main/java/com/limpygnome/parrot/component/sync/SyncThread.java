package com.limpygnome.parrot.component.sync;

public interface SyncThread
{

    /**
     * Executes the async operation.
     *
     * @param options options
     * @param profile profile
     * @return the result of the operation
     */
    SyncResult execute(SyncOptions options, SyncProfile profile);

}
