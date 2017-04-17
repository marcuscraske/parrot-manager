import { Component, Renderer } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

import { DatabaseService } from 'app/service/database.service'
import { RuntimeService } from 'app/service/runtime.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'

@Component({
    moduleId: module.id,
    selector: 'viewer',
    templateUrl: 'viewer.component.html',
    styleUrls: ['viewer.component.css']
})
export class ViewerComponent
{

    // Event handle for "databaseUpdated" events
    private databaseEntryDeleteEvent: Function;
    private databaseEntryAddEvent: Function;
    private databaseClipboardEvent: Function;

    // The current node being edited
    public currentNode: any;

    // The node's current sub view (entries, history)
    public currentSubView: "entries";

    // Form for editing encrypted value; stored at parent level so we can check for change
    public updateEntryForm = this.fb.group({
        currentValue: [""]
    });

    constructor(
        private databaseService: DatabaseService,
        private runtimeService: RuntimeService,
        private encryptedValueService: EncryptedValueService,
        private renderer: Renderer,
        public fb: FormBuilder)
    {
        // Setup tree
        this.initTree();

        // Hook for events raised by application
        this.databaseEntryDeleteEvent = renderer.listenGlobal("document", "databaseEntryDelete", (event) => {
            console.log("databaseEntryDeleted event raised");

            var targetNode = event.data;

            // Fetch parent node
            var parentNode = targetNode.getParent();

            // Delete target node
            targetNode.remove();

            // Update tree
            this.updateTree();

            // Check current node still exists
            if (this.currentNode != null && this.currentNode.getId() == targetNode.getId())
            {
                console.log("current node deleted, navigating to parent...");
                this.changeNodeBeingViewed(parentNode.getId());
            }
            else
            {
                console.log("current node not deleted");
            }
        });

        this.databaseEntryAddEvent = renderer.listenGlobal("document", "databaseEntryAdd", (event) => {
            console.log("databaseEntryAdded event raised");

            var targetNode = event.data;

            // Add new node
            var newNode = targetNode.addNew();

            // Update tree
            this.updateTree();

            // Navigate to new node
            this.changeNodeBeingViewed(newNode.getId());
        });

        this.databaseClipboardEvent = renderer.listenGlobal("document", "databaseClipboardEvent", (event) => {
            console.log("databaseClipboardEvent event raised");

            // Decrypt value
            var targetNode = event.data;
            var encryptedValue = targetNode.getValue();
            var decryptedValue = this.encryptedValueService.getStringFromValue(encryptedValue);

            // Set on clipboard
            this.runtimeService.setClipboard(decryptedValue);
        });
    }

    ngOnInit()
    {
        // Set root node as current by default
        var database = this.databaseService.getDatabase();
        var rootNode = database.getRoot();
        this.changeNodeBeingViewed(rootNode.getId());
    }

    ngOnDestroy()
    {
        // Dispose events
        this.databaseEntryDeleteEvent();
        this.databaseEntryAddEvent();
        this.databaseClipboardEvent();
    }

    initTree()
    {
        $(() => {

            // Setup tree with drag-and-drop enabled
            var tree = $("#sidebar").jstree(
            {
                core:
                {
                    check_callback: true,
                    data: {}
                },
                dnd : { },
                types:
                {
                    "#":
                    {
                        max_children: 1
                    }
                },
                plugins: [ "types", "dnd", "sort" ]
            });

            // Always keep nodes open
            $("#sidebar").on("refresh.jstree load.jstree", () => {
                // Expand all nodes
                $("#sidebar").jstree("open_all");
            });

            // Hook tree for select event
            $("#sidebar").on("select_node.jstree", (e, data) => {
                // Check button was definitely left click
                // -- Disabling ctxmenu does not work, as JavaFX seems to change the event to left-click when selecting
                //    native item
                var evt = window.event || event;
                var button = evt == null ? null : (evt as any).which || (evt as any).button;

                if (button == null || button != 1)
                {
                    console.log("ignoring node selection, as not left click");
                    return false;
                }

                // Fetch UUID/ID of node from tree
                var nodeId = data.node.id;
                console.log("node selected in tree - id: " + nodeId);

                // Update current node being edited
                this.changeNodeBeingViewed(nodeId);
            });

            // Hook tree for move/dnd event
            // TODO: restrict root node from being moved
            $("#sidebar").on("move_node.jstree", (e, data) => {
                var nodeId = data.node.id;
                var newParentId = data.parent;

                var node = this.databaseService.getNode(nodeId);
                var newParentNode = this.databaseService.getNode(newParentId);
                var isCurrentNode = (nodeId == node.getId());

                // Move the node to new target node
                console.log("moving node: " + nodeId);
                node.moveTo(newParentNode);

                // Update tree data
                this.updateTree();

                // Re-navigate to moved node if we're currently viewing it
                if (isCurrentNode)
                {
                    console.log("viewing moved node, re-navigating...");

                    var newNodeId = node.getId();
                    this.changeNodeBeingViewed(newNodeId);
                }
            });

        });

        // Update actual data
        this.updateTree();
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
        });

        this.updateTreeSelection();
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
        console.log("request to change node - id: " + nodeId);

        this.continueActionWithPromptForDirtyValue(() => {
            // Update node being viewed
            this.currentNode = this.databaseService.getNode(nodeId);

            // Update node selected in tree
            this.updateTreeSelection();

            // Reset form
            this.updateEntryForm.reset();

            // Reset edit mode
            $("#currentValue").data("edit", false);

            // Reset sub-view
            this.currentSubView = "entries";

            console.log("updated current node being edited: " + nodeId + " - result found: " + (this.currentNode != null));
        });
    }

    // TODO: doesnt work for global exit of application, need to think of good way to approach this...
    continueActionWithPromptForDirtyValue(callbackContinue)
    {
        if (this.updateEntryForm.dirty)
        {
            bootbox.dialog({
                message: "Unsaved changes to value, these will be lost!",
                buttons: {
                    cancel: {
                        label: "Cancel",
                        className: "btn-default",
                        callback: () => { }
                    },
                    ignore: {
                        label: "Ignore",
                        className: "btn-default",
                        callback: () => { callbackContinue(); }
                    },
                    saveAndContinue: {
                        label: "Save and Continue",
                        className: "btn-primary",
                        callback: () => {
                            this.saveValue();
                            callbackContinue();
                        }
                    }
                }
            });
        }
        else
        {
            callbackContinue();
        }
    }

    // Saves the current (decrypted) value
    saveValue()
    {
        // Only allow save if in edit mode
        var currentValue = $("#currentValue");
        var isEditMode = currentValue.data("edit");

        console.error("EDIT MODE : " + isEditMode);

        if (isEditMode == true)
        {
            console.log("saving current value");

            // Fetch value and update current node
            var value = currentValue.val();
            this.encryptedValueService.setString(this.currentNode, value);

            // Reset form as untouched
            this.updateEntryForm.reset();

            // Switch out of edit mode
            currentValue.data("edit", false);
        }
    }

}
