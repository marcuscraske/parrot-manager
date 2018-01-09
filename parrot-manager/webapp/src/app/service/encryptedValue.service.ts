import { Injectable } from '@angular/core';

import { DatabaseService } from 'app/service/database.service'

@Injectable()
export class EncryptedValueService {

    encryptedValueService : any;

    constructor(
        private databaseService: DatabaseService
    ) {
        this.encryptedValueService = (window as any).encryptedValueService;
    }

    // Persists SSH options; defined in this service to keep the actual injected service in this layer
    persistSshOptions(options)
    {
        var database = this.databaseService.getDatabase();
        options.persist(this.encryptedValueService, database);
    }

    setString(databaseNode, value)
    {
        var database = this.databaseService.getDatabase();
        var encryptedValue = this.encryptedValueService.fromString(database, value);
        databaseNode.setValue(encryptedValue);
    }

    getString(databaseNode) : string
    {
        var encryptedValue = databaseNode.getValue();
        var result = this.getStringFromValue(encryptedValue);
        return result;
    }

    getStringFromValue(encryptedValue) : string
    {
        var database = this.databaseService.getDatabase();
        var result = this.encryptedValueService.asString(database, encryptedValue);
        return result;
    }

}
