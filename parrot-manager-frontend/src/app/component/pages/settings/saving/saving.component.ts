import { Component, Input } from '@angular/core';

@Component({
    templateUrl: "saving.component.html",
    selector: "saving"
})
export class SavingComponent
{
    @Input()
    globalSettingsForm: any;

    constructor() {}

}
