import { Component, Input, EventEmitter, ChangeDetectionStrategy } from '@angular/core';

import { SendKeysService } from 'app/service/sendKeys.service'

@Component({
    moduleId: module.id,
    selector: 'send-keys',
    templateUrl: 'sendKeys.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [SendKeysService]
})
export class SendKeysComponent
{

    // The encrypted value being displayed
    @Input() encryptedValue : any;

    constructor(
        private sendKeysService: SendKeysService
    ) { }

    sendKeys()
    {
        console.log("sending keys...");
        this.sendKeysService.send(this.encryptedValue);
    }

}
