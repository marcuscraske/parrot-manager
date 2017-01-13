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

    save() : string
    {
        return this.databaseService.save();
    }

    close()
    {
        this.databaseService.close();
    }

    getFileName() : string
    {
        return this.databaseService.getFileName();
    }

    isOpen() : boolean
    {
        return this.databaseService.isOpen();
    }

    isDirty() : boolean
    {
        return this.databaseService.isDirty();
    }

}