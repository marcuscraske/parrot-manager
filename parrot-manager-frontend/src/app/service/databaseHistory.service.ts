import { Injectable } from '@angular/core';
import { EncryptedValue } from "app/model/encryptedValue"
import { DatabaseService } from "app/service/database.service"

@Injectable()
export class DatabaseHistoryService
{

    constructor(private databaseService: DatabaseService)
    {
    }

    fetch(nodeId) : EncryptedValue[]
    {
        var result = [];

        // Fetch the node
        var nativeNode = this.databaseService.getNativeNode(nodeId);

        if (nativeNode != null)
        {
            // Fetch entries
            var nativeEncryptedValues = nativeNode.getHistory().fetch();

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

}
