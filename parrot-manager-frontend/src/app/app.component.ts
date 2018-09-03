import { Component } from '@angular/core';

import { KeyBindsService } from 'app/service/keyBinds.service'
import { InactivityWatcherService } from 'app/service/inactivityWatcher.service'
import { RuntimeService } from 'app/service/runtime.service'
import { ClipboardService } from 'app/service/clipboard.service'
import { DatabaseService } from 'app/service/database.service'
import { BackupService } from 'app/service/backup.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'
import { SettingsService } from 'app/service/settings.service'
import { ThemeService } from 'app/service/theme.service'
import { SendKeysService } from 'app/service/sendKeys.service'
import { SyncSshService } from 'app/service/syncssh.service'
import { SyncService } from 'app/service/sync.service'
import { SyncProfileService } from 'app/service/syncProfile.service'
import { SyncResultService } from 'app/service/syncResult.service'

@Component({
  selector: 'my-app',
  templateUrl: 'app.component.html',
  providers: [
        KeyBindsService,
        InactivityWatcherService,
        RuntimeService,
        ClipboardService,
        DatabaseService,
        BackupService,
        EncryptedValueService,
        SettingsService,
        ThemeService,
        SendKeysService,
        SyncSshService,
        SyncService,
        SyncProfileService,
        SyncResultService
  ]
})
export class AppComponent
{

    constructor(
        private keyBindsService: KeyBindsService,
        private inactivityWatcherService: InactivityWatcherService,
        private themeService: ThemeService,
        private settingsService: SettingsService
    )
    {
        // setup global toastr configuration
        toastr.options = {
          "closeButton": true,
          "debug": false,
          "newestOnTop": false,
          "progressBar": false,
          "positionClass": "toast-top-right",
          "preventDuplicates": false,
          "onclick": null,
          "showDuration": "300",
          "hideDuration": "1000",
          "timeOut": "5000",
          "extendedTimeOut": "1000",
          "showEasing": "swing",
          "hideEasing": "linear",
          "showMethod": "fadeIn",
          "hideMethod": "fadeOut"
        }
    }

    ngOnInit()
    {
        // apply theme
        var theme = this.settingsService.fetch("theme");
        this.themeService.set(theme);
    }


}
