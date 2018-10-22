import { Component, Renderer, EventEmitter, Output } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

import { ViewerService } from 'app/service/ui/viewer.service'
import { DatabaseService } from 'app/service/database.service'
import { DatabaseNodeService } from 'app/service/databaseNode.service'
import { ClipboardService } from 'app/service/clipboard.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'
import { SearchFilterService } from 'app/service/searchFilter.service'

import { DatabaseNode } from "app/model/databaseNode"
import { EncryptedValue } from "app/model/encryptedValue"

@Component({
    selector: 'viewer',
    templateUrl: 'viewer.component.html',
    styleUrls: ['viewer.component.css'],
    providers: [SearchFilterService]
})
export class ViewerComponent
{

    // The current node being edited
    public currentNodeId: string;
    public currentNode: DatabaseNode;

    // The node's current sub view (entries, history)
    public currentSubView: string = "entries";

    // Event handle for "databaseUpdated" events
    private databaseEntryDeleteEvent: Function;
    private databaseEntryAddEvent: Function;
    private databaseClipboardEvent: Function;
    private databaseEntryExpandEvent: Function;
    private databaseEntryExpandAllEvent: Function;
    private databaseEntryCollapseEvent: Function;
    private databaseEntryCollapseAllEvent: Function;
    private remoteSyncingFinishedEvent: Function;


    // Form for editing encrypted value; stored at parent level so we can check for change
    public updateEntryForm = this.fb.group({
        currentValue: [""]
    });

    // Current search filter
    public searchFilter: string = "";

    constructor(
        public viewerService: ViewerService,
        public databaseService: DatabaseService,
        public databaseNodeService: DatabaseNodeService,
        public clipboardService: ClipboardService,
        public encryptedValueService: EncryptedValueService,
        public searchFilterService: SearchFilterService,
        public renderer: Renderer,
        public fb: FormBuilder
    )
    {
        // Setup tree
        this.initTree();

        // Hook for events raised by application
        this.databaseEntryDeleteEvent = renderer.listenGlobal("document", "databaseEntryDelete", (event) => {
            console.log("databaseEntryDeleted event raised");

            var nodeId = event.data;
            var node = this.databaseNodeService.delete(nodeId);

            // Update tree
            this.updateTree();

            // Check current node still exists
            if (this.currentNode != null && this.currentNode.id == node.id)
            {
                console.log("current node deleted, navigating to parent...");
                this.changeNodeBeingViewed(node.parentId);
            }
            else
            {
                console.log("current node not deleted");
            }
        });

        this.databaseEntryAddEvent = renderer.listenGlobal("document", "databaseEntryAdd", (event) => {
            console.log("databaseEntryAdded event raised");

            var nodeId = event.data;

            // Add new node
            var newNode = this.databaseNodeService.addChild(nodeId);

            // Update tree
            this.updateTree();

            // Navigate to new node
            this.changeNodeBeingViewed(newNode.id);
        });

        this.databaseClipboardEvent = renderer.listenGlobal("document", "databaseClipboardEvent", (event) => {
            console.log("databaseClipboardEvent event raised");

            var nodeId = event.data;
            var decryptedValue = this.encryptedValueService.getString(nodeId, null);
            this.clipboardService.setText(decryptedValue);
        });

        this.databaseEntryExpandEvent = renderer.listenGlobal("document", "databaseEntryExpand", (event) => {
            console.log("databaseEntryExpand event raised");

            var nodeId = event.data;
            this.databaseNodeService.setLocalProperty(nodeId, "collapsed", "false", false);
            this.updateTree();
        });

        this.databaseEntryExpandAllEvent = renderer.listenGlobal("document", "databaseEntryExpandAll", (event) => {
            console.log("databaseEntryExpandAll event raised");

            var nodeId = event.data;
            this.databaseNodeService.setLocalProperty(nodeId, "collapsed", "false", true);
            this.updateTree();
        });

        this.databaseEntryCollapseEvent = renderer.listenGlobal("document", "databaseEntryCollapse", (event) => {
            console.log("databaseEntryCollapse event raised");

            var nodeId = event.data;
            this.databaseNodeService.setLocalProperty(nodeId, "collapsed", "true", false);
            this.updateTree();
        });

        this.databaseEntryCollapseAllEvent = renderer.listenGlobal("document", "databaseEntryCollapseAll", (event) => {
            console.log("databaseEntryCollapseAll event raised");

            var nodeId = event.data;
            this.databaseNodeService.setLocalProperty(nodeId, "collapsed", "true", true);
            this.updateTree();
        });

        this.remoteSyncingFinishedEvent = renderer.listenGlobal("document", "sync.finish", (event) => {
            this.updateTree();
        });

        this.viewerService.getChanges().subscribe(data => {
            this.refreshData();
        });
    }

    ngOnInit()
    {
        // Set root node as current by default
        var rootNode = this.databaseNodeService.getRoot();
        this.changeNodeBeingViewed(rootNode.id);
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
        this.remoteSyncingFinishedEvent();
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
                var node = this.databaseService.getNativeNode(nodeId);
                node.setLocalProperty("collapsed", "false");
                console.log("node expanded - id: " + nodeId);
            });

            $("#tree").on("close_node.jstree", (e, data) => {
                var nodeId = data.node.id;
                var node = this.databaseService.getNativeNode(nodeId);
                node.setLocalProperty("collapsed", "true");
                console.log("node collapsed - id: " + nodeId);
            });

            // Hook tree for move/dnd event
            $("#tree").on("move_node.jstree", (e, data) => {
                var nodeId = data.node.id;
                var newParentId = data.parent;

                var node = this.databaseService.getNativeNode(nodeId);
                var newParentNode = this.databaseService.getNativeNode(newParentId);
                var isCurrentNode = (nodeId == node.id);

                // Move the node to new target node
                console.log("moving node: " + nodeId);
                node.moveTo(newParentNode);

                // Update tree data
                this.updateTree();

                // Re-navigate to moved node if we're currently viewing it
                if (isCurrentNode)
                {
                    console.log("viewing moved node, re-navigating...");

                    var newNodeId = node.id;
                    this.changeNodeBeingViewed(newNodeId);
                }
            });

            $("#tree").on("changed.jstree", (e, data) => {
                console.log("tree changed");
                this.updateTreeSelection();
            });

            $("#tree").on("refresh.jstree", (e, data) => {
                console.log("tree refreshed");
                this.updateTreeSelection();
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
        $(() => {
            // Update tree
            var tree = $("#tree").jstree(true);

            if (tree)
            {
                // Wipe tree; seems to be a bug with jstree where state is lost
                tree.settings.core.data = { };
                tree.refresh();

                // Restore data
                tree.settings.core.data = data;
                tree.refresh();

                console.log("tree updated");
            }
            else
            {
                console.log("tree not found");
            }
        });
    }

    // Updates the selected node in the tree with the current node
    updateTreeSelection()
    {
        $(() => {
            var currentSelected = $("#tree").jstree("get_selected");

            // Update selected item to match current node being viewed
            if (this.currentNode != null)
            {
                var targetNodeId = this.currentNode.id;

                // Check node exists in tree
                var exists = ($("#tree").find("#" + targetNodeId).length > 0);

                // Check the node is not already selected
                if (exists)
                {
                    if (currentSelected == null || targetNodeId != currentSelected)
                    {
                        $("#tree").jstree("deselect_all");
                        $("#tree").jstree("select_node", "#" + targetNodeId);
                        console.log("updated tree selection #2 - id: " + targetNodeId);
                    }
                    else
                    {
                        console.log("tree item already selected");
                    }
                }
                else
                {
                    console.log("unable to select tree node - not found - id: " + targetNodeId);
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

        // Update node being viewed
        this.currentNodeId = nodeId;
        this.refreshData();

        // Update node selected in tree
        this.updateTreeSelection();

        // Reset form
        this.updateEntryForm.reset();

        // Reset edit mode
        $("#currentValue").data("edit", false);

        // Reset sub-view
        this.currentSubView = "entries";

        console.log("updated current node being edited: " + nodeId + " - result found: " + (this.currentNode != null));
    }

    refreshData()
    {
        this.currentNode = this.databaseNodeService.getNode(this.currentNodeId);
    }

    updateSearchFilter(searchFilter)
    {
        console.log("search filter changed: " + searchFilter);
        this.searchFilter = searchFilter;
        this.updateTree();
    }

}
