import { Component } from '@angular/core';

import { RuntimeService } from 'app/service/runtime.service'
import { ClipboardService } from 'app/service/clipboard.service'
import { BuildInfoService } from 'app/service/buildInfo.service'

@Component({
    templateUrl: 'help.component.html',
    styleUrls: ['help.component.css'],
    providers: [BuildInfoService]
})
export class HelpComponent {

    constructor(
        public buildInfoService: BuildInfoService,
        public runtimeService: RuntimeService,
        public clipboardService: ClipboardService
    ) {
    }

    ngOnInit()
    {
        this.updateBuildInfo();
    }

    updateBuildInfo()
    {
        var field = $("#buildInfo")[0];

        // Update with build info
        var buildInfo = this.buildInfoService.getBuildInfo();
        field.value = buildInfo;

        // Resize box to fit content; reset to avoid inf. growing box
        field.style.height = "0px";
        field.style.height = field.scrollHeight + "px";
    }

    copyBuildInfo()
    {
        var buildInfo = this.buildInfoService.getBuildInfo();
        this.clipboardService.setText(buildInfo);
    }

}
