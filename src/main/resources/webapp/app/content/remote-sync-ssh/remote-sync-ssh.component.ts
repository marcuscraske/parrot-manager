import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { RemoteSshFileService } from 'app/service/remoteSshFileService.service'
import { DatabaseService } from 'app/service/database.service'
import { Router, ActivatedRoute } from '@angular/router';

@Component({
    moduleId: module.id,
    templateUrl: 'remote-sync-ssh.component.html',
    providers: [RemoteSshFileService, DatabaseService]
})
export class RemoteSyncSshComponent {

    public openForm = this.fb.group({
       name: ["", Validators.required],
       host: ["", Validators.required],
       port: [22, Validators.required],
       strictHostChecking : [false],
       user : ["", Validators.required],
       userPass : [""],
       remotePath : ["", Validators.required],
       destinationPath : [""],                      // Validator is dynamic based on mode (required only for open)
       privateKey : [""],
       privateKeyPass : [""],
       proxyHost : [""],
       proxyPort : [0],
       proxyType : ["None"],
       promptUserPass : [false],
       promptKeyPass : [false]
    });

    errorMessage : string;

    // Observable subscription for params
    subParams : any;

    // The mode: open, new, edit
    currentMode : string;

    // The ID (key stored under remote-sync) of the current node being changed; passed by routing config
    currentNode : string;

    constructor(private remoteSshFileService: RemoteSshFileService, private databaseService: DatabaseService,
                private router: Router, public fb: FormBuilder, private route: ActivatedRoute) { }

    ngOnInit()
    {
        this.subParams = this.route.params.subscribe(params => {

            var passedNode = params['currentNode'];

            // Determine appropriate mode and if we need to populate form with existing data
            if (passedNode == null)
            {
                this.currentNode = null;
                this.currentMode = "open";
                this.openForm.controls["destinationPath"].setValidators(Validators.required);
            }
            else if (passedNode == "new")
            {
                this.currentNode = null;
                this.currentMode = "new";
                console.log("changed to new mode");
            }
            else
            {
                this.currentNode = passedNode;
                this.currentMode = "edit";
                this.populate(passedNode);

                console.log("changed to edit mode - node id: " + this.currentNode);
            }
        });
    }

    ngOnDestroy()
    {
        this.subParams.unsubscribe();
    }

    /* Populates form using a specific node of remote-sync. */
    populate(nodeId)
    {
        // Fetch actual JSON for node
        var node = this.databaseService.getNode(nodeId);
        var json = node != null ? node.getDecryptedValueString() : null;
        var config = json != null ? JSON.parse(json) : null;

        if (config != null)
        {
            // Populate form with data
            var form = this.openForm;
            form.patchValue(config);
            console.log("form populated - node id: " + nodeId);
        }
        else
        {
            console.log("no config found - node id: " + nodeId);
        }
    }

    open(event)
    {
        var form = this.openForm;

        console.log("#### " + form.controls["port"].valid + " ### " + form.value["proxyPort"]);

        if (form.valid)
        {
            // Create download options
            var options = this.createOptions();

            if (this.currentMode == "open")
            {
                // Perform download and save config...
                this.performOpen(options);
            }
            else if (this.currentMode == "edit" || this.currentMode == "new")
            {
                // Update existing
                this.persistOptions(options);
                this.router.navigate(["/remote-sync"]);
            }
            else
            {
                console.log("unhandled mode");
            }
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
                    options.setPrivateKeyPass(password);

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
                    this.setFormDisabled(false);
                }
                else
                {
                    // Persist options
                    this.persistOptions(options);

                    // Navigate to viewer
                    console.log("navigating to viewer...");
                    this.router.navigate(["/viewer"]);
                }

            });
        }
    }

    /* Converts the current form into SshOptions instance */
    createOptions() : any
    {
        var form = this.openForm;

        // Set default name if empty
        var name = form.value["name"];

        if (name == null || !name.length)
        {
            console.log("setting default name");
            form.value["name"] = form.value["host"] + ":" + form.value["port"];
        }

        // Build random token for tracking download status
        // -- This is just left for the future, in case we want async downloading / progress bar
        var randomToken = "not so random";

        // Create actual instance
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
        options.setPromptUserPass(form.value["promptUserPass"]);
        options.setPromptKeyPass(form.value["promptKeyPass"]);

        // Handle options based on mode
        console.log("download options: " + options.toString());

        return options;
    }

    /* Saves currently configuration to database */
    persistOptions(options)
    {
        if (this.currentMode == "edit")
        {
            var node = this.databaseService.getNode(this.currentNode);
            node.remove();
            console.log("dropped existing options node - id: " + this.currentNode);
        }

        console.log("persisting options to new node");
        options.persist(this.databaseService.getDatabase());
    }

    /* Handler for cancel button (navigates to appropriate previous page in flow */
    actionCancel()
    {
        if (this.currentMode == "open")
        {
            this.router.navigate(["/open"]);
        }
        else
        {
            this.router.navigate(["/remote-sync"]);
        }
    }

}
