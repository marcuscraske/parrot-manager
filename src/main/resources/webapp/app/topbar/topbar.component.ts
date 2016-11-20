import { Component } from '@angular/core';

@Component({
  selector: 'topbar',
  templateUrl: './app/topbar/topbar.component.html'
})
export class TopBarComponent {

    exit() {
        (window as any).controller.exit();
    }

}
