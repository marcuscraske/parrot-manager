import { Component, Input } from '@angular/core';

@Component({
    templateUrl: "backups.component.html",
    selector: "backups"
})
export class BackupsComponent
{
    @Input()
    globalSettingsForm: any;

    constructor() {}

}
