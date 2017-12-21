import { Injectable, Renderer } from '@angular/core';

import { RemoteSyncChangeLogService } from 'app/service/remoteSyncChangeLog.service'

import "app/global-vars"

@Injectable()
export class RemoteSyncService {

    remoteSyncService : any;

    private remoteSyncingFinishedEvent: Function;
    syncing : boolean;
    currentHost : string;

    constructor(
        private remoteSyncChangeLogService: RemoteSyncChangeLogService,
        private renderer: Renderer
    ) {
        this.remoteSyncService = (window as any).remoteSyncService;

        // Setup hook for when remote syncing starts
        this.remoteSyncingFinishedEvent = renderer.listenGlobal("document", "remoteSyncStart", (event) => {
            console.log("received remote sync start event");

            // Update state
            this.syncing = true;

            // Update host being synchronized
            var options = event.data;
            var hostName = options.getName();
            this.currentHost = hostName;

            // Show notification
            toastr.info("syncing " + hostName);
        });

        // Setup hook for when remote syncing finishes
        this.remoteSyncingFinishedEvent = renderer.listenGlobal("document", "remoteSyncFinish", (event) => {
            console.log("received remote sync finish event");

            var messages = event.data.getMessages();
            var isSuccess = event.data.isSuccess();
            var isChanges = event.data.isChanges();
            var hostName = event.data.getHostName();

            // Switch state to not syncing
            this.syncing = false;

            // Send result to logging service
            this.remoteSyncChangeLogService.add(messages);

            // Show notification
            if (isSuccess)
            {
                if (isChanges)
                {
                    toastr.success(hostName + " has changes");
                }
                else
                {
                    toastr.info(hostName + " has no changes");
                }
            }
            else
            {
                toastr.error("failed to synchronize " + hostName);
            }
        });
    }

    createOptions(randomToken, name, host, port, user, remotePath, destinationPath)
    {
        var options = this.remoteSyncService.createOptions(randomToken, name, host, port, user, remotePath, destinationPath);
        return options;
    }

    createOptionsFromNode(node)
    {
        var options = this.remoteSyncService.createOptionsFromNode(node);
        return options;
    }

    getStatus(randomToken)
    {
        var result = this.remoteSyncService.getStatus(randomToken);
        return result;
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

    syncAll()
    {
        this.remoteSyncService.syncAll();
    }

    sync(options)
    {
        this.remoteSyncService.sync(options);
    }

    syncWithAuth(options, remoteDatabasePassword)
    {
        this.remoteSyncService.syncWithAuth(options, remoteDatabasePassword);
    }

    isSyncing() : boolean
    {
        return this.syncing;
    }

    // TODO rename this at some point
    getCurrentHost() : string
    {
        return this.currentHost;
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

}
