import { Component, Renderer } from '@angular/core';
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

    constructor(private databaseService: DatabaseService, private renderer: Renderer)
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
        // Setup tree with drag-drop enabled
        $(function(){

            var tree = $('#sidebar').jstree({
                core: {
                    check_callback: true,
                    data: {}
                },
                dnd : { },
                plugins: [ "dnd" ]
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
