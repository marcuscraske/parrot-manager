import { Injectable, Renderer } from '@angular/core';

import { RemoteSyncChangeLogService } from 'app/service/remoteSyncChangeLog.service'

@Injectable()
export class RemoteSshFileService {

    remoteSshFileService : any;

    private remoteSyncingFinishedEvent: Function;
    syncing : boolean;
    currentHost : string;

    constructor(
        private remoteSyncChangeLogService: RemoteSyncChangeLogService,
        private renderer: Renderer
    ) {
        this.remoteSshFileService = (window as any).remoteSshFileService;

        // Setup hook for when remote syncing starts
        this.remoteSyncingFinishedEvent = renderer.listenGlobal("document", "remoteSyncStart", (event) => {
            console.log("received remote sync start event");

            // Update state
            this.syncing = true;

            // Update host being synchronized
            var options = event.data;
            this.currentHost = "test";//options.getName();
        });

        // Setup hook for when remote syncing finishes
        this.remoteSyncingFinishedEvent = renderer.listenGlobal("document", "remoteSyncFinish", (event) => {
            console.log("received remote sync finish event");

            // Switch state to not syncing
            this.syncing = false;

            // Send result to logging service
            var result = event.data;
            this.remoteSyncChangeLogService.add(result);
        });
    }

    createOptions(randomToken, name, host, port, user, remotePath, destinationPath)
    {
        var options = this.remoteSshFileService.createOptions(randomToken, name, host, port, user, remotePath, destinationPath);
        return options;
    }

    createOptionsFromNode(node)
    {
        var options = this.remoteSshFileService.createOptionsFromNode(node);
        return options;
    }

    getStatus(randomToken)
    {
        var result = this.remoteSshFileService.getStatus(randomToken);
        return result;
    }

    download(options)
    {
        var result = this.remoteSshFileService.download(options);
        return result;
    }

    test(options)
    {
        var result = this.remoteSshFileService.test(options);
        return result;
    }

    sync(database, options, remoteDatabasePassword)
    {
        // Initialize syncing; callback comes from hook
        this.remoteSshFileService.sync(database, options, remoteDatabasePassword);
    }

    isSyncing() : boolean
    {
        return this.syncing;
    }

    getCurrentHost() : string
    {
        return this.currentHost;
    }

    abort()
    {
        this.remoteSshFileService.abort();
    }

}
