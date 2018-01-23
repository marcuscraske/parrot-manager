import { Component, Input } from '@angular/core';

@Component({
    templateUrl: "remoteSync.component.html",
    selector: "remoteSync"
})
export class RemoteSyncComponent
{
    @Input()
    globalSettingsForm: any;

    constructor() {}

}
