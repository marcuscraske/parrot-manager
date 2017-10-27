import { Component } from '@angular/core';

import { KeyBindsService } from 'app/service/global/keyBinds.service'
import { RuntimeService } from 'app/service/runtime.service'
import { DatabaseService } from 'app/service/database.service'
import { BackupService } from 'app/service/backup.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'
import { RemoteSshFileService } from 'app/service/remoteSshFileService.service'
import { RemoteSyncChangeLogService } from 'app/service/remoteSyncChangeLog.service'
import { SettingsService } from 'app/service/settings.service'
import { ThemeService } from 'app/service/theme.service'

@Component({
  selector: 'my-app',
  templateUrl: '/app/app.component.html',
  providers: [
        KeyBindsService,
        RuntimeService,
        DatabaseService,
        BackupService,
        EncryptedValueService,
        RemoteSshFileService,
        RemoteSyncChangeLogService,
        SettingsService,
        ThemeService
  ]
})
export class AppComponent
{

    constructor(
        keyBindsService: KeyBindsService,
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
