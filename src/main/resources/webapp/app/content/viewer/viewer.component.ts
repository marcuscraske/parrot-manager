import { Component, Renderer } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DatabaseService } from '../../service/database.service'
import { RuntimeService } from '../../service/runtime.service'

@Component({
    moduleId: module.id,
    selector: 'viewer',
    templateUrl: 'viewer.component.html',
    styleUrls: ['viewer.component.css'],
    providers: [DatabaseService, RuntimeService]
})
export class ViewerComponent
{

    // Event handle for "databaseUpdated" events
    private nativeDatabaseUpdatedEvent: Function;

    // The current node being edited
    public currentNode: any;

    public updateEntryForm = this.fb.group({
        currentValue: [""]
    });

    constructor(private databaseService: DatabaseService, private runtimeService: RuntimeService,
                private renderer: Renderer, public fb: FormBuilder)
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
        this.continueActionWithPromptForDirtyValue(() => {
            // Update node being viewed
            this.currentNode = this.databaseService.getNode(nodeId);
            this.updateTreeSelection();

            // Reset form
            this.updateEntryForm.reset();

            console.log("updated current node being edited: " + nodeId + " - result found: " + (this.currentNode != null));
        });
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

    deleteCurrentEntry()
    {
        console.log("deleting current entry");

        // Save parent identifier
        var parentNodeId = this.currentNode.getParent().getId();

        // Delete the node
        this.currentNode.remove();

        // Update tree
        this.updateTree();

        // Navigate to parent node
        console.log("navigating to parent node...");
        this.changeNodeBeingViewed(parentNodeId);
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

    postUpdateName(event)
    {
        var field = event.target;

        // Reset name to "(unnamed)" if empty
        if (field.value.length == 0)
        {
            field.value = "(unnamed)";
        }
    }

    preUpdateValue(event)
    {
        var field = event.target;

        // Only populate if empty
        if (field.value.length == 0)
        {
            this.populateValueTextAreaWithDecryptedStringValue(field);
        }
    }

    updateValue(event)
    {
        var field = event.target;
        this.resizeValueTextAreaToFitContent(field);
    }

    saveValue()
    {
        // Fetch value and update current node
        var value = $("#currentValue").val();
        this.currentNode.setValueString(value);

        // Reset form as untouched
        this.updateEntryForm.reset();
    }

    hideValue(target, ignoreDirty)
    {
        var field = $("#currentValue")[0];

        this.continueActionWithPromptForDirtyValue(() => {
            // Reset to empty and resize
            field.value = "";
            this.resizeValueTextAreaToFitContent(field);
        });
    }

    // Populates field with decrypted string value
    populateValueTextAreaWithDecryptedStringValue(field)
    {
        var decryptedValue = this.currentNode.getDecryptedValueString();
        field.value = decryptedValue;
        console.log("populated value field with actual decrypted value");

        this.resizeValueTextAreaToFitContent(field);
    }

    // Resize field to fit value/content
    resizeValueTextAreaToFitContent(field)
    {
        // Resize box to fit content; reset to avoid inf. growing box
        field.style.height = "0px";
        field.style.height = field.scrollHeight + "px";
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
                        callback: () => { this.saveValue(); callbackContinue(); }
                    }
                }
            });
        }
        else
        {
            callbackContinue();
        }
    }

}
