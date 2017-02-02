import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { RemoteSshFileService } from 'app/service/remoteSshFileService.service'
import { DatabaseService } from 'app/service/database.service'
import { Router } from '@angular/router';

@Component({
    moduleId: module.id,
    templateUrl: 'open-remote-ssh.component.html',
    providers: [RemoteSshFileService, DatabaseService]
})
export class OpenRemoteSshComponent {

    errorMessage : string;

    public openForm = this.fb.group({
        host: ["localhost", Validators.required],
        port: ["22", Validators.required],
        strictHostChecking : [""],
        user : ["limpygnome", Validators.required],
        pass : ["test123"],
        remotePath : ["~/git-remote/parrot/test.parrot", Validators.required],
        destinationPath : ["~/sync.parrot", Validators.required],
        privateKey : [""],
        privateKeyPass : [""],
        proxyHost : [""],
        proxyPort : [""],
        proxyType : [""],
        saveAuth : [""]
    });

    constructor(private remoteSshFileService: RemoteSshFileService, private databaseService: DatabaseService,
                private router: Router, public fb: FormBuilder) { }

    open(event)
    {
        var form = this.openForm;

        // Build random token for tracking download status
        var randomToken = "not so random";

        // Create download options
        var options = this.remoteSshFileService.createDownloadOptions(
            randomToken,
            form.value["host"],
            form.value["port"],
            form.value["user"],
            form.value["remotePath"],
            form.value["destinationPath"]
        );

        options.setPass(form.value["pass"]);


        // Perform download...
        console.log("download options: " + options.toString());

        this.performOpen(options);
    }

    setFormDisabled(isDisabled)
    {
        $("#openRemoteSsh :input").prop("disabled", isDisabled);
    }

    performOpen(options)
    {
        // Disable form
        this.setFormDisabled(true);

        // Request download in promise
        this.errorMessage = this.remoteSshFileService.download(options);

        // Check if download failed...
        if (this.errorMessage != null)
        {
            this.setFormDisabled(false);
        }
        else
        {
            // Attempt to open file
            this.databaseService.openWithPrompt(options.getDestinationPath(), (message) => {

                if (message != null)
                {
                    this.errorMessage = message;
                }
                else
                {
                    this.router.navigate(["/viewer"]);
                }

            });
        }
    }

}
