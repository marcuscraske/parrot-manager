import { Injectable } from '@angular/core';

@Injectable()
export class RuntimeService {

    changeHeight(newHeight) : void {
        (window as any).runtimeService.changeHeight(newHeight);
    }

    updateHeight() : void {
        var newHeight = document.body.scrollHeight;
        this.changeHeight(newHeight);
    }

    exit() : void {
         (window as any).runtimeService.exit();
    }

}
