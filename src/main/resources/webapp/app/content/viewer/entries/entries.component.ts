import { Component, Renderer, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { RuntimeService } from 'app/service/runtime.service'
import { DatabaseService } from 'app/service/database.service'

@Component({
    moduleId: module.id,
    selector: 'viewer-entries',
    templateUrl: 'entries.component.html',
    styleUrls: ['entries.component.css'],
    providers: [RuntimeService, DatabaseService],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ViewerEntriesComponent
{

    // The current node being changed; passed from parent
    @Input() currentNode : any;

    // Functions on parent
    @Output() updateTree = new EventEmitter();
    @Output() changeNodeBeingViewed = new EventEmitter();

    constructor(private runtimeService: RuntimeService, private databaseService: DatabaseService,
                private renderer: Renderer)
    {
    }

    addNewEntry()
    {
        // Add new node to current node
        var newNode = this.currentNode.add();

        // Change view to new node
        var nodeId = newNode.getId();
        this.changeNodeBeingViewed.emit(nodeId);

        // Update tree
        // TODO: implement events on server side....
        this.updateTree.emit();

        console.log("added new entry - id: " + nodeId);
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

    toggleMask(target, targetNode, isInput)
    {
        var field = $(target);
        var masked = field.data("data-masked");

        // Determine new value
        var newValue;

        if (masked == null || masked)
        {
            // Switch to unmasked
            if (targetNode == null)
            {
                newValue = this.currentNode.getDecryptedValueString();
            }
            else
            {
                newValue = targetNode.getDecryptedValueString();
            }

            field.data("data-masked", false);
        }
        else
        {
            // Switch to masked
            newValue = "********";
            field.data("data-masked", true);
        }

        // Update element with new value
        if (isInput)
        {
            field.val(newValue);
        }
        else
        {
            field.text(newValue);
        }
    }

}
