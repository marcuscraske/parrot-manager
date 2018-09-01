import { Component, AfterViewChecked, Renderer } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { DatabaseService } from 'app/service/database.service'
import { RemoteSyncService } from 'app/service/remoteSyncService.service'
import { RemoteSyncResultService } from 'app/service/remoteSyncResult.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'
import { ClipboardService } from 'app/service/clipboard.service'

@Component({
    templateUrl: 'remote-sync.component.html',
    styleUrls: ['remote-sync.component.css'],
    providers: [RemoteSyncResultService]
})
export class RemoteSyncComponent implements AfterViewChecked {

    private remoteSyncNode: any;
    private oldChangeLog: string;
    private remoteSyncChangeEvent: Function;
    public syncResults: any;

    constructor(
        public remoteSyncService: RemoteSyncService,
        public remoteSyncResultService: RemoteSyncResultService,
        public databaseService: DatabaseService,
        public encryptedValueService: EncryptedValueService,
        public clipboardService: ClipboardService,
        public router: Router,
        public fb: FormBuilder,
        private renderer: Renderer
    ) { }

    ngOnInit()
    {
        // Fetch 'remote-sync' node from database
        var database = this.databaseService.getDatabase();
        var rootNode = database.getRoot();

        this.remoteSyncNode = rootNode.getByName("remote-sync");

        // Hook for sync result changes
        this.remoteSyncChangeEvent = this.renderer.listenGlobal("document", "remoteSyncLogChange", (event) => {
            this.syncResults = event.data;
        });

        // Fetch last results
        this.syncResults = this.remoteSyncResultService.getResults();
    }

    ngOnDestroy()
    {
        this.remoteSyncChangeEvent();
    }

    ngAfterViewChecked()
    {
        // Keeps changelog scrolled to bottom
        var changeLog = $("#changeLog");
        var newChangeLog = changeLog.val();

        if (newChangeLog != this.oldChangeLog)
        {
            changeLog.scrollTop(changeLog[0].scrollHeight - changeLog.height());
            this.oldChangeLog = newChangeLog;
        }
    }

    trackChildren(index, profile)
    {
        return profile ? profile.id : null;
    }

    /* Determines if there are any remote-sync targets/hosts. */
    isTargets() : boolean
    {
        var result = this.remoteSyncNode != null && this.remoteSyncNode.getChildCount() > 0;
        return result;
    }

    // TODO cleanup
    overwrite(nodeId)
    {
        var options = this.convertNodeIdToOptions(nodeId);

        if (options != null)
        {
            this.remoteSyncService.overwrite(options);
        }
    }

    // TODO cleanup
    unlock(nodeId)
    {
        var options = this.convertNodeIdToOptions(nodeId);

        if (options != null)
        {
            this.remoteSyncService.unlock(options);
        }
    }

    // TODO cleanup
    sync(nodeId, askForPassword)
    {
        console.log("syncing node - id: " + nodeId);

        var options = this.convertNodeIdToOptions(nodeId);

        if (options != null)
        {
            if (askForPassword)
            {
                console.log("starting sync chain for host...");
                this.syncChainPromptDatabasePass(options);
            }
            else
            {
                console.log("syncing host...");
                this.syncHost(options, null);
            }
        }
    }

    syncChainPromptDatabasePass(options)
    {
        console.log("prompting for remote db pass...");

        bootbox.prompt({
            title: options.getName() + " - enter database password:",
            inputType: "password",
            callback: (password) => {

                if (password != null)
                {
                    // Continue next stage in the chain...
                    console.log("continuing to perform actual sync...");
                    this.syncChainPromptUserPass(options, password);
                }
                else
                {
                    console.log("no password specified, user has cancelled entering remote db password");
                }
            }
        });
    }

    syncChainPromptUserPass(options, remoteDatabasePassword)
    {
        if (options.isPromptUserPass())
        {
            console.log("prompting for user pass...");

            bootbox.prompt({
                title: options.getName() + " - enter SSH user password:",
                inputType: "password",
                callback: (password) => {
                    // Update options
                    options.setUserPass(password);

                    // Continue next stage in the chain...
                    console.log("continuing to key pass chain...");
                    this.syncChainPromptKeyPass(options, remoteDatabasePassword);
                }
            });
        }
        else
        {
            console.log("skipped user pass prompt, moving to key pass...");
            this.syncChainPromptKeyPass(options, remoteDatabasePassword);
        }
    }

    syncChainPromptKeyPass(options, remoteDatabasePassword)
    {
        if (options.isPromptKeyPass())
        {
            console.log("prompting for key pass...");

            bootbox.prompt({
                title: options.getName() + " - enter key password:",
                inputType: "password",
                callback: (password) => {
                    // Update options
                    options.setPrivateKeyPass(password);

                    // Continue next stage in the chain...
                    console.log("continuing to perform actual sync...");
                    this.syncHost(options, remoteDatabasePassword);
                }
            });
        }
        else
        {
            console.log("skipped prompting user pass, invoking final callback...");
            this.syncHost(options, remoteDatabasePassword);
        }
    }

    syncHost(options, remoteDatabasePassword)
    {
        // Invoke (async/non-blocking) sync...
        if (remoteDatabasePassword == null)
        {
            console.log("syncing without auth");
            this.remoteSyncService.sync(options);
        }
        else
        {
            console.log("syncing with auth");
            this.remoteSyncService.syncWithAuth(options, remoteDatabasePassword);
        }
    }

    isSyncing() : boolean
    {
        return this.remoteSyncService.isSyncing();
    }

    abort()
    {
        console.log("aborting sync");
        this.remoteSyncService.abort();
    }

    copySyncLogToClipboard()
    {
        // Fetch log as text
        var text = this.remoteSyncResultService.getResultsAsText();

        // Update clipboard
        this.clipboardService.setText(text);
    }

    private convertNodeIdToOptions(nodeId)
    {
        var options = null;

        try
        {
            var database = this.databaseService.getDatabase();
            var node = this.databaseService.getNode(nodeId);

            if (node != null)
            {
                console.log("creating initial ssh options for host...");
                options = this.remoteSyncService.createOptionsFromNode(database, node);

                if (options == null)
                {
                    throw "no options returned";
                }

                // Set destination path to current file open
                // TODO should be in backend
                var currentPath = this.databaseService.getPath();
                options.setDestinationPath(currentPath);
            }
            else
            {
                console.log("node not found for sync - id: " + nodeId);
            }
        }
        catch (e)
        {
            console.log("failed to create options for host");
            console.error(e);
            options = null;
        }

        // show error if failed
        if (options == null)
        {
            toastr.error(node.getName() + " - failed to read host options; delete and re-create the sync host...");
        }

        return options;
    }

}
