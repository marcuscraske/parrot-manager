import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

import { RuntimeService } from 'app/service/runtime.service'
import { DatabaseService } from 'app/service/database.service'
import { SyncService } from 'app/service/sync.service'
import { SyncSshService } from 'app/service/syncSsh.service'
import { SyncProfileService } from 'app/service/syncProfile.service'

@Component({
    templateUrl: 'sync-ssh.component.html'
})
export class SyncSshComponent {

    public openForm = this.fb.group({
       name: ["", [Validators.required, Validators.minLength(1), Validators.maxLength(128)]],
       host: ["", [Validators.required, Validators.minLength(1), Validators.maxLength(128)]],
       port: [22, [Validators.required, Validators.min(1), Validators.max(65535)]],
       strictHostChecking : [false],
       user : ["", [Validators.required, Validators.minLength(1), Validators.maxLength(128)]],
       userPass : [""],
       remotePath : ["", Validators.required],
       destinationPath : [""],                      // Validator is dynamic based on mode (required only for open)
       privateKeyPath : ["~/.ssh/id_rsa"],
       privateKeyPass : [""],
       proxyHost : ["", [Validators.minLength(1), Validators.maxLength(128)]],
       proxyPort : ["", [Validators.min(1), Validators.max(65535)]],
       proxyType : ["None"],
       promptUserPass : [false],
       promptKeyPass : [false],
       machineFilter: [""]
    });

    // Messages displayed to user; hidden when null
    errorMessage : string;
    successMessage : string;

    // Observable subscription for params
    subParams : any;

    // The mode: open, new, edit
    currentMode : string;

    // The ID (key stored under remote-sync) of the current node being changed; passed by routing config
    currentNode : string;

    constructor(
        private runtimeService: RuntimeService,
        private databaseService: DatabaseService,
        private syncService: SyncService,
        private syncSshService: SyncSshService,
        private syncProfileService: SyncProfileService,
        private router: Router,
        public fb: FormBuilder,
        private route: ActivatedRoute
    ) { }

    ngOnInit()
    {
        this.subParams = this.route.params.subscribe(params => {

            var passedNode = params['currentNode'];
            var populateMachineName = false;

            // determine appropriate mode and if we need to populate form with existing data
            if (passedNode == null)
            {
                this.currentNode = null;
                this.currentMode = "open";
                populateMachineName = true;

                this.openForm.controls["destinationPath"].setValidators(Validators.required);
            }
            else if (passedNode == "new")
            {
                this.currentNode = null;
                this.currentMode = "new";
                populateMachineName = true;

                console.log("changed to new mode");
            }
            else
            {
                this.currentNode = passedNode;
                this.currentMode = "edit";
                this.populate(passedNode);

                console.log("changed to edit mode - node id: " + this.currentNode);
            }

            // populate machine name
            if (populateMachineName)
            {
                // populate machine filter with current hostname
                var currentHostname = this.syncService.getCurrentHostName();
                this.openForm.patchValue({
                    "name" : currentHostname,
                    "machineFilter" : currentHostname
                });
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
        // Fetch profile and populate form
        var profile = this.syncProfileService.fetchById(nodeId);

        if (profile != null)
        {
            // Populate form with data
            var form = this.openForm;
            form.patchValue(profile);
            console.log("form populated - node id: " + nodeId);
        }
        else
        {
            console.log("no profile found - node id: " + nodeId);
        }
    }

    persist()
    {
        var form = this.openForm;

        if (form.valid)
        {
            // Create download options
            var options = this.createOptions();
            var profile = this.createProfile();

            if (this.currentMode == "open")
            {
                // Perform download and save config...
                this.performOpen(options, profile);
            }
            else if (this.currentMode == "edit" || this.currentMode == "new")
            {
                // Update existing
                this.syncProfileService.save(profile);
                this.router.navigate(["/sync"]);
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

    performOpen(options, profile)
    {
        // Begin async chain to prompt for passwords etc
        this.sshAuthChain(options, profile, (options, profile) => { this.performDownloadAndOpen(options, profile) });
    }

    performTest()
    {
        if (this.openForm.valid)
        {
            console.log("testing...");

            // Create download options
            var options = this.createOptions();
            var profile = this.createProfile();

            // Begin async chain to prompt for passwords etc
            this.sshAuthChain(options, profile, (options, profile) => { this.performTestWithAuth(options, profile); });
        }
        else
        {
            console.log("not testing, form is invalid");
        }
    }

    sshAuthChain(options, profile, callback)
    {
        console.log("disabling form, wiping messages...");

        // Wipe existing messages
        this.errorMessage = null;
        this.successMessage = null;

        // Disable form
        this.setFormDisabled(true);

        this.syncSshService.authChain(options, profile, (options, profile) => {
            // Re-enable form
            this.setFormDisabled(false);
            callback(options, profile);
        });
    }

    performDownloadAndOpen(options, profile)
    {
        console.log("going to start download of remote database...");

        // Request download...
        this.errorMessage = this.syncService.download(options, profile);

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
                    // Persist profile
                    this.syncProfileService.save(profile);

                    // Navigate to viewer
                    console.log("navigating to viewer...");
                    this.router.navigate(["/viewer"]);
                }

            });
        }
    }

    performTestWithAuth(options, profile)
    {
        console.log("going to test options...");

        // Invoke and assign error message (successful if null)
        this.errorMessage = this.syncService.test(options, profile);

        if (this.errorMessage == null)
        {
            this.successMessage = "Working!";
        }

        // Re-enable form
        this.setFormDisabled(false);
    }

    /* Converts the current form into SyncProfile instance */
    private createProfile() : any
    {
        var form = this.openForm;

        // Set default name if empty
        var name = form.value["name"];

        if (name == null || !name.length)
        {
            console.log("setting default name");
            form.value["name"] = form.value["host"] + ":" + form.value["port"];
        }

        // Create actual instance
        var profile = this.syncProfileService.createTemporaryProfile("ssh");

        profile.setName(form.value["name"]);
        profile.setHost(form.value["host"]);
        profile.setPort(form.value["port"]);
        profile.setUser(form.value["user"]);
        profile.setRemotePath(form.value["remotePath"]);

        profile.setStrictHostChecking(form.value["strictHostChecking"]);
        profile.setUserPass(form.value["userPass"]);
        profile.setPrivateKeyPath(form.value["privateKeyPath"]);
        profile.setPrivateKeyPass(form.value["privateKeyPass"]);
        profile.setProxyHost(form.value["proxyHost"]);
        profile.setProxyPort(form.value["proxyPort"]);
        profile.setProxyType(form.value["proxyType"]);
        profile.setPromptUserPass(form.value["promptUserPass"]);
        profile.setPromptKeyPass(form.value["promptKeyPass"]);
        profile.setMachineFilter(form.value["machineFilter"]);

        // Handle options based on mode
        console.log("download options: " + profile.toString());

        return profile;
    }

    /* Converts currnet form into SyncOptions instance */
    private createOptions()
    {
        var form = this.openForm;
        var options = this.syncService.createTemporaryOptions();

        if (this.currentMode == "open")
        {
            options.setDestinationPath(form.value["destinationPath"]);
        }

        return options;
    }

    /* Handler for cancel button (navigates to appropriate previous page in flow */
    actionCancel()
    {
        if (this.openForm.dirty)
        {
            bootbox.dialog({
                message: "You have unsaved changes, are you sure?",
                buttons: {
                    cancel: {
                        label: "No",
                        className: "btn-primary"
                    },
                    exit: {
                        label: "Yes",
                        className: "btn-default",
                        callback: () => { this.actionCancelContinue(); }
                    }
                }
            });
        }
        else
        {
            this.actionCancelContinue();
        }
    }

    actionCancelContinue()
    {
        if (this.currentMode == "open")
        {
            this.router.navigate(["/open"]);
        }
        else
        {
            this.router.navigate(["/sync"]);
        }
    }

    selectPrivateKey()
    {
        var form = this.openForm;

        // Open dialogue and read file
        var path = this.runtimeService.pickFile("Select private key", null, false);
        form.value["privateKeyPath"] = path;
    }

}
