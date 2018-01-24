import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { ReactiveFormsModule } from "@angular/forms";

import { PipesModule } from "app/component/pipes/pipes.module"

import { MessageBoxComponent } from "app/component/controls/messageBox/messageBox.component"
import { ChangeLogComponent } from "app/component/controls/changeLog/changeLog.component"

@NgModule({
    imports: [
        BrowserModule,
        ReactiveFormsModule,
        PipesModule
    ],
    declarations: [
        MessageBoxComponent,
        ChangeLogComponent
    ],
    exports: [
        MessageBoxComponent,
        ChangeLogComponent
    ]
})
export class ControlsModule { }
