import { Injectable } from '@angular/core';

@Injectable()
export class DatabaseService {

    isOpen: boolean;
    fileName: string;

    constructor() {
        this.isOpen = false;
        this.fileName = "undefined";
    }

    create(location, password, rounds) : void {
        // Create database
        //(window as any).databaseService.create(location, password, rounds);

        // Set DB to open and file name from location
        this.isOpen = true;

        var pathSeparator = location.lastIndexOf('/');
        this.fileName = pathSeparator != -1 ? location.substring(pathSeparator+1) : location;
    }

    test() : string {
        return new Date() + " - " + this.fileName;
    }

}
