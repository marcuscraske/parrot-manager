import { Component, AfterViewChecked } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { DatabaseService } from 'app/service/database.service'
import { RemoteSshFileService } from 'app/service/remoteSshFileService.service'
import { RemoteSyncChangeLogService } from 'app/service/remoteSyncChangeLog.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'
import { RuntimeService } from 'app/service/runtime.service'

@Component({
    moduleId: module.id,
    templateUrl: 'remote-sync.component.html',
    styleUrls: ['remote-sync.component.css']
})
export class RemoteSyncComponent implements AfterViewChecked {

    remoteSyncNode : any;
    oldChangeLog : string;

    constructor(
        private remoteSshFileService: RemoteSshFileService,
        private databaseService: DatabaseService,
        private encryptedValueService: EncryptedValueService,
        private runtimeService: RuntimeService,
        private router: Router,
        public fb: FormBuilder,
        private remoteSyncChangeLogService: RemoteSyncChangeLogService
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
        var json = this.encryptedValueService.getString(node);
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

    syncSelected()
    {
        console.log("starting sync...");

        // Grab all the selected hosts and convert each one to options
        var targetHosts = $("#remoteSyncTargets input[type=checkbox]:checked");

        var self = this;
        targetHosts.each(function() {
            var nodeId = $(this).attr("data-node-id");
            self.sync(nodeId);
        });
    }

    sync(nodeId)
    {
        console.log("syncing node - id: " + nodeId);

        // Create SSH options and invoke sync
        var node = this.databaseService.getNode(nodeId);

        if (node != null)
        {
            console.log("creating initial ssh options for host...");
            var options = null;

            try
            {
                // Read existing options
                options = this.remoteSshFileService.createOptionsFromNode(node);

                if (options == null)
                {
                    throw "no options returned";
                }

                // Set destination path to current file open
                var currentPath = this.databaseService.getPath();
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
                this.syncHost(options);
            }
            else
            {
                this.remoteSyncChangeLogService.add(node.getName() + " - failed to read host options; delete and re-create the sync host...");
            }
        }
        else
        {
            console.log("node not found for sync - id: " + nodeId);
        }
    }

    syncHost(options)
    {
        var database = this.databaseService.getDatabase();

        // Invoke (async/non-blocking) sync...
        this.remoteSshFileService.sync(database, options);
    }

    copyToClipboard()
    {
        var changeLog = this.remoteSyncChangeLogService.getChangeLog();
        this.runtimeService.setClipboard(changeLog);
    }

    isSyncing() : boolean
    {
        return this.remoteSshFileService.isSyncing();
    }

    getCurrentHost() : string
    {
        return this.remoteSshFileService.getCurrentHost();
    }

    abort()
    {
        console.log("aborting sync");
        this.remoteSshFileService.abort();
    }

}