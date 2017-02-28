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

    // Form for editing encrypted value; stored at parent level so we can check for change
    public updateEntryForm = this.fb.group({
        currentValue: [""]
    });

    constructor(
        private databaseService: DatabaseService,
        private runtimeService: RuntimeService,
        private renderer: Renderer,
        public fb: FormBuilder)
    {
        // Setup tree
        this.initTree();

        // Hook for database update events
        this.nativeDatabaseUpdatedEvent = renderer.listenGlobal("document", "nativeDatabaseUpdated", (event) => {
            console.log("native databaseUpdated event raised, updating tree...");
            this.updateTree();
        });
    }

    ngOnInit()
    {
        // Set root node as current by default
        var database = this.databaseService.getDatabase();
        this.currentNode = database.getRoot();
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
            //this.updateEntryForm.reset();

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

    saveValue()
    {
        // Fetch value and update current node
        var value = $("#currentValue").val();
        this.currentNode.setValueString(value);

        // Reset form as untouched
        this.updateEntryForm.reset();
    }

}
