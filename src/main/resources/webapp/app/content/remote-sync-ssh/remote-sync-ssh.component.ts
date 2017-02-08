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
       name: [""],
       host: ["", Validators.required],
       port: ["22", Validators.required],
       strictHostChecking : [false],
       user : ["", Validators.required],
       userPass : [""],
       remotePath : ["", Validators.required],
       destinationPath : ["", Validators.required],
       privateKey : [""],
       privateKeyPass : [""],
       proxyHost : [""],
       proxyPort : [""],
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
                //this.populate(passedNode);

                console.log("changed to edit mode - node id: " + this.currentNode);
            }
        });
    }

    // TODO: NOT WORKING...
    ngAfterViewInit()
    {
        this.populate(this.currentNode);
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

            form.value["name"] = node.getName();
            form.value["host"] = config["host"]
            form.value["port"] = config["port"];
            form.value["strictHostChecking"] = config["strictHostChecking"];
            form.value["user"] = config["user"];
            form.value["userPass"] = config["userPass"];
            form.value["remotePath"] = config["remotePath"];
            form.value["destinationPath"] = config["destinationPath"];
            form.value["privateKey"] = config["privateKey"];
            form.value["privateKeyPass"] = config["privateKeyPass"];
            form.value["proxyHost"] = config["proxyHost"];
            form.value["proxyPort"] = config["proxyPort"];
            form.value["proxyType"] = config["proxyType"];
            form.value["promptUserPass"] = config["promptUserPass"];
            form.value["promptKeyPass"] = config["promptKeyPass"];

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
            options.setPromptUserPass(form.value["promptUserPass"]);
            options.setPromptKeyPass(form.value["promptKeyPass"]);

            // Handle options based on mode
            console.log("download options: " + options.toString());

            if (this.currentMode == "open")
            {
                // Perform download and save config...
                this.performOpen(options);
            }
            else if (this.currentMode == "edit")
            {
                // Update existing
            }
            else if (this.currentMode == "new")
            {
                // Test and save config
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
                    console.log("persisting options");
                    options.persist(this.databaseService.getDatabase());

                    // Navigate to viewer
                    console.log("navigating to viewer...");
                    this.router.navigate(["/viewer"]);
                }

            });
        }
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
