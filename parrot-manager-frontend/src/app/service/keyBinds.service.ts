import { Injectable, Renderer } from '@angular/core';
import { Router } from '@angular/router';

import { DatabaseService } from 'app/service/database.service'

@Injectable()
export class KeyBindsService
{
    keyDownEvent : any;

    constructor(
        private databaseService: DatabaseService,
        private renderer: Renderer,
        private router: Router
    ) {
        // Add hook for key down
        this.keyDownEvent = renderer.listenGlobal("window", "keydown", (event) => {
            this.handleKeyDown(event);
        });

        console.log("key events binded");
    }

    handleKeyDown(event)
    {
        var ctrlKey = event.ctrlKey;

        if (ctrlKey)
        {
            var key = event.key || event.which || event.keyCode;
            var isDatabaseOpen = this.databaseService.isOpen();

            // ctrl+s for saving database
            if (isDatabaseOpen && (key == "83" || key == "115"))
            {
                console.log("key bind for saving database triggered");
                this.databaseService.save();
            }

            // ctrl+o / ctrl+l for opening / locking database
            if ((key == "o" || key == "79") || (key == "l" || key == "76"))
            {
                this.databaseService.close();

                console.log("redirecting to open page...");
                this.router.navigate(["/open"]);
            }
        }
    }

}
