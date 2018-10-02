import { Component, Renderer } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

import { RuntimeService } from 'app/service/runtime.service'
import { DatabaseService } from 'app/service/database.service'
import { SyncService } from 'app/service/sync.service'
import { SyncSshService } from 'app/service/syncSsh.service'
import { SyncProfileService } from 'app/service/syncProfile.service'
import { SyncResultService } from 'app/service/syncResult.service'

import { Log } from 'app/model/log'
import { SyncProfile } from 'app/model/syncProfile'

@Component({
    templateUrl: 'sync-ssh.component.html',
    styleUrls: ['sync-ssh.component.css']
})
export class SyncSshComponent {

    public openForm = this.fb.group({
       name: ["", [Validators.required, Validators.minLength(1), Validators.maxLength(128)]],
       host: ["", [Validators.required, Validators.minLength(1), Validators.maxLength(128)]],
       port: [22, [Validators.required, Validators.min(1), Validators.max(65535)]],
       strictHostChecking : [false],
       user : ["", [Validators.required, Validators.minLength(1), Validators.maxLength(128)]],
       userPass : [""],
       remotePath : ["~/passwords.parrot", Validators.required],
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

    // Log from attempting to test settings
    log : Log = null;

    // Indicates whether to show spinner (for when syncing)
    showSpinner: boolean = false;

    // Observable subscription for params
    subParams : any;

    // The mode: open, new, edit
    currentMode : string;

    // The profile ID of the current sync profile being changed; passed by routing config, or populated upon test/download
    profileId : string;

    // Copy of original profile being edited
    profile: SyncProfile;

    // Event for listening to DB opening
    databaseOpenEvent: any;

    // Event for sync changes
    syncResultChangesEvent: any;
    syncStartEvent: any;

    constructor(
        private runtimeService: RuntimeService,
        private databaseService: DatabaseService,
        public syncService: SyncService,
        private syncSshService: SyncSshService,
        private syncProfileService: SyncProfileService,
        private syncResultService: SyncResultService,
        private router: Router,
        public fb: FormBuilder,
        private route: ActivatedRoute,
        private renderer: Renderer
    ) { }

    ngOnInit()
    {
        this.subParams = this.route.params.subscribe(params => {

            var passedNode = params['currentNode'];
            var populateMachineName = false;

            // determine appropriate mode and if we need to populate form with existing data
            if (passedNode == null)
            {
                this.profileId = null;
                this.currentMode = "open";
                populateMachineName = true;

                this.openForm.controls["destinationPath"].setValidators(Validators.required);
            }
            else if (passedNode == "new")
            {
                this.profileId = null;
                this.currentMode = "new";
                populateMachineName = true;

                console.log("changed to new mode");
            }
            else
            {
                this.profileId = passedNode;
                this.currentMode = "edit";
                this.populate(passedNode);

                console.log("changed to edit mode - profileId=" + this.profileId);
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

        // Listen for when DB opens (opening existing remote DB)
        this.databaseOpenEvent = this.renderer.listenGlobal("document", "database.open", (event) => {
            console.log("database open event, navigating to view entries page...");
            this.router.navigate(["/sync"]);
        });

        // Listen for when syncing starts
        this.syncStartEvent = this.renderer.listenGlobal("document", "sync.start", (event) => {
            var profile = this.syncProfileService.toJson(event.data);
            if (this.profileId == profile.id)
            {
                this.showSpinner = true;
                this.log = null;
                this.setFormDisabled(true);
            }
        });

        // Listen for sync changes, so we can populate any error results
        this.syncResultChangesEvent = this.renderer.listenGlobal("document", "syncResults.change", (event) => {
            var syncResults = this.syncResultService.getResults();
            if (syncResults != null && syncResults.length == 1)
            {
                var syncResult = syncResults[0];
                if (syncResult.profileId == this.profileId)
                {
                    this.log = syncResult.log;
                    this.showSpinner = false;
                    this.setFormDisabled(false);
                }
            }

        });
    }

    ngOnDestroy()
    {
        this.subParams.unsubscribe();

        // Reset results service
        this.syncResultService.clear();

        // Destroy event handlers
        this.syncStartEvent();
        this.syncResultChangesEvent();
    }

    /* Populates form using a specific node of remote-sync. */
    populate(nodeId)
    {
        // Fetch profile and populate form
        var profile = this.syncProfileService.fetchById(nodeId);

        if (profile != null)
        {
            // Store copy
            this.profile = profile;

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
        console.log("form " + (isDisabled ? "disabled" : "enabled"));
    }

    performOpen(options, profile)
    {
        // TODO disable form
        // Begin async chain to prompt for passwords etc
        this.sshAuthChain(options, profile, (options, profile) => {
            console.log("performing ssh sync download");
            this.syncService.download(options, profile);
        });
    }

    performTest()
    {
        // TODO disable form
        // TODO success message
        if (this.openForm.valid)
        {
            console.log("testing...");

            // Create download options
            var options = this.createOptions();
            var profile = this.createProfile();

            console.log("performing ssh sync test");
            this.syncService.test(options, profile, "ssh");
        }
        else
        {
            console.log("not testing, form is invalid");
        }
    }

    delete(profile)
    {
        this.syncProfileService.delete(profile);
        this.router.navigate(["/sync"]);
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

    // Ensures form has required values, even if not specified
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
        var json = form.value;
        var profile = this.syncProfileService.toProfile(json, "ssh");

        // Populate id as current node
        var jsonProfile = this.syncProfileService.toJson(profile);
        this.profileId = jsonProfile.id;

        return profile;
    }

    // Converts current form into SyncOptions instance
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
