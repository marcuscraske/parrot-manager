import { Injectable } from '@angular/core';

@Injectable()
export class SendKeysService {

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

}
