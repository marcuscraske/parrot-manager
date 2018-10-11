import { Component, Input, EventEmitter, ChangeDetectionStrategy } from '@angular/core';

import { RuntimeService } from 'app/service/runtime.service'
import { ClipboardService } from 'app/service/clipboard.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'

import { EncryptedValue } from "app/model/encryptedValue"

@Component({
    selector: 'copy-clipboard',
    templateUrl: 'copyClipboard.component.html',
    styleUrls: ['copyClipboard.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CopyClipboardComponent
{
    // Current node ID
    @Input() nodeId: string;

    // The encrypted value being displayed
    @Input() encryptedValue? : EncryptedValue;

    constructor(
        private runtimeService: RuntimeService,
        private clipboardService: ClipboardService,
        private encryptedValueService: EncryptedValueService
    ) { }

    copyToClipboard(event)
    {
        console.log("decrypting value for clipboard");

        var decryptedValue = this.encryptedValueService.getString(this.nodeId, this.encryptedValue);

        if (decryptedValue == null)
        {
            decryptedValue = "";
            console.log("setting clipboard to empty, as decrypted value is not set");
        }

        this.clipboardService.setText(decryptedValue);
        console.log("clipboard updated with decrypted value");

        // Add rainbow effect to button
        var button = $(event.target);
        button.addClass("copiedAnimation");

        setTimeout(() => {
            button.removeClass("copiedAnimation");
        }, 500);
    }

}
