import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { RemoteSshFileService } from 'app/service/remoteSshFileService.service'
import { DatabaseService } from 'app/service/database.service'
import { Router } from '@angular/router';

@Component({
    moduleId: module.id,
    templateUrl: 'remote-sync.component.html',
    styleUrls: ['remote-sync.component.css'],
    providers: [RemoteSshFileService, DatabaseService]
})
export class RemoteSyncComponent {

    remoteSyncNode : any;

    constructor(private remoteSshFileService: RemoteSshFileService, private databaseService: DatabaseService,
                    private router: Router, public fb: FormBuilder) { }

    ngOnInit()
    {
        // Fetch 'remote-sync' node from database
        var database = this.databaseService.getDatabase();
        var rootNode = database.getRoot();

        this.remoteSyncNode = rootNode.getByName("remote-sync");
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

                    // Set destination path to current file open
                    var currentPath = this.databaseService.getPath();
                    options.setDestinationPath(currentPath);
                }
                catch (e)
                {
                    console.log("failed to create options for host");
                    console.error(e);
                }

                if (options != null)
                {
                    console.log("starting sync chain for host...");
                    self.syncChainPromptUserPass(options);
                }
                else
                {
                    self.logChange(node.getName() + " - failed to read host options; delete and re-create the sync host...");
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
                    this.syncHost(options);
                }
            });
        }
        else
        {
            console.log("skipped prompting user pass, invoking final callback...");
            this.syncHost(options);
        }
    }

    syncChainPromptKeyPass(options)
    {
        console.log("prompting for remote db pass...");

        bootbox.prompt({
            title: options.getName() + " - enter database password:",
            inputType: "password",
            callback: (password) => {
                // Continue next stage in the chain...
                console.log("continuing to perform actual sync...");
                this.syncHost(options, password);
            }
        });
    }

    syncHost(options, remoteDatabasePassword)
    {
        this.logChange(options.getName() + " - syncing host");

        var database = this.databaseService.getDatabase();
        var result = this.remoteSshFileService.sync(database, options, remoteDatabasePassword);
        this.logChange(options.getName() + " - " + result);
    }

    logChange(message)
    {
        var changeLog = $("#changeLog");

        // Append date to message
        var date = new Date();
        message = date.toLocaleTimeString() + " - " + message;

        // Log to console
        console.log("change log - " + message);

        // Append to changelog
        var html = "<option>" + message + "</option>";
        changeLog.append(html);

        // Scroll to bottom
        // TODO: find better method, perhaps with scrollTop...
        changeLog.children(":last-child").prop("selected", true);
        changeLog.children(":last-child").prop("selected", false);
    }

}