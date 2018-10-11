import { Component, Input, EventEmitter, ChangeDetectionStrategy } from '@angular/core';

import { EncryptedValueService } from 'app/service/encryptedValue.service'

import { EncryptedValue } from "app/model/encryptedValue"

@Component({
    selector: 'toggle-value',
    templateUrl: 'toggleValue.component.html',
    styleUrls: ['toggleValue.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ToggleValueComponent
{
    // The maximum length of displayed decrypted values
    private MAX_LENGTH = 32;

    // Current node ID
    @Input() nodeId: string;

    // The encrypted value being displayed
    @Input() encryptedValue? : EncryptedValue;

    // The value currently displayed by this component; toggled between masked chars and the decrypted value
    public displayedValue : string;

    constructor(
        public encryptedValueService: EncryptedValueService
    ) { }

    toggleMask()
    {
        if (this.displayedValue == null)
        {
            var result = this.encryptedValueService.getString(this.nodeId, this.encryptedValue);

            if (result == null)
            {
                result = "(empty)";
            }
            else if (result.length > this.MAX_LENGTH)
            {
                result = result.substring(0, this.MAX_LENGTH) + " ...";
            }

            this.displayedValue = result;
        }
        else
        {
            this.displayedValue = null;
        }
    }

}
