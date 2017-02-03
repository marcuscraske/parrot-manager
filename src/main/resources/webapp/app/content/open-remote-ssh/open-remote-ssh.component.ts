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
        name: [""],
        host: ["localhost", Validators.required],
        port: ["22", Validators.required],
        strictHostChecking : [""],
        user : ["limpygnome", Validators.required],
        userPass : ["test123"],
        remotePath : ["~/git-remote/parrot/test.parrot", Validators.required],
        destinationPath : ["~/sync.parrot", Validators.required],
        privateKey : [""],
        privateKeyPass : [""],
        proxyHost : [""],
        proxyPort : [""],
        proxyType : [""],
        saveAuth : [""],
        promptPass : [""],
        promptKeyPass : [""]
    });

    constructor(private remoteSshFileService: RemoteSshFileService, private databaseService: DatabaseService,
                private router: Router, public fb: FormBuilder) { }

    open(event)
    {
        var form = this.openForm;

        if (form.valid)
        {
            // Set default name if empty
            var name = form.value["name"];

            if (name == null || !name.length)
            {
                console.log("setting default name");
                form.value["name"] = form.value["host"] + ":" + form.value["port"];
            }

            // Build random token for tracking download status
            var randomToken = "not so random";

            // Create download options
            var options = this.remoteSshFileService.createOptions(
                randomToken,
                form.value["name"],
                form.value["host"],
                form.value["port"],
                form.value["user"],
                form.value["remotePath"],
                form.value["destinationPath"]
            );

            options.setStrictHostChecking(form.value["strictHostChecking"]);
            options.setUserPass(form.value["userPass"]);
            options.setPrivateKeyPath(form.value["privateKey"]);
            options.setPrivateKeyPass(form.value["privateKeyPass"]);
            options.setProxyHost(form.value["proxyHost"]);
            options.setProxyPort(form.value["proxyPort"]);
            options.setProxyType(form.value["proxyType"]);
            options.setSaveAuth(form.value["saveAuth"]);
            options.setPromptUserPass(form.value["promptUserPass"]);
            options.setPromptKeyPass(form.value["promptKeyPass"]);

            // Perform download...
            // TODO: consider converting to a promise with a loading box...
            console.log("download options: " + options.toString());
            this.performOpen(options);
        }
        else
        {
            console.log("form is not valid");
        }
    }

    setFormDisabled(isDisabled)
    {
        $("#openRemoteSsh :input").prop("disabled", isDisabled);
    }

    performOpen(options)
    {
        // Disable form
        this.setFormDisabled(true);

        // Begin chain to async prompt for passwords
        this.chainPromptUserPass(options);
    }

    chainPromptUserPass(options)
    {
        if (options.isPromptUserPass())
        {
            bootbox.prompt({
                title: "Enter SSH user password:",
                inputType: "password",
                callback: (password) => {
                    // Update options
                    options.setUserPass(password);

                    // Continue next stage in the chain...
                    console.log("continuing to key pass chain...");
                    this.chainPromptKeyPass(options);
                }
            });
        }
        else
        {
            this.chainPromptKeyPass(options);
        }
    }

    chainPromptKeyPass(options)
    {
        if (options.isPromptUserPass())
        {
            bootbox.prompt({
                title: "Enter key password:",
                inputType: "password",
                callback: (password) => {
                    // Update options
                    options.setKeyPass(password);

                    // Continue next stage in the chain...
                    console.log("continuing to perform actual download and open...");
                    this.performDownloadAndOpen(options);
                }
            });
        }
        else
        {
            this.performDownloadAndOpen(options);
        }
    }

    performDownloadAndOpen(options)
    {
        console.log("going to start download of remote database...");

        // Request download...
        this.errorMessage = this.remoteSshFileService.download(options);

        // Check if download failed...
        if (this.errorMessage != null)
        {
            this.setFormDisabled(false);
            console.log("download failed - " + this.errorMessage);
        }
        else
        {
            console.log("downloaded file, now performing async prompt for database password...");

            // Attempt to open file
            this.databaseService.openWithPrompt(options.getDestinationPath(), (message) => {

                if (message != null)
                {
                    console.log("failed to open database - " + message);
                    this.errorMessage = message;
                }
                else
                {
                    // Persist options
                    console.log("persisting options");
                    options.persist(this.databaseService.getDatabase());

                    // Navigate to viewer
                    console.log("navigating to viewer...");
                    this.router.navigate(["/viewer"]);
                }

            });
        }
    }

}
