import { Injectable } from '@angular/core';
import { DatabaseService } from "app/service/database.service"
import { DatabaseNode } from "app/model/databaseNode"

@Injectable()
export class DatabaseNodeService
{

    constructor(private databaseService: DatabaseService)
    {
    }

    getChildren(nodeId: string) : DatabaseNode[]
    {
        var result = [];

        // Fetch native database node
        var nativeNode = this.databaseService.getNativeNode(nodeId);

        // Fetch native children
        var nativeChildNodes = nativeNode.getChildren();

        // Convert to json nodes
        for (var i = 0; i < nativeChildNodes.length; i++)
        {
            var nativeChildNode = nativeChildNodes[i];
            var node = new DatabaseNode(nativeChildNode);

            // Ignore "remote-sync" in root, as this is a special entry that should be hidden
            if (!node.isRoot && node.name != "remote-sync")
            {
                result.push(node);
            }
        }

        return result;
    }

}
