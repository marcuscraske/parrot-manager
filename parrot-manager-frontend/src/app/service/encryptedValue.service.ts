import { Injectable } from "@angular/core";
import { EncryptedValue } from "app/model/encryptedValue"
import { DatabaseService } from "app/service/database.service"
import { DatabaseHistoryService } from "app/service/databaseHistory.service"

@Injectable()
export class EncryptedValueService
{
    encryptedValueService : any;

    constructor(
        private databaseService: DatabaseService,
        private databaseHistoryService: DatabaseHistoryService,
    ) {
        this.encryptedValueService = (window as any).encryptedValueService;
    }

    setString(nodeId: string, value: string)
    {
        var nativeDatabase = this.databaseService.getDatabase();
        var nativeDatabaseNode = nativeDatabase.getNode(nodeId);

        if (nativeDatabaseNode != null)
        {
            var nativeEncryptedValue = this.encryptedValueService.fromString(nativeDatabase, value);
            nativeDatabaseNode.setValue(nativeEncryptedValue);
        }
    }

    /*
        Retrieves an encrypted value from a database node.

        Param 'historicItem' can be null if the value is the main database node value.
    */
    getString(nodeId: string, historicItem: EncryptedValue) : string
    {
        var result = null;

        var nativeDatabase = this.databaseService.getDatabase();
        var nativeDatabaseNode = nativeDatabase.getNode(nodeId);

        if (nativeDatabaseNode != null)
        {
            // Retrieve native encrypted value
            var nativeEncryptedValue;
            if (historicItem == null)
            {
                nativeEncryptedValue = nativeDatabaseNode.getValue();
            }
            else
            {
                var nativeHistory = nativeDatabaseNode.getHistory();
                nativeEncryptedValue = nativeHistory.fetchById(historicItem.id);
            }

            // Decrypt data
            result = this.encryptedValueService.asString(nativeDatabase, nativeEncryptedValue);
        }
        return result;
    }

}
