import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { ReactiveFormsModule } from "@angular/forms";

import { PipesModule } from "app/component/pipes/pipes.module"

import { MessageBoxComponent } from "app/component/controls/messageBox/messageBox.component"
import { ChangeLogComponent } from "app/component/controls/changeLog/changeLog.component"
import { LogComponent } from "app/component/controls/log/log.component"

@NgModule({
    imports: [
        BrowserModule,
        ReactiveFormsModule,
        PipesModule
    ],
    declarations: [
        ChangeLogComponent,
        LogComponent,
        MessageBoxComponent
    ],
    exports: [
        ChangeLogComponent,
        LogComponent,
        MessageBoxComponent
    ]
})
export class ControlsModule { }
