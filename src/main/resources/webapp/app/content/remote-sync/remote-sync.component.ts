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

}