import { Component, Renderer } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

import { DatabaseService } from 'app/service/database.service'
import { RuntimeService } from 'app/service/runtime.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'
import { SearchFilterService } from 'app/service/searchFilter.service'

@Component({
    moduleId: module.id,
    selector: 'viewer',
    templateUrl: 'viewer.component.html',
    styleUrls: ['viewer.component.css'],
    providers: [SearchFilterService]
})
export class ViewerComponent
{

    // Event handle for "databaseUpdated" events
    private databaseEntryDeleteEvent: Function;
    private databaseEntryAddEvent: Function;
    private databaseClipboardEvent: Function;
    private databaseEntryExpandEvent: Function;
    private databaseEntryExpandAllEvent: Function;
    private databaseEntryCollapseEvent: Function;
    private databaseEntryCollapseAllEvent: Function;

    // The current node being edited
    public currentNode: any;

    // The node's current sub view (entries, history)
    public currentSubView: "entries";

    // Form for editing encrypted value; stored at parent level so we can check for change
    public updateEntryForm = this.fb.group({
        currentValue: [""]
    });

    // Current search filter
    public searchFilter: string = "";

    constructor(
        private databaseService: DatabaseService,
        private runtimeService: RuntimeService,
        private encryptedValueService: EncryptedValueService,
        private searchFilterService: SearchFilterService,
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

        this.databaseEntryExpandEvent = renderer.listenGlobal("document", "databaseEntryExpand", (event) => {
            console.log("databaseEntryExpand event raised");

            var targetNode = event.data;
            targetNode.setLocalProperty("collapsed", "false");
            this.updateTree();
        });

        this.databaseEntryExpandAllEvent = renderer.listenGlobal("document", "databaseEntryExpandAll", (event) => {
            console.log("databaseEntryExpandAll event raised");

            var targetNode = event.data;
            this.setLocalPropertyRecurse(targetNode, "collapsed", "false");
            this.updateTree();
        });

        this.databaseEntryCollapseEvent = renderer.listenGlobal("document", "databaseEntryCollapse", (event) => {
            console.log("databaseEntryCollapse event raised");

            var targetNode = event.data;
            targetNode.setLocalProperty("collapsed", "true");
            this.updateTree();
        });

        this.databaseEntryCollapseAllEvent = renderer.listenGlobal("document", "databaseEntryCollapseAll", (event) => {
            console.log("databaseEntryCollapseAll event raised");

            var targetNode = event.data;
            this.setLocalPropertyRecurse(targetNode, "collapsed", "true");
            this.updateTree();
        });
    }

    setLocalPropertyRecurse(parentNode, key, value)
    {
        parentNode.setLocalProperty(key, value);

        var childNodes = parentNode.getChildren();
        for (var i = 0; i < childNodes.length; i++)
        {
            this.setLocalPropertyRecurse(childNodes[i], key, value);
        }
    }

    ngOnInit()
    {
        // Set root node as current by default
        var database = this.databaseService.getDatabase();
        var rootNode = database.getRoot();

        if (database == null)
        {
            console.error("database is null");
        }

        if (rootNode == null)
        {
            console.error("root node is null");
        }

        this.changeNodeBeingViewed(rootNode.getId());
    }

    ngOnDestroy()
    {
        // Dispose events
        this.databaseEntryDeleteEvent();
        this.databaseEntryAddEvent();
        this.databaseClipboardEvent();
        this.databaseEntryExpandEvent();
        this.databaseEntryExpandAllEvent();
        this.databaseEntryCollapseEvent();
        this.databaseEntryCollapseAllEvent();
    }

    initTree()
    {
        $(() => {

            // Setup tree with drag-and-drop enabled
            var tree = $("#tree").jstree(
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

            // Hook tree for select event
            $("#tree").on("select_node.jstree", (e, data) => {
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

            // Hook tree for collapse/expand events
            $("#tree").on("open_node.jstree", (e, data) => {
                var nodeId = data.node.id;
                var node = this.databaseService.getNode(nodeId);
                node.setLocalProperty("collapsed", "false");
                console.log("node expanded - id: " + nodeId);
            });

            $("#tree").on("close_node.jstree", (e, data) => {
                var nodeId = data.node.id;
                var node = this.databaseService.getNode(nodeId);
                node.setLocalProperty("collapsed", "true");
                console.log("node collapsed - id: " + nodeId);
            });

            // Hook tree for move/dnd event
            $("#tree").on("move_node.jstree", (e, data) => {
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
        // fetch json data
        var data = this.databaseService.getJson();

        // apply search filter
        var searchFilter = this.searchFilter;
        if (searchFilter != null && searchFilter.length > 0)
        {
            data = this.searchFilterService.filterByName(data, searchFilter);
        }

        // update tree
        $(function(){
            // Update tree
            var tree = $("#tree").jstree(true);

            // wipe tree; seems to be a bug with jstree where state is lost
            tree.settings.core.data = { };
            tree.refresh();

            // restore data
            tree.settings.core.data = data;
            tree.refresh();
        });

        this.updateTreeSelection();
    }

    // Updates the selected node in the tree with the current node
    updateTreeSelection()
    {
        $(() => {
            var currentSelected = $("#tree").jstree("get_selected");

            // Update selected item to match current node being viewed
            if (this.currentNode != null)
            {
                var targetNodeId = this.currentNode.getId();

                // Check the node is not already selected
                if (currentSelected == null || targetNodeId != currentSelected)
                {
                    $("#tree").jstree("deselect_all");
                    $("#tree").jstree("select_node", "#" + targetNodeId);
                    console.log("updated tree selection - id: " + targetNodeId);
                }
            }
            else
            {
                // Reset selected node
                $("#tree").jstree("deselect_all");
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
        console.log("saving current value");

        // Fetch value and update current node
        var currentValue = $("#currentValue");
        var value = currentValue.val();
        this.encryptedValueService.setString(this.currentNode, value);

        // Reset form as untouched
        this.updateEntryForm.reset();

        // Switch out of edit mode
        currentValue.data("edit", false);
    }

    updateSearchFilter(searchFilter)
    {
        console.log("search filter changed: " + searchFilter);
        this.searchFilter = searchFilter;
        this.updateTree();
    }

}
