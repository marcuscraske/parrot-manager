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
                this.currentNode = this.databaseService.getNode(nodeId);

                console.log("updated current node being edited: " + nodeId + " - result found: " + (this.currentNode != null));
            });

        });

        // Update actual data
        this.updateTree();
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

}
