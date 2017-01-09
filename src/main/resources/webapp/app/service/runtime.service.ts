import { Injectable } from '@angular/core';

@Injectable()
export class RuntimeService {

    oldHeight: number;

    constructor() {
        setInterval( () => { this.updateHeight(); }, 50);
    }



    updateHeight() : void {

        var elementHtml = document.documentElement;
        var elementBody = document.body;

        var newHeight = document.getElementById("app").scrollHeight;

        if (this.oldHeight != newHeight)
        {
            this.oldHeight = newHeight;
            this.changeHeight(newHeight);
        }
    }
    changeHeight(newHeight) : void {
         (window as any).runtimeService.changeHeight(newHeight);
     }

    exit() : void {
         (window as any).runtimeService.exit();
    }

}
