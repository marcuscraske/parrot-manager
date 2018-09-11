import { Injectable } from '@angular/core';
import { DatabaseService } from './database.service'
import { SyncSshService } from './syncSsh.service'

import "app/global-vars"

@Injectable()
export class SyncService {

    private syncService : any;

    private syncing : boolean;
    private currentHost : string;

    // TODO deprecated; only temporary, should be multi-threaded in background and UI shows status per each host
    private lastHostSynchronizing: string;

    constructor(
        private databaseService: DatabaseService,
        private syncSshService: SyncSshService
    ) {
        this.syncService = (window as any).syncService;
    }

    createTemporaryOptions()
    {
        var options = this.syncService.createTemporaryOptions();
        return options;
    }

    download(options, profile)
    {
        var result = this.syncService.download(options, profile);
        return result;
    }

    test(options, profile)
    {
        var result = this.syncService.test(options, profile);
        return result;
    }

    overwrite(profile)
    {
        var options = this.syncService.createTemporaryOptions();
        this.authChain(options, profile, (options, profile) => {
            this.syncService.overwrite(options, profile);
        });
    }

    unlock(profile)
    {
        var options = this.syncService.createTemporaryOptions();
        this.authChain(options, profile, (options, profile) => {
            this.syncService.unlock(options, profile);
        });
    }

    syncAll()
    {
        if (this.canContinue())
        {
            this.syncService.syncAll();
        }
    }

    sync(profile, promptForAuth)
    {
        if (this.canContinue())
        {
            if (promptForAuth)
            {
                console.log("sync using auth chain");

                var options = this.syncService.createTemporaryOptions();
                this.authChain(options, profile, (options, profile) => {
                    this.syncService.sync(options, profile);
                });
            }
            else
            {
                console.log("invoking sync");

                var options = this.syncService.getDefaultSyncOptions();
                this.syncService.sync(options, profile);
            }
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
        this.syncService.abort();
    }

    // This is the actual host-cname of the current box / machine / physical host
    getCurrentHostName() : string
    {
        return this.syncService.getCurrentHostName();
    }

    getLastSync()
    {
        return this.syncService.getLastSync();
    }

    getDefaultSyncOptions()
    {
        return this.syncService.getDefaultSyncOptions();
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

    /*
        Auth chain is used to retrieve sufficient authentication details for a profile, in order to
        connect to it.

        This may involve prompting the user for passwords.
    */
    authChain(options, profile, callback)
    {
        console.log("starting auth chain");
        this.authChainPromptDatabasePass(options, profile, (options, profile) => {
            this.syncSshService.authChain(options, profile, (options, profile) => {
                this.authChainFinish(options, profile, callback);
            });
        });
    }

    private authChainPromptDatabasePass(options, profile, callback)
    {
        console.log("prompting for remote db pass...");

        bootbox.prompt({
            title: options.getName() + " - enter database password:",
            inputType: "password",
            callback: (password) => {

                if (password != null)
                {
                    // Update options
                    options.setDatabasePassword(password);

                    // Continue next stage in the chain...
                    console.log("continuing to perform actual sync...");
                    callback(options, profile);
                }
                else
                {
                    console.log("no password specified, user has cancelled entering remote db password");
                }
            }
        });
    }

    private authChainFinish(options, profile, callback)
    {
        console.log("auth chain finished, invoking callback");
        callback(options, profile);
    }

}
