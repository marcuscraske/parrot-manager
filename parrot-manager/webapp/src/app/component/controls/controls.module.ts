import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { ReactiveFormsModule } from "@angular/forms";

import { PipesModule } from "app/component/pipes/pipes.module"

import { MessageBoxComponent } from "app/component/controls/messageBox/messageBox.component"
import { ChangeLogComponent } from "app/component/controls/changeLog/changeLog.component"
import { MergeLogComponent } from "app/component/controls/mergeLog/mergeLog.component"

@NgModule({
    imports: [
        BrowserModule,
        ReactiveFormsModule,
        PipesModule
    ],
    declarations: [
        MessageBoxComponent,
        MergeLogComponent,
        ChangeLogComponent
    ],
    exports: [
        MessageBoxComponent,
        ChangeLogComponent,
        MergeLogComponent
    ]
})
export class ControlsModule { }
