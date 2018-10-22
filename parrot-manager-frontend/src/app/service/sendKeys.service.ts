import { Injectable } from '@angular/core';

import { EncryptedValue } from "app/model/encryptedValue"

@Injectable()
export class SendKeysService
{
    private sendKeysService : any;

    constructor()
    {
        this.sendKeysService = (window as any).sendKeysService;
    }

    send(nodeId: string, encryptedValue: EncryptedValue) : string
    {
        var encryptedValueId = (encryptedValue != null ? encryptedValue.id : null);
        var result = this.sendKeysService.send(nodeId, encryptedValueId);
        return result;
    }

    sendTest(text)
    {
        this.sendKeysService.sendTest(text);
    }

    isQueued(nodeId: string, encryptedValue: EncryptedValue) : boolean
    {
        var encryptedValueId = (encryptedValue != null ? encryptedValue.id : null);
        return this.sendKeysService.isQueued(nodeId, encryptedValueId);
    }

    reload()
    {
        return this.sendKeysService.reload();
    }

    refreshKeyboardLayout()
    {
        this.sendKeysService.refreshKeyboardLayout();
    }

    getKeyboardLayouts() : any
    {
        return this.sendKeysService.getKeyboardLayouts();
    }

    getKeyboardLayout() : any
    {
        return this.sendKeysService.getKeyboardLayout();
    }

    openLocalUserDirectory()
    {
        this.sendKeysService.openLocalUserDirectory();
    }

    openWorkingDirectory()
    {
        this.sendKeysService.openWorkingDirectory();
    }

}
