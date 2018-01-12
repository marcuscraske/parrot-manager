import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { ReactiveFormsModule } from "@angular/forms";

import { PipesModule } from "app/component/pipes/pipes.module"

import { MessageBoxComponent } from "app/component/controls/messageBox/messageBox.component"

@NgModule({
    imports: [
        BrowserModule,
        ReactiveFormsModule,
        PipesModule
    ],
    declarations: [
        MessageBoxComponent
    ],
    exports: [
        MessageBoxComponent
    ]
})
export class ControlsModule { }
