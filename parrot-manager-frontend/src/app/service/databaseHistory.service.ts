import { Injectable } from '@angular/core';
import { EncryptedValue } from "app/model/encryptedValue"
import { ViewerService } from "app/service/ui/viewer.service"
import { DatabaseService } from "app/service/database.service"

@Injectable()
export class DatabaseHistoryService
{

    constructor(
        private viewerService: ViewerService,
        private databaseService: DatabaseService
    ) {
    }

    fetch(nodeId) : EncryptedValue[]
    {
        var result = [];

        var nativeHistory = this.getNativeHistory(nodeId);

        if (nativeHistory != null)
        {
            // Fetch entries
            var nativeEncryptedValues = nativeHistory.fetch();

            // Translate to JSON objects
            for (var i = 0; i < nativeEncryptedValues.length; i++)
            {
                var nativeEncryptedValue = nativeEncryptedValues[i];
                var encryptedValue = new EncryptedValue(nativeEncryptedValue);
                result.push(encryptedValue);
            }
        }

        return result;
    }

    delete(nodeId: string, encryptedValue: EncryptedValue)
    {
        var nativeHistory = this.getNativeHistory(nodeId);

        if (nativeHistory != null)
        {
            nativeHistory.delete(encryptedValue.id);
        }

        // Update rest of view
        this.viewerService.changed();
    }

    deleteAll(nodeId: string)
    {
        var nativeHistory = this.getNativeHistory(nodeId);

        if (nativeHistory != null)
        {
            nativeHistory.deleteAll();
        }

        // Update rest of view
        this.viewerService.changed();
    }

    restore(nodeId: string, encryptedValue: EncryptedValue)
    {
        var nativeHistory = this.getNativeHistory(nodeId);
        if (nativeHistory != null)
        {
            nativeHistory.restore(encryptedValue.id);
        }

        // Update rest of view
        this.viewerService.changed();
    }

    private getNativeHistory(nodeId) : any
    {
        var nativeHistory = null;

        var nativeNode = this.databaseService.getNativeNode(nodeId);
        if (nativeNode != null)
        {
            nativeHistory = nativeNode.getHistory();
        }

        return nativeHistory;
    }

}
