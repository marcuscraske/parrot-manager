import { Component } from '@angular/core';

@Component({
    templateUrl: 'home.component.html'
})
export class HomeComponent {

    // options
    public importText: boolean;
    public success: string;
    public error: string;

    // result
    public exportText: string;

    constructor() { }

    chooseImportText()
    {
        this.reset();
        this.importText = true;

        var format = this.getFormat();
        var text = $("#importText").val();

        if (format.length > 0 && text.length > 0)
        {
        }
    }

    chooseImportFile()
    {
        this.reset();
    }

    chooseExportText()
    {
        this.reset();
        var format = this.getFormat();

        if (format != null)
        {
        }
    }

    chooseExportFile()
    {
        this.reset();
        // show dialogue
    }


    private getFormat() : string
    {
        var format = $("#format").val();
        return format;
    }

    private reset()
    {
        this.importText = false;
    }

}
