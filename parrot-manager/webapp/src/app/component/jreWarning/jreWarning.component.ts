import { Component } from '@angular/core';

import { SettingsService } from 'app/service/settings.service'
import { BuildInfoService } from 'app/service/buildInfo.service'
import { BrowserService } from 'app/service/browser.service'

@Component({
  selector: 'jreWarning',
  templateUrl: 'jreWarning.component.html',
  providers: [BrowserService, BuildInfoService]
})
export class JreWarningComponent
{
    constructor(
        public settingsService: SettingsService,
        public buildInfoService: BuildInfoService,
        public browserService: BrowserService
    ) { }

    ignore()
    {
        this.settingsService.setBoolean("ignoreJavaVersion", true);
    }

    isIgnored()
    {
        return this.settingsService.fetch("ignoreJavaVersion") == true;
    }

}
