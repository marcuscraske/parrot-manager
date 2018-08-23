import { Component } from '@angular/core';

import { DatabaseOptimizerService } from 'app/service/databaseOptimizer.service'
import { BackupService } from 'app/service/backup.service'

@Component({
    templateUrl: "databaseOptimisation.component.html",
    providers: [DatabaseOptimizerService],
    selector: "databaseOptimisation"
})
export class DatabaseOptimisationComponent
{

    constructor(
        private backupService: BackupService,
        private databaseOptimizerService: DatabaseOptimizerService
    ) { }

    databaseOptimizeDeletedNodeHistory()
    {
        if (this.backupService.create())
        {
            console.log("optimizing delete node history");
            this.databaseOptimizerService.optimizeDeletedNodeHistory();

            toastr.success("Cleared database node history");
        }
    }

    databaseOptimizeValueHistory()
    {
        if (this.backupService.create())
        {
            console.log("optimizing value history");
            this.databaseOptimizerService.optimizeValueHistory();

            toastr.success("Database value history cleared");
        }
    }

}
