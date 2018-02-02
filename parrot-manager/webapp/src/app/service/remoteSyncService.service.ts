import { Injectable } from '@angular/core';
import { DatabaseService } from './database.service'

import "app/global-vars"

@Injectable()
export class RemoteSyncService {

    remoteSyncService : any;

    private remoteSyncingFinishedEvent: Function;
    private syncing : boolean;
    private currentHost : string;

    // TODO deprecated; only temporary, should be multi-threaded in background and UI shows status per each host
    private lastHostSynchronizing: string;

    constructor(
        private databaseService: DatabaseService
    ) {
        this.remoteSyncService = (window as any).remoteSyncService;
    }

    createOptions(name, host, port, user, remotePath, destinationPath)
    {
        var options = this.remoteSyncService.createOptions(name, host, port, user, remotePath, destinationPath);
        return options;
    }

    createOptionsFromNode(database, node)
    {
        var options = this.remoteSyncService.createOptionsFromNode(database, node);
        return options;
    }

    download(options)
    {
        var result = this.remoteSyncService.download(options);
        return result;
    }

    test(options)
    {
        var result = this.remoteSyncService.test(options);
        return result;
    }

    overwrite(options)
    {
        this.remoteSyncService.overwrite(options);
    }

    unlock(options)
    {
        this.remoteSyncService.unlock(options);
    }

    syncAll()
    {
        if (this.canContinue())
        {
            this.remoteSyncService.syncAll();
        }
    }

    sync(options)
    {
        if (this.canContinue())
        {
            this.remoteSyncService.sync(options);
        }
    }

    syncWithAuth(options, remoteDatabasePassword)
    {
        if (this.canContinue())
        {
            this.remoteSyncService.syncWithAuth(options, remoteDatabasePassword);
        }
    }

    setSyncing(syncing)
    {
        this.syncing = syncing;
    }

    isSyncing() : boolean
    {
        return this.syncing;
    }

    // TODO deprecated
    setLastHostSynchronizing(lastHostSynchronizing)
    {
        this.lastHostSynchronizing = lastHostSynchronizing;
    }

    // TODO deprecated
    getLastHostSynchronizing() : string
    {
        return this.lastHostSynchronizing;
    }

    abort()
    {
        this.remoteSyncService.abort();
    }

    // This is the actual host-name of the current box / machine / physical host
    getCurrentHostname() : string
    {
        return this.remoteSyncService.getCurrentHostName();
    }

    getLastSync()
    {
        return this.remoteSyncService.getLastSync();
    }

    private canContinue() : boolean
    {
        var result = !this.databaseService.isDirty();
        if (!result)
        {
            toastr.error("Unable to sync until you save your database changes.");
        }
        return result;
    }

}
