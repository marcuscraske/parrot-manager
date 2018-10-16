import { Component, Renderer, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';

import { RuntimeService } from 'app/service/runtime.service'
import { DatabaseService } from 'app/service/database.service'
import { DatabaseNodeService } from 'app/service/databaseNode.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'

import { DatabaseNode } from "app/model/databaseNode"

@Component({
    selector: 'viewer-entries',
    templateUrl: 'entries.component.html',
    styleUrls: ['entries.component.css']
})
export class ViewerEntriesComponent
{

    // The current node being changed; passed from parent
    @Input() currentNode : any;

    // Functions on parent
    @Output() updateTree = new EventEmitter();
    @Output() changeNodeBeingViewed = new EventEmitter();

    // Cached children for current node being viewed
    children: DatabaseNode[];

    constructor(
        private runtimeService: RuntimeService,
        private databaseService: DatabaseService,
        private databaseNodeService: DatabaseNodeService,
        private encryptedValueService: EncryptedValueService,
        private renderer: Renderer
    ) { }

    ngOnChanges(changes: SimpleChanges)
    {
        var nodeId = this.currentNode.id;
        this.children = this.databaseNodeService.getChildren(nodeId);
    }

    addNewEntry()
    {
        // Add new node to current node
        var nodeId = this.currentNode.id;
        var newNode = this.databaseNodeService.addChild(nodeId);

        if (newNode != null)
        {
            // Update tree
            this.updateTree.emit();

            // Change view to new node
            var newNodeId = newNode.id;
            this.changeNodeBeingViewed.emit(newNodeId);

            console.log("added new entry - id: " + newNodeId);
        }
        else
        {
            console.log("failed to create new node - curr node: " + this.currentNode.id);
        }
    }

    deleteSelectAll(event)
    {
        var control = event.target;
        var value = $(control).prop("checked");

        var checkboxes = $("#currentValueEntries input[type=checkbox]").prop("checked", value);
        console.log("changing state of all delete checkboxes - value: " + value + ", count: " + checkboxes.length);
    }

    deleteSelected()
    {
        // Fetch each node selected and delete
        var entries = $("#currentValueEntries input[type=checkbox]:checked");

        console.log("deleting multiple entries - selected count: " + entries.length);

        var self = this;
        entries.each(function() {

            var nodeId = $(this).attr("data-node-id");
            console.log("deleting node - id: " + nodeId);

            var node = self.databaseService.getNativeNode(nodeId);

            if (node != null)
            {
                node.remove();
                console.log("node removed - id: " + nodeId);
            }
            else
            {
                console.log("node not found for removal - id: " + nodeId);
            }

        });

        // Update tree
        this.updateTree.emit();
    }

    deleteEntry(nodeId)
    {
        console.log("deleting entry - node id: " + nodeId);

        // Fetch node
        var node = this.databaseService.getNativeNode(nodeId);

        // Delete the node
        node.remove();

        // Update tree
        this.updateTree.emit();
    }

    // Used by ngFor for custom tracking of nodes, otherwise DOM is spammed with create/destroy of children
    // http://blog.angular-university.io/angular-2-ngfor/
    trackChildren(index, node)
    {
        return node ? node.id : null;
    }

    isChildren()
    {
        return this.children != null && this.children.length > 0;
    }

}
