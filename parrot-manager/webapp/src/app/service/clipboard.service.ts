import { Injectable } from '@angular/core';

@Injectable()
export class ClipboardService
{
    clipboardService: any;

    constructor()
    {
        this.clipboardService = (window as any).clipboardService;
    }

    setText(value : string)
    {
        // Set clipboard
        this.clipboardService.setText(value);

        // Show notification
        toastr.info("Copied to clipboard");
    }

}
