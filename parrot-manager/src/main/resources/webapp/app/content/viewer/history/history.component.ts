import { Component, Renderer, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { RuntimeService } from 'app/service/runtime.service'
import { DatabaseService } from 'app/service/database.service'

@Component({
    moduleId: module.id,
    selector: 'history',
    templateUrl: 'history.component.html',
    styleUrls: ['history.component.css'],
    providers: [RuntimeService, DatabaseService],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class HistoryComponent
{

    // The current node being changed; passed from parent
    @Input() currentNode : any;

    constructor(private runtimeService: RuntimeService, private databaseService: DatabaseService,
                private renderer: Renderer)
    {
    }

    trackChildren(index, node)
    {
        return node ? node.getId() : null;
    }

}
