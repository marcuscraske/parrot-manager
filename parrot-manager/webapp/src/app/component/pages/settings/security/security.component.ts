import { Component, Input } from '@angular/core';

@Component({
    templateUrl: "security.component.html",
    selector: "security"
})
export class SecurityComponent
{
    @Input()
    globalSettingsForm: any;

    constructor() {}

}
