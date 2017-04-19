import { Injectable, Renderer } from '@angular/core';
import { DatabaseService } from 'app/service/database.service'

@Injectable()
export class KeyBindsService {

    keyDownEvent : any;

    constructor(
        private databaseService: DatabaseService,
        private renderer: Renderer
    )
    {
        // Add hook for key down
        this.keyDownEvent= renderer.listenGlobal("window", "keydown", (event) => {
            this.handleKeyDown(event);
        });

        console.log("key events binded");
    }

    handleKeyDown(event)
    {
        var ctrlKey = event.ctrlKey;
        var key = event.key || event.keyCode;
        var isDatabaseOpen = this.databaseService.isOpen();

        // ctrl+s for saving database
        if (isDatabaseOpen && ctrlKey && (key == "s" || key == "115"))
        {
            console.log("key bind for saving database triggered");
            this.databaseService.save();
        }

        // ctrl+o for opening database
        if (ctrlKey && (key == "o" || key == "111"))
        {
            // TODO: somehow call logic in topbar.component.ts; cannot be moved into runtime service due to webkit bug
        }
    }

}
