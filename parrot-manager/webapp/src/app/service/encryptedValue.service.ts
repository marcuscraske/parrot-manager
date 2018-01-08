import { Injectable } from '@angular/core';

@Injectable()
export class EncryptedValueService {

    encryptedValueService : any;

    constructor()
    {
        this.encryptedValueService = (window as any).encryptedValueService;
    }

    // Persists SSH options; defined in this service to keep the actual injected service in this layer
    persistSshOptions(database, options)
    {
        options.persist(this.encryptedValueService, database);
    }

    setString(databaseNode, value)
    {
        // TODO fix
        var encryptedValue = this.encryptedValueService.fromString(value);
        databaseNode.setValue(encryptedValue);
    }

    getString(databaseNode) : string
    {
        // TODO fix
        var encryptedValue = databaseNode.getValue();
        var result = this.getStringFromValue(encryptedValue);
        return result;
    }

    getStringFromValue(encryptedValue) : string
    {
        // TODO fix
        var result = this.encryptedValueService.asString(encryptedValue);
        return result;
    }

}
