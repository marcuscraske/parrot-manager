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
    public messages: string[];

    constructor(
        private importExportService: ImportExportService,
        private runtimeService: RuntimeService
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
                this.messages = result.getMessages();
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
            this.messages = result.getMessages();
        }
    }

    chooseExportText()
    {
        this.reset();

        var options = this.createOptions();
        var result = this.importExportService.databaseExportText(options);

        if (this.isSuccess(result))
        {
            this.exportText = result.getText();
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
            this.messages = result.getMessages();
        }
    }

    copyText()
    {
        this.runtimeService.setClipboard(this.exportText);
    }


    private createOptions()
    {
        var format = $("#format").val();
        var remoteSync = $("#remoteSync").val();

        var options = this.importExportService.createOptions(format, remoteSync);
        return options;
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
        this.messages = null;
    }

}
