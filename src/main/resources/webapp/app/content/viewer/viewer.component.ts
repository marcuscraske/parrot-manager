import { Component, Renderer } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DatabaseService } from '../../service/database.service'

@Component({
    moduleId: module.id,
    selector: 'viewer',
    templateUrl: 'viewer.component.html',
    styleUrls: ['viewer.component.css'],
    providers: [DatabaseService]
})
export class ViewerComponent
{

    // Event handle for "databaseUpdated" events
    private nativeDatabaseUpdatedEvent: Function;

    // The current node being edited
    currentNode: any;

    public addEntryForm = this.fb.group({
        name: ["", Validators.required],
        value: ["", Validators.required]
    });

    constructor(private databaseService: DatabaseService, private renderer: Renderer, public fb: FormBuilder)
    {
        // Setup tree
        this.initTree();

        // Hook for database update events
        this.nativeDatabaseUpdatedEvent = renderer.listenGlobal("document", "nativeDatabaseUpdated", (event) => {
            console.log("native databaseUpdated event raised, updating tree...");
            this.updateTree();
        });
    }

    ngOnDestroy()
    {
        // Dispose events
        this.nativeDatabaseUpdatedEvent();
    }

    initTree()
    {
        $(() => {

            // Setup tree with drag-and-drop enabled
            var tree = $("#sidebar").jstree({
                core: {
                    check_callback: true,
                    data: {}
                },
                dnd : { },
                plugins: [ "dnd" ]
            });

            // Hook tree for select event
            $("#sidebar").on("select_node.jstree", (e, data) => {
                // Fetch UUID/ID of node from tree
                var nodeId = data.node.id;

                // Update current node being edited
                this.changeNodeBeingViewed(nodeId);
            });

        });

        // Update actual data
        this.updateTree();
    }

    changeNodeBeingViewed(nodeId)
    {
        this.currentNode = this.databaseService.getNode(nodeId);
        console.log("updated current node being edited: " + nodeId + " - result found: " + (this.currentNode != null));
    }

    updateTree()
    {
        // Fetch JSON data
        var data = this.databaseService.getJson();

        // Update tree
        $(function(){
            var tree = $('#sidebar').jstree(true);
            tree.settings.core.data = data;
            tree.refresh();
        });
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

    addNewEntry()
    {
        // Add new node to current node
        var newNode = this.currentNode.add();

        // Change view to new node
        var nodeId = newNode.getId();
        changeNodeBeingViewed(nodeId);

        // Update tree
        this.updateTree();

        console.log("added new entry - id: " + nodeId);
    }

}
