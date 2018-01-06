import { Component, Renderer, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';

import { RuntimeService } from 'app/service/runtime.service'
import { DatabaseService } from 'app/service/database.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'

@Component({
    selector: 'viewer-entries',
    templateUrl: 'entries.component.html',
    styleUrls: ['entries.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ViewerEntriesComponent
{

    // The current node being changed; passed from parent
    @Input() currentNode : any;

    // Functions on parent
    @Output() updateTree = new EventEmitter();
    @Output() changeNodeBeingViewed = new EventEmitter();

    constructor(
        private runtimeService: RuntimeService,
        private databaseService: DatabaseService,
        private encryptedValueService: EncryptedValueService,
        private renderer: Renderer
    ) { }

    addNewEntry()
    {
        // Add new node to current node
        var newNode = this.currentNode.addNew();

        if (newNode != null)
        {
            // Update tree
            this.updateTree.emit();

            // Change view to new node
            var nodeId = newNode.getId();
            this.changeNodeBeingViewed.emit(nodeId);

            console.log("added new entry - id: " + nodeId);
        }
        else
        {
            console.log("failed to create new node - curr node: " + this.currentNode.getId());
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

            var node = self.databaseService.getNode(nodeId);

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
        var node = this.databaseService.getNode(nodeId);

        // Delete the node
        node.remove();

        // Update tree
        this.updateTree.emit();
    }

    // Used by ngFor for custom tracking of nodes, otherwise DOM is spammed with create/destroy of children
    // http://blog.angular-university.io/angular-2-ngfor/
    trackChildren(index, node)
    {
        return node ? node.getId() : null;
    }

    isChildren()
    {
        var count;

        if (this.currentNode.isRoot())
        {
            var filtered = this.getFilteredChildren();
            count = filtered.length;
        }
        else
        {
            count = this.currentNode.getChildCount();
        }

        return count > 0;
    }

    getFilteredChildren()
    {
        var result;

        var children = this.currentNode.getChildren();

        if (this.currentNode.isRoot())
        {
            var filtered = [];
            var child;

            for (var i = 0; i < children.length; i++)
            {
                child = children[i];

                if (child.getName() != "remote-sync")
                {
                    filtered.push(child);
                }
            }

            result = filtered;
        }
        else
        {
            result = children;
        }

        return result;
    }

}
