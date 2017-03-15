import { Component } from '@angular/core';

import { RuntimeService } from 'app/service/runtime.service'
import { DatabaseService } from 'app/service/database.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'

@Component({
  selector: 'my-app',
  templateUrl: '/app/app.component.html',
  providers: [RuntimeService, DatabaseService, EncryptedValueService]
})
export class AppComponent {

    constructor()
    {
        // Setup global toastr configuration
        toastr.options = {
          "closeButton": false,
          "debug": false,
          "newestOnTop": false,
          "progressBar": false,
          "positionClass": "toast-top-right",
          "preventDuplicates": false,
          "onclick": null,
          "showDuration": "300",
          "hideDuration": "1000",
          "timeOut": "5000",
          "extendedTimeOut": "1000",
          "showEasing": "swing",
          "hideEasing": "linear",
          "showMethod": "fadeIn",
          "hideMethod": "fadeOut"
        }
    }

}
