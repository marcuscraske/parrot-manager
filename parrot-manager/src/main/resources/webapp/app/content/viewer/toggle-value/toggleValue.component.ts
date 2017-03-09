import { Component, Input, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { EncryptedValueService } from 'app/service/encryptedValue.service'

@Component({
    moduleId: module.id,
    selector: 'toggle-value',
    templateUrl: 'toggleValue.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ToggleValueComponent
{

    // The encrypted value being displayed
    @Input() value : any;

    // The value currently displayed by this component; toggled between masked chars and the decrypted value
    private displayedValue : string;

    constructor(
        private encryptedValueService: EncryptedValueService
    ) { }

    toggleMask()
    {
        if (this.displayedValue == null)
        {
            var result = this.encryptedValueService.getStringFromValue(this.value);

            if (result == null)
            {
                result = "(empty)";
            }

            this.displayedValue = result;
        }
        else
        {
            this.displayedValue = null;
        }
    }

}
