import { Component, Input } from '@angular/core';
import { SyncResult } from 'app/model/syncResult'

@Component({
    selector: 'syncResultErrors',
    templateUrl: 'syncResultErrors.component.html'
})
export class SyncResultErrorsComponent
{
    @Input()
    syncResults : SyncResult[]

    trackSyncResults(index, syncResult)
    {
        return syncResult ? syncResult.hostName : null;
    }

}
