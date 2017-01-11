import { Injectable } from '@angular/core';

@Injectable()
export class DatabaseService {

    databaseService: any;

    constructor() {
        this.databaseService = (window as any).databaseService;
    }

    create(location, password, rounds) : boolean {
        // Create database
        return this.databaseService.create(location, password, rounds);
    }

    open(path, password) : string
    {
        return this.databaseService.open(path, password);
    }

    getFileName() : string {
        return this.databaseService.getFileName();
    }

    isOpen() : boolean {
        return this.databaseService.isOpen();
    }

}
