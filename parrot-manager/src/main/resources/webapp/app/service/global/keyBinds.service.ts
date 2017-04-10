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

        // ctrl+s for when database open
        if (isDatabaseOpen && ctrlKey && (key == "s" || key == "83"))
        {
            console.log("key bind for saving database triggered");
            this.databaseService.save();
        }
    }

}
