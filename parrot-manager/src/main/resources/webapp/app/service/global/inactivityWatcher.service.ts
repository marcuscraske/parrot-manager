import { Injectable, Renderer } from '@angular/core';
import { Router } from '@angular/router';

import { DatabaseService } from 'app/service/database.service'
import { SettingsService } from 'app/service/settings.service'

@Injectable()
export class InactivityWatcherService
{
    private mouseMoveEvent: any = null;
    private keyDownEvent: any = null;

    private inactivityTimeoutHandle: any = null;

    constructor(
        private databaseService: DatabaseService,
        private settingsService: SettingsService,
        private renderer: Renderer,
        private router: Router
    ) {
        // setup listeners
        this.mouseMoveEvent = renderer.listenGlobal("window", "mouseover", (event) => {
            this.reset();
        });
        this.keyDownEvent = renderer.listenGlobal("window", "keydown", (event) => {
            this.reset();
        });
    }

    reset()
    {
        // reset old timeout
        if (this.inactivityTimeoutHandle != null)
        {
            clearTimeout(this.inactivityTimeoutHandle);
            console.debug("inactivity timer reset");
        }

        // setup new timeout
        var timeout = this.settingsService.fetch("inactivityTimeout");

        if (timeout != null && timeout > 0)
        {
            // convert back to milliseconds
            timeout = timeout * 60 * 1000;

            // setup timeout
            this.inactivityTimeoutHandle = setTimeout(() => {
                this.trigger();
            }, timeout);
        }
    }

    trigger()
    {
        console.debug("inactivity timer triggered");

        if (this.databaseService.isOpen())
        {
            console.log("inactivity timeout occurred, closing database");
            this.databaseService.close();

            // navigate to open page
            this.router.navigate(["/open"]);
        }
    }

}