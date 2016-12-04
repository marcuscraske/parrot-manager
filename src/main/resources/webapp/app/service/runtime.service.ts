import { Injectable } from '@angular/core';

@Injectable()
export class RuntimeService {

    exit() : void {
         (window as any).runtimeService.exit();
    }

}
