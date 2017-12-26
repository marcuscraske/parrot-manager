import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";

// Pipes
import { OrderBy } from "app/component/pipes/orderBy"
import { FriendlyTime } from "app/component/pipes/friendlyTime"
import { FormattedDate } from "app/component/pipes/formattedDate"

@NgModule({
    imports: [
        BrowserModule
    ],
    declarations: [
        OrderBy, FriendlyTime, FormattedDate
    ],
    exports: [
        OrderBy, FriendlyTime, FormattedDate
    ]
})
export class PipesModule { }
