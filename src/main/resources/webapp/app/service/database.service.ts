import { Injectable } from '@angular/core';

@Injectable()
export class DatabaseService {

    create(location, password, rounds) : void {
         (window as any).controller.exit();
    }

}
