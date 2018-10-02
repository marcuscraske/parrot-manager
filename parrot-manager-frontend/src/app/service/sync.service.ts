import { Injectable } from '@angular/core';
import { DatabaseService } from './database.service'
import { SyncProfileService } from './syncProfile.service'
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
        private syncProfileService: SyncProfileService,
        private syncSshService: SyncSshService
    ) {
        this.syncService = (window as any).syncService;
    }

    createTemporaryOptions()
    {
        var options = this.syncService.createTemporaryOptions();
        return options;
    }

    test(options, profile, type)
    {
        this.authChain(options, profile, (options, profile) => {
            this.syncService.test(options, profile);
        });
    }

    download(options, profileId)
    {
        // TODO what if DB open already? how is this used?
        var profile = this.syncProfileService.fetchById(profileId);
        this.authChain(options, profile, (options, profile) => {
            this.syncService.download(options, profile);
        });
    }

    overwrite(options, profileId)
    {
        if (this.canContinue())
        {
            var profile = this.syncProfileService.fetchById(profileId);
            this.authChain(options, profile, (options, profile) => {
                this.syncService.overwrite(options, profile);
            });
        }
    }

    unlock(options, profileId)
    {
        var profile = this.syncProfileService.fetchById(profileId);
        this.authChain(options, profile, (options, profile) => {
            var nativeProfile = this.syncProfileService.toNative(profile);
            this.syncService.unlock(options, nativeProfile);
        });
    }

    syncAll()
    {
        if (this.canContinue())
        {
            this.syncService.syncAll();
        }
    }

    sync(options, profileId)
    {
        if (this.canContinue())
        {
            var profile = this.syncProfileService.fetchById(profileId);
            this.authChain(options, profile, (options, profile) => {
                this.syncService.sync(options, profile);
            });
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
    private authChain(options, profile, callback)
    {
        console.log("starting auth chain");

        if (options == null)
        {
            throw "Auth chain cannot be invoked with null options";
        }
        else if (profile == null)
        {
            throw "Auth chain cannot be invoked with null profile";
        }

        this.syncSshService.authChain(options, profile, (options, profile) => {
            this.authChainPromptDatabasePass(options, profile, (options, profile) => {
                this.authChainFinish(options, profile, callback);
            });
        });
    }

    private authChainPromptDatabasePass(options, profile, callback)
    {
        console.log("prompting for remote db pass...");

        var promptDbPass = !options.isDatabasePassword();
        if (promptDbPass)
        {
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
        else
        {
            console.log("skipped prompting for database password, as present");
            callback(options, profile);
        }
    }

    private authChainFinish(options, profile, callback)
    {
        console.log("auth chain finished, invoking callbac - profile: " + (profile != null) + ", options: " + (options != null));
        callback(options, profile);
    }

}
