import { Component, AfterViewChecked } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { RemoteSshFileService } from 'app/service/remoteSshFileService.service'
import { DatabaseService } from 'app/service/database.service'
import { RemoteSyncChangeLogService } from 'app/service/remoteSyncChangeLog.service'
import { Router } from '@angular/router';

@Component({
    moduleId: module.id,
    templateUrl: 'remote-sync.component.html',
    styleUrls: ['remote-sync.component.css'],
    providers: [RemoteSshFileService, DatabaseService, RemoteSyncChangeLogService]
})
export class RemoteSyncComponent implements AfterViewChecked {

    remoteSyncNode : any;
    oldChangeLog : string;

    constructor(private remoteSshFileService: RemoteSshFileService, private databaseService: DatabaseService,
                private router: Router, public fb: FormBuilder, private remoteSyncChangeLogService: RemoteSyncChangeLogService
    ) { }

    ngOnInit()
    {
        // Fetch 'remote-sync' node from database
        var database = this.databaseService.getDatabase();
        var rootNode = database.getRoot();

        this.remoteSyncNode = rootNode.getByName("remote-sync");
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

    trackChildren(index, node)
    {
        return node ? node.getId() : null;
    }

    /* Retrieves the hostname behind a host/target, improves UI. */
    getHostName(node) : string
    {
        var result = "unknown";

        // Decrypt node value
        var json = node.getDecryptedValueString();
        var config = json != null ? JSON.parse(json) : null;

        if (config != null)
        {
            var host = config.host;
            var port = config.port;

            result = host + ":" + port;
        }
        else
        {
            console.log("unable to load host config - id: " + node.getId());
        }

        // If the name is the same as the host, then don't bother...
        if (result == node.getName())
        {
            result = "";
        }

        return result;
    }

    /* Determines if there are any remote-sync targets/hosts. */
    isTargets() : boolean
    {
        var result = this.remoteSyncNode != null && this.remoteSyncNode.getChildCount() > 0;
        return result;
    }

    sync()
    {
        console.log("starting sync...");

        // Grab all the selected hosts and convert each one to options
        var targetHosts = $("#remoteSyncTargets input[type=checkbox]:checked");

        var self = this;
        targetHosts.each(function() {

            // Read the node identifier
            var nodeId = $(this).attr("data-node-id");
            console.log("syncing node - id: " + nodeId);

            // Create SSH options and invoke sync
            var node = self.databaseService.getNode(nodeId);

            if (node != null)
            {
                console.log("creating initial ssh options for host...");
                var options = null;

                try
                {
                    // Read existing options
                    options = self.remoteSshFileService.createOptionsFromNode(node);

                    if (options == null)
                    {
                        throw "no options returned";
                    }

                    // Set destination path to current file open
                    var currentPath = self.databaseService.getPath();
                    options.setDestinationPath(currentPath);
                }
                catch (e)
                {
                    console.log("failed to create options for host");
                    console.error(e);
                    options = null;
                }

                if (options != null)
                {
                    console.log("starting sync chain for host...");
                    self.syncChainPromptUserPass(options);
                }
                else
                {
                    self.remoteSyncChangeLogService.add(node.getName() + " - failed to read host options; delete and re-create the sync host...");
                }
            }
            else
            {
                console.log("node not found for sync - id: " + nodeId);
            }

        });
    }

    syncChainPromptUserPass(options)
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
                    this.syncChainPromptKeyPass(options);
                }
            });
        }
        else
        {
            console.log("skipped user pass prompt, moving to key pass...");
            this.syncChainPromptKeyPass(options);
        }
    }

    syncChainPromptKeyPass(options)
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
                    this.syncChainPromptDatabasePass(options);
                }
            });
        }
        else
        {
            console.log("skipped prompting user pass, invoking final callback...");
            this.syncChainPromptDatabasePass(options);
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
                    this.syncHost(options, password);
                }
                else
                {
                    console.log("no password specified, user has cancelled entering remote db password");
                }
            }
        });
    }

    syncHost(options, remoteDatabasePassword)
    {
        this.remoteSyncChangeLogService.add(options.getName() + " - syncing host");

        var database = this.databaseService.getDatabase();
        var result = this.remoteSshFileService.sync(database, options, remoteDatabasePassword);

        // Split result message and log each line
        var lines = result.split("\n");
        var line;

        for (var i = 0; i < lines.length; i++)
        {
            line = options.getName() + " - " + lines[i];
            this.remoteSyncChangeLogService.add(line);
        }
    }

}