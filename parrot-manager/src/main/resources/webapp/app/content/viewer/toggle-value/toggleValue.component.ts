import { Component, Input, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { EncryptedValueService } from 'app/service/encryptedValue.service'

@Component({
    moduleId: module.id,
    selector: 'toggle-value',
    templateUrl: 'toggleValue.component.html',
    styleUrls: ['toggleValue.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ToggleValueComponent
{
    // The maximum length of displayed decrypted values
    private MAX_LENGTH = 32 : int;

    // The encrypted value being displayed
    @Input() encryptedValue : any;

    // The value currently displayed by this component; toggled between masked chars and the decrypted value
    private displayedValue : string;

    constructor(
        private encryptedValueService: EncryptedValueService
    ) { }

    toggleMask()
    {
        if (this.displayedValue == null)
        {
            var result = this.encryptedValueService.getStringFromValue(this.encryptedValue);

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
