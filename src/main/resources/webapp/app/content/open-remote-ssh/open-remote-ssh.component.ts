import { Component } from '@angular/core';
import { DatabaseService } from '../../service/database.service'
import { RuntimeService } from '../../service/runtime.service'
import { Router } from '@angular/router';

import "app/global-vars"

@Component({
    moduleId: module.id,
    templateUrl: 'open-remote-ssh.component.html',
    providers: [DatabaseService, RuntimeService]
})
export class OpenRemoteSshComponent {

    errorMessage: string;

    constructor(private databaseService: DatabaseService, private runtimeService: RuntimeService, private router: Router) { }

}
