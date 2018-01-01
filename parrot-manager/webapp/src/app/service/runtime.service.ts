import { Injectable } from '@angular/core';

import { EncryptedValueService } from 'app/service/encryptedValue.service'

@Injectable()
export class RuntimeService {

    oldHeight: number;
    runtimeService : any;

    constructor(
        private encryptedValueService: EncryptedValueService,
    )
    {
        // TODO decide whether we're keeping automatic height updating or remove it entirely
        //setInterval( () => { this.updateHeight(); }, 50);
        this.runtimeService = (window as any).runtimeService;
    }

    updateHeight()
    {
        var elementHtml = document.documentElement;
        var elementBody = document.body;

        var newHeight = document.getElementById("app").offsetHeight;

        if (this.oldHeight != newHeight)
        {
            console.log("changing height: " + newHeight);

            this.oldHeight = newHeight;
            this.changeHeight(newHeight);
        }
    }

    changeHeight(newHeight)
    {
        this.runtimeService.changeHeight(newHeight);
    }

    pickFile(title, initialPath, isSave) : string
    {
        var path = this.runtimeService.pickFile(title, initialPath, isSave);
        return path;
    }

    exit()
    {
         this.runtimeService.exit();
    }

    setClipboard(value)
    {
        // Set clipboard
        this.runtimeService.setClipboard(value);

        // Show notification
        toastr.info("Copied to clipboard");
    }

    isDevelopmentMode() : boolean
    {
        return this.runtimeService.isDevelopmentMode();
    }

    refreshPage(relativeCurrentUrl)
    {
        this.runtimeService.loadPage("http://localhost" + relativeCurrentUrl);
    }

    openLink(url)
    {
        this.runtimeService.openLink(url);
    }

}
