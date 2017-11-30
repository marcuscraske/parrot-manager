import { Injectable, Renderer } from '@angular/core';
import { Router } from '@angular/router';

import { DatabaseService } from 'app/service/database.service'
import { RemoteSyncChangeLogService } from 'app/service/remoteSyncChangeLog.service'

@Injectable()
export class InactivityWatcherService
{
    private mouseMoveEvent: any = null;
    private keyDownEvent: any = null;

    private timeout: int = 0;

    private inactivityTimeoutHandle: any = null;

    constructor(
        private databaseService: DatabaseService,
        private renderer: Renderer
    ) {
        // setup listeners
        this.mouseMoveEvent = renderer.listenGlobal("window", "mouseover", (event) => {
            this.reset();
        });
        this.keyDownEvent = renderer.listenGlobal("window", "keydown", (event) => {
            this.reset();
        });

        // read timeout setting
        // TODO timeout setting
        this.timeout = 5000;
    }

    reset()
    {
        // reset old timeout
        if (this.inactivityTimeoutHandle != null)
        {
            clearTimeout(this.inactivityTimeoutHandle);
        }

        // setup new timeout
        if (this.timeout > 0)
        {
            this.inactivityTimeoutHandle = setTimeout(() => {
                this.trigger();
            }, this.timeout);
        }
    }

    trigger()
    {
        if (this.databaseService.isOpen())
        {
            console.log("inactivity timeout occurred, closing database");
            this.databaseService.close();
        }
    }

}