import { Component } from '@angular/core';

import { RuntimeService } from 'app/service/runtime.service'
import { BuildInfoService } from 'app/service/buildInfo.service'

@Component({
    moduleId: module.id,
    templateUrl: 'help.component.html',
    styleUrls: ['help.component.css'],
    providers: [BuildInfoService]
})
export class HelpComponent {

    constructor(
        private buildInfoService: BuildInfoService,
        private runtimeService: RuntimeService
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
        this.runtimeService.setClipboard(buildInfo);
    }

}
