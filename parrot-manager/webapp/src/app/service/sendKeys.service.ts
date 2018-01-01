import { Injectable } from '@angular/core';

@Injectable()
export class SendKeysService
{
    private sendKeysService : any;

    constructor()
    {
        this.sendKeysService = (window as any).sendKeysService;
    }

    send(encryptedValue) : string
    {
        var result = this.sendKeysService.send(encryptedValue);
        return result;
    }

    sendTest(text)
    {
        this.sendKeysService.sendTest(text);
    }

    isQueued(encryptedValue) : boolean
    {
        return this.sendKeysService.isQueued(encryptedValue);
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
