import { Component } from '@angular/core';
import { RuntimeService } from '../service/runtime.service'
import { DatabaseService } from '../service/database.service'

@Component({
  moduleId: module.id,
  selector: 'topbar',
  templateUrl: 'topbar.component.html',
  styleUrls: ['topbar.component.css'],
  providers: [RuntimeService, DatabaseService]
})
export class TopBarComponent {

    constructor(private runtimeService: RuntimeService, private databaseService: DatabaseService) { }

    exit() {
        this.runtimeService.exit();
    }

}
