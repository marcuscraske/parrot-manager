import { Component } from '@angular/core';

import { RuntimeService } from 'app/service/runtime.service'
import { DatabaseService } from 'app/service/database.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'

@Component({
  selector: 'my-app',
  templateUrl: '/app/app.component.html',
  providers: [RuntimeService, DatabaseService, EncryptedValueService]
})
export class AppComponent { }
