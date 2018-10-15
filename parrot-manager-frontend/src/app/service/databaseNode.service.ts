import { Injectable } from '@angular/core';
import { DatabaseService } from "app/service/database.service"
import { DatabaseNode } from "app/model/databaseNode"

@Injectable()
export class DatabaseNodeService
{
    // TODO consider moving operations into native

    constructor(private databaseService: DatabaseService)
    {
    }

    getRoot() : DatabaseNode
    {
        var nativeDatabase = this.databaseService.getDatabase();
        var nativeNode = nativeDatabase.getRoot();
        var node = new DatabaseNode(nativeNode);
        return node;
    }

    addChild(nodeId: string) : DatabaseNode
    {
        var nativeNode = this.databaseService.getNativeNode(nodeId);
        var newNativeNode = nativeNode.addNew();
        var newNode = new DatabaseNode(newNativeNode);
        return newNode;
    }

    delete(nodeId: string) : DatabaseNode
    {
        var nativeNode = this.databaseService.getNativeNode(nodeId);
        var node = new DatabaseNode(nativeNode);
        nativeNode.remove();
        return node;
    }

    setName(nodeId: string, name: string)
    {
        var nativeNode = this.databaseService.getNativeNode(nodeId);
        nativeNode.setName(name);
    }

    setLocalProperty(nodeId: string, key: string, value: string, applyToChildren: boolean)
    {
        var nativeNode = this.databaseService.getNativeNode(nodeId);
        nativeNode.setLocalProperty(key, value);
    }

    getNode(nodeId: string) : DatabaseNode
    {
        var nativeNode = this.databaseService.getNativeNode(nodeId);
        var node = new DatabaseNode(nativeNode);
        return node;
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
