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

        var newHeight = document.getElementById("app").offsetHeight;

        if (this.oldHeight != newHeight)
        {
            console.log("changing height: " + newHeight);

            this.oldHeight = newHeight;
            this.changeHeight(newHeight);

            console.log("invoked");

        }
    }

    changeHeight(newHeight) : void {
         (window as any).runtimeService.changeHeight(newHeight);
     }

    openFile(initialPath) : string {
        var path = (window as any).runtimeService.openFile(initialPath);
        return path;
    }

    exit() : void {
         (window as any).runtimeService.exit();
    }

}
