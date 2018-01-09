import { Component } from '@angular/core';

import { ImportExportService } from 'app/service/importExport.service'
import { RuntimeService } from 'app/service/runtime.service'

@Component({
    templateUrl: 'home.component.html',
    styleUrls: ['home.component.css'],
    providers: [ImportExportService]
})
export class HomeComponent {

    // state/flags
    public importText: boolean;

    // result
    public success: string;
    public error: string;
    public exportText: string;

    constructor(
        private importExportService: ImportExportService,
        private runtimeService: RuntimeService
    ) { }

    chooseImportText()
    {
        this.reset();
        this.importText = true;

        var text = $("#importText").val();

        if (format.length > 0 && text.length > 0)
        {
            var options = this.createOptions();
            var result = this.importExportService.databaseImportText(options, text);
            this.isSuccess(result);
        }
        else
        {
            this.errorSelectFormat();
        }
    }

    chooseImportFile()
    {
        this.reset();
    }

    chooseExportText()
    {
        this.reset();

        if (format != null)
        {
            var options = this.createOptions();
            var result = this.importExportService.databaseExportText(options);

            if (this.isSuccess(result))
            {
                this.exportText = result.getText();
            }
        }
        else
        {
            this.errorSelectFormat();
        }
    }

    chooseExportFile()
    {
        this.reset();
        // show dialogue
    }

    copyText()
    {
        this.runtimeService.setClipboard(this.text);
    }


    private createOptions()
    {
        var format = $("#format").val();
        var remoteSync = $("#remoteSync").val();

        var options = this.importExportService.createOptions(format, remoteSync);
        return options;
    }

    private errorSelectFormat()
    {
        this.error = "You need to select a format";
    }

    private isSuccess(result) : boolean
    {
        var error = result.getError();
        this.error = error;
        return error == null;
    }

    private reset()
    {
        this.importText = false;

        this.exportText = null;
        this.error = null;
        this.success = null;
    }

}
