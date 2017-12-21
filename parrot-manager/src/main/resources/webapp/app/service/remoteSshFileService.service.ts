import { Injectable, Renderer } from '@angular/core';

import { RemoteSyncChangeLogService } from 'app/service/remoteSyncChangeLog.service'

import "app/global-vars"

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

    syncAll()
    {
        this.remoteSshFileService.syncAll();
    }

    sync(options)
    {
        this.remoteSshFileService.sync(options);
    }

    syncWithAuth(options, remoteDatabasePassword)
    {
        this.remoteSshFileService.syncWithAuth(options, remoteDatabasePassword);
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
        this.remoteSshFileService.abort();
    }

    // This is the actual host-name of the current box / machine / physical host
    getCurrentHostname() : string
    {
        return this.remoteSshFileService.getCurrentHostName();
    }

    getLastSync()
    {
        return this.remoteSshFileService.getLastSync();
    }

}
