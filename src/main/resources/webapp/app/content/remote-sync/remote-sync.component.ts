import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { RemoteSshFileService } from 'app/service/remoteSshFileService.service'
import { DatabaseService } from 'app/service/database.service'
import { Router, ActivatedRoute } from '@angular/router';

@Component({
    moduleId: module.id,
    templateUrl: 'remote-sync.component.html',
    styleUrls: ['remote-sync.component.css'],
    providers: [RemoteSshFileService, DatabaseService]
})
export class RemoteSyncComponent {

    constructor(private remoteSshFileService: RemoteSshFileService, private databaseService: DatabaseService,
                    private router: Router, public fb: FormBuilder, private route: ActivatedRoute) { }

}