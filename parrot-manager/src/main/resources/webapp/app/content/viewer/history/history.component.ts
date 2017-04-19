import { Component, Renderer, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { RuntimeService } from 'app/service/runtime.service'

@Component({
    moduleId: module.id,
    selector: 'history',
    templateUrl: 'history.component.html',
    styleUrls: ['history.component.css']
})
export class HistoryComponent
{

    // The current node being changed; passed from parent
    @Input() currentNode : any;

    constructor(
        private runtimeService: RuntimeService,
        private renderer: Renderer
    ) { }

    trackChildren(index, historicValue)
    {
        return historicValue ? historicValue.getFormattedLastModified() : null;
    }

    clearAll()
    {
        this.currentNode.getHistory().clearAll();
    }

    delete(encryptedValue)
    {
        this.currentNode.getHistory().remove(encryptedValue);
    }

}
