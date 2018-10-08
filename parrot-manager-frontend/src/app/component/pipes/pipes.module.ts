import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";

// Pipes
import { OrderBy } from "app/component/pipes/orderBy"
import { FriendlyTime } from "app/component/pipes/friendlyTime"
import { FormattedDate } from "app/component/pipes/formattedDate"
import { FileNameWithoutExtension } from "app/component/pipes/fileNameWithoutExtension"

@NgModule({
    imports: [
        BrowserModule
    ],
    declarations: [
        OrderBy, FriendlyTime, FormattedDate, FileNameWithoutExtension
    ],
    exports: [
        OrderBy, FriendlyTime, FormattedDate, FileNameWithoutExtension
    ]
})
export class PipesModule { }
