import { Injectable } from '@angular/core';

@Injectable()
export class RuntimeService {

    updateHeight() : void {
        var newHeight = document.body.scrollHeight;
        changeHeight(newHeight);
    }

    changeHeight(newHeight) : void {
        (window as any).runtimeService.changeHeight(newHeight);
    }

    exit() : void {
         (window as any).runtimeService.exit();
    }

}
