import { Injectable } from '@angular/core';

@Injectable()
export class DatabaseOptimizerService {

    databaseOptimizerService : any;

    constructor()
    {
        this.databaseOptimizerService = (window as any).databaseOptimizerService;
    }

    optimizeDeletedNodeHistory()
    {
        this.databaseOptimizerService.optimizeDeletedNodeHistory();
    }

    optimizeValueHistory()
    {
        this.databaseOptimizerService.optimizeValueHistory();
    }

}
