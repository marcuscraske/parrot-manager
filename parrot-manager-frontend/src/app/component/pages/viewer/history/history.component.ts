import { Component, Renderer, Input, OnChanges, SimpleChanges } from '@angular/core';
import { EncryptedValue } from "app/model/encryptedValue"

import { RuntimeService } from "app/service/runtime.service"
import { DatabaseHistoryService } from "app/service/databaseHistory.service"

import { DatabaseNode } from "app/model/databaseNode"

@Component({
    selector: 'history',
    templateUrl: 'history.component.html',
    styleUrls: ['history.component.css']
})
export class HistoryComponent
{

    // The current node being changed; passed from parent
    @Input() currentNode : DatabaseNode;

    // Current history of the current node; cache to avoid fetching multiple times (cheaper)
    history: EncryptedValue[];

    constructor(
        private runtimeService: RuntimeService,
        private databaseHistoryService: DatabaseHistoryService,
        private renderer: Renderer
    ) { }

    ngOnChanges(changes: SimpleChanges)
    {
        var nodeId = this.currentNode.id;
        this.history = this.databaseHistoryService.fetch(nodeId);
    }

    trackChildren(index, historicValue)
    {
        return historicValue ? historicValue.lastModified : null;
    }

    clearAll()
    {
        // TODO pass ID to service
        //this.currentNode.getHistory().clearAll();
    }

    delete(encryptedValue)
    {
        // TODO pass ID
        //this.currentNode.getHistory().remove(encryptedValue);
    }

    restore(encryptedValue)
    {
        // TODO pass ID to service
        //this.currentNode.setValue(encryptedValue);
    }

}
