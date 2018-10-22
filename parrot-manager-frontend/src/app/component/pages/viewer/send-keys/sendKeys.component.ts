import { Component, Input, EventEmitter, ChangeDetectionStrategy, Renderer } from '@angular/core';

import { SendKeysService } from 'app/service/sendKeys.service'

import { EncryptedValue } from "app/model/encryptedValue"

@Component({
    selector: 'send-keys',
    templateUrl: 'sendKeys.component.html',
    styleUrls: ['sendKeys.component.css'],
    providers: [SendKeysService]
})
export class SendKeysComponent
{

    // Current node ID
    @Input() nodeId: string;

    // The encrypted value being displayed
    @Input() encryptedValue? : EncryptedValue;

    // Holds state as to whether this button's value is queued to be sent as keys
    public isQueued : boolean;

    // Holds event for send keys changing
    public sendKeysChangeEvent: Function;

    constructor(
        private sendKeysService: SendKeysService,
        private renderer: Renderer
    ) {
        this.isQueued = false;

        this.sendKeysChangeEvent = this.renderer.listenGlobal("document", "sendKeys.change", (event) => {
            // Check whether still pending when sendKeys service raises change event
            if (this.isQueued)
            {
                var currentEncryptedValueId = (this.encryptedValue != null ? this.encryptedValue.id : null);

                var nodeId = event.data.getNodeId();
                var encryptedValueId = event.data.getEncryptedValueId();
                var queued = event.data.isQueued();

                if (nodeId != this.nodeId || encryptedValueId != currentEncryptedValueId || !queued)
                {
                    this.isQueued = false;
                }
            }
        });
    }

    ngOnDestroy()
    {
        this.sendKeysChangeEvent();
    }

    sendKeys()
    {
        console.log("sending keys...");
        this.sendKeysService.send(this.nodeId, this.encryptedValue);
        toastr.info("Click in a different application to send value as keys...");
        this.isQueued = true;
    }

}
