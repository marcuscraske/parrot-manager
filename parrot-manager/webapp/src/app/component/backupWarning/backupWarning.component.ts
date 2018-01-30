import { Component, Renderer } from '@angular/core';

import { RuntimeService } from 'app/service/runtime.service'
import { DatabaseService } from 'app/service/database.service'
import { RemoteSyncService } from 'app/service/remoteSyncService.service'
import { BackupService } from 'app/service/backup.service'

@Component({
  selector: 'backupWarning',
  templateUrl: 'backupWarning.component.html',
  styleUrls: ['backupWarning.component.css']
})
export class BackupWarningComponent
{
    constructor(
        public backupService: BackupService
    ) { }

}
