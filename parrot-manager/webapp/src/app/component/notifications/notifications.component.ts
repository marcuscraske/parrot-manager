import { Component, Renderer } from '@angular/core';

import { RemoteSyncService } from 'app/service/remoteSyncService.service'

@Component({
  selector: 'notifications',
  template: ''
})
export class NotificationsComponent
{
    private syncStartEvent: Function;
    private syncFinishEvent: Function;

    constructor(
        private renderer: Renderer,
        private remoteSyncService: RemoteSyncService
    ) { }

    ngOnInit()
    {
        // Setup hook for when remote syncing starts
        this.syncStartEvent = this.renderer.listenGlobal("document", "remoteSyncStart", (event) => {
            console.log("received remote sync start event");

            // Update state
            this.remoteSyncService.setSyncing(true);

            // Update host being synchronized
            var options = event.data;
            var hostName = options.getName();

            // TODO deprecated
            this.remoteSyncService.setLastHostSynchronizing(hostName);

            // Show notification
            toastr.info("Syncing " + hostName);
        });

        // Setup hook for when remote syncing changes/finishes
        this.syncFinishEvent = this.renderer.listenGlobal("document", "remoteSyncFinish", (event) => {
            console.log("received remote sync finish event");

            var hostName = event.data.getHostName();
            var isSuccess = event.data.isSuccess();
            var isChanges = event.data.isChanges();

            // Switch state to not syncing
            this.remoteSyncService.setSyncing(false);

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
                toastr.error("Failed to synchronize " + hostName);
            }
        });
    }

    ngOnDestroy()
    {
        this.syncStartEvent();
        this.syncFinishEvent();
    }

}