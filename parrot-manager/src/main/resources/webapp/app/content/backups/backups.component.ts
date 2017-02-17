import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DatabaseService } from '../../service/database.service'
import { RuntimeService } from '../../service/runtime.service'
import { Router } from '@angular/router';

@Component({
    moduleId: module.id,
    templateUrl: "backups.component.html",
    providers: [DatabaseService, RuntimeService],
    styleUrls: ["backups.component.css"]
})
export class BackupsComponent {

    constructor(
        public fb: FormBuilder, private databaseService: DatabaseService, private runtimeService: RuntimeService,
        private router: Router
    ) {}

}
