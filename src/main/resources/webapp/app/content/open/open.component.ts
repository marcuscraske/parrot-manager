import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DatabaseService } from '../../service/database.service'
import { Router } from '@angular/router';

@Component({
    moduleId: module.id,
    templateUrl: 'open.component.html',
    providers: [DatabaseService]
})
export class OpenComponent {

    constructor(private databaseService: DatabaseService) { }

}
