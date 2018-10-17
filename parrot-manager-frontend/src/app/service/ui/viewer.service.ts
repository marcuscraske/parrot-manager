import { Injectable } from "@angular/core";
import { Observable } from "rxjs"
import { Subject } from "rxjs"

@Injectable()
export class ViewerService
{
    private refreshDataSubject = new Subject();

    /*
        Signals for subscribed components to refresh their data.
    */
    changed()
    {
        this.refreshDataSubject.next();
        console.log("triggered viewer changed");
    }

    /*
        Used to subscribe to changes, so that data in sub-components is refreshed.
    */
    getChanges() : Observable<any>
    {
        return this.refreshDataSubject.asObservable();
    }

}
