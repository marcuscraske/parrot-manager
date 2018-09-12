import { Component } from '@angular/core';

import { ImportExportService } from 'app/service/importExport.service'
import { RuntimeService } from 'app/service/runtime.service'
import { ClipboardService } from 'app/service/clipboard.service'

@Component({
    templateUrl: 'home.component.html',
    styleUrls: ['home.component.css'],
    providers: [ImportExportService]
})
export class HomeComponent {

    // state/flags
    public importText: boolean;
    public format: string = "json";

    // result
    public success: string;
    public error: string;
    public exportText: string;
    public mergeLog: any;

    constructor(
        private importExportService: ImportExportService,
        private runtimeService: RuntimeService,
        private clipboardService: ClipboardService
    ) { }

    chooseImportText()
    {
        var importText = this.importText;
        this.reset();

        if (importText)
        {
            var text = $("#importText").val();

            if (text.length > 0)
            {
                var options = this.createOptions();
                var result = this.importExportService.databaseImportText(options, text);
                if (this.isSuccess(result))
                {
                    this.success = "Imported database changes successfully";
                }

                this.mergeLog = result.mergeLog;
            }
            else
            {
                this.error = "Enter some text to be imported";
            }
        }
        else
        {
            // Show text area first
            this.importText = true;
        }
    }

    chooseImportFile()
    {
        this.reset();
        var path = this.runtimeService.pickFile("Select file to import", null, false);

        if (path != null)
        {
            var options = this.createOptions();
            var result = this.importExportService.databaseImportFile(options, path);
            if (this.isSuccess(result))
            {
                this.success = "Imported database changes successfully";
            }
            this.mergeLog = result.mergeLog;
        }
    }

    chooseExportText()
    {
        this.reset();

        var options = this.createOptions();
        var result = this.importExportService.databaseExportText(options);

        if (this.isSuccess(result))
        {
            this.exportText = result.text;
        }
    }

    chooseExportFile()
    {
        this.reset();
        var path = this.runtimeService.pickFile("Select file to export", null, true);

        if (path != null)
        {
            var options = this.createOptions();
            var result = this.importExportService.databaseExportFile(options, path);

            if (this.isSuccess(result))
            {
                this.success = "Exported database successfully";
            }
        }
    }

    copyText()
    {
        this.clipboardService.setText(this.exportText);
    }

    private createOptions()
    {
        var remoteSync = $("#remoteSync").val();

        var options = this.importExportService.createOptions(this.format, remoteSync);
        return options;
    }

    private isSuccess(result) : boolean
    {
        var error = result.error;
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
