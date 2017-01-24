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

    public updateEntryForm = this.fb.group({
        currentValue: [""]
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

    // Updates the selected node in the tree with the current node
    updateTreeSelection()
    {
        $(() => {
            var currentSelected = $("#sidebar").jstree("get_selected");

            // Update selected item to match current node being viewed
            if (this.currentNode != null)
            {
                var targetNodeId = this.currentNode.getId();

                // Check the node is not already selected
                if (currentSelected == null || targetNodeId != currentSelected)
                {
                    // TODO: see if this can be improved with single call
                    $("#sidebar").jstree("deselect_all");
                    $("#sidebar").jstree("select_node", "#" + targetNodeId);
                    console.log("updated tree selection - id: " + targetNodeId);
                }
            }
            else
            {
                // Reset selected node
                $("#sidebar").jstree("deselect_all");
            }
        });
    }

    changeNodeBeingViewed(nodeId)
    {
        // Update node being viewed
        this.currentNode = this.databaseService.getNode(nodeId);
        this.updateTreeSelection();

        console.log("updated current node being edited: " + nodeId + " - result found: " + (this.currentNode != null));
    }

    updateTree()
    {
        // Fetch JSON data
        var data = this.databaseService.getJson();

        $(function(){
            // Update tree
            var tree = $("#sidebar").jstree(true);
            tree.settings.core.data = data;
            tree.refresh();

            // Expand all nodes
            $("#sidebar").jstree("open_all");
        });

        this.updateTreeSelection();
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
        this.changeNodeBeingViewed(nodeId);

        // Update tree
        // TODO: implement events on server side....
        this.updateTree();

        console.log("added new entry - id: " + nodeId);
    }

    // Used by ngFor for custom tracking of nodes, otherwise DOM is spammed with create/destroy of children
    // http://blog.angular-university.io/angular-2-ngfor/
    trackChildren(index, node)
    {
        return node ? node.getId() : null;
    }

    preUpdateName(event)
    {
        var field = event.target;
        var currentValue = field.value;

        // Wipe the name of unnamed nodes
        if (currentValue == "(unnamed)")
        {
            field.value = "";
        }
    }

    updateName(event)
    {
        // Update name
        var newName = event.target.value;
        this.currentNode.setName(newName);
        console.log("updateTitle - new name: " + newName);

        // Update tree
        this.updateTree();
    }

    updateCurrentEntry(event)
    {
        var form = this.updateEntryForm;

        if (form.valid)
        {
            var title = form.value["title"];
            var currentValue = form.value["currentValue"];

            console.log("updateCurrentEntry - updating entry - title: " + title);
        }
        else
        {
            console.log("updateCurrentEntry - invalid form");
        }
    }

}
