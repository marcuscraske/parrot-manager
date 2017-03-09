import { Component } from '@angular/core';

import { EncryptedValueService } from 'app/service/encryptedValue.service'

@Component({
  selector: 'my-app',
  templateUrl: '/app/app.component.html',
  providers: [EncryptedValueService]
})
export class AppComponent { }
