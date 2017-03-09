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
        setInterval( () => { this.updateHeight(); }, 50);
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
        this.runtimeService.setClipboard(value);
    }

    copyNodeValueToClipboard(node)
    {
        // Update clipboard
        if (node != null)
        {
            var nodeId = node.getId();
            var encryptedValue = node.getValue();

            console.log("copying value to clipboard - node id: " + nodeId);

            this.copyEncryptedValueToClipboard(encryptedValue);
        }
        else
        {
            console.log("unable to copy to clipboard - null node passed");
        }
    }

    copyEncryptedValueToClipboard(encryptedValue)
    {
        var decryptedValue = this.encryptedValueService.getStringFromValue(encryptedValue);

        if (decryptedValue != null)
        {
            this.runtimeService.setClipboard(decryptedValue);
            console.log("updated clipboard with encrypted value - length: " + decryptedValue.length);
        }
        else
        {
            console.log("skipped copying encrypted value to clipboard - empty/null");
        }
    }

    isDevelopmentMode() : boolean
    {
        return this.runtimeService.isDevelopmentMode();
    }

    refreshPage(relativeCurrentUrl)
    {
        this.runtimeService.loadPage("http://localhost" + relativeCurrentUrl);
    }

}
