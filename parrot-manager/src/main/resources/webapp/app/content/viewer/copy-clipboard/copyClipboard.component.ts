import { Component, Input, EventEmitter, ChangeDetectionStrategy } from '@angular/core';

import { RuntimeService } from 'app/service/runtime.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'

@Component({
    moduleId: module.id,
    selector: 'copy-clipboard',
    templateUrl: 'copyClipboard.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CopyClipboardComponent
{

    // The encrypted value being displayed
    @Input() encryptedValue : any;

    constructor(
        private runtimeService: RuntimeService,
        private encryptedValueService: EncryptedValueService
    ) { }

    copyToClipboard()
    {
        console.log("decrypting value for clipboard");

        var decryptedValue = this.encryptedValueService.getStringFromValue(this.encryptedValue);

        if (decryptedValue == null)
        {
            decryptedValue = "";
            console.log("setting clipboard to empty, as decrypted value is not set");
        }

        this.runtimeService.setClipboard(decryptedValue);
        console.log("clipboard updated with decrypted value");
    }

}
