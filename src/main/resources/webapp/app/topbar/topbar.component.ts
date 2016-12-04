import { Component } from '@angular/core';
import { RuntimeService } from '/app/service/runtime.service'

@Component({
  moduleId: module.id,
  selector: 'topbar',
  templateUrl: 'topbar.component.html',
  styleUrls: ['topbar.component.css'],
  providers: [RuntimeService]
})
export class TopBarComponent {

    constructor(private runtimeService: RuntimeService) { }

    exit() {
        this.runtimeService.exit();
    }

}
