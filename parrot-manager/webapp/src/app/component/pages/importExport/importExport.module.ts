import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { BrowserModule } from "@angular/platform-browser";
import { ReactiveFormsModule } from "@angular/forms";

import { PipesModule } from "app/component/pipes/pipes.module"

import { HomeComponent } from "app/component/pages/importExport/home/home.component"

const routes: Routes = [
    { path: "import-export", component: HomeComponent }
];

@NgModule({
    imports: [
        BrowserModule,
        RouterModule.forChild(routes),
        ReactiveFormsModule,
        PipesModule
    ],
    declarations: [
        HomeComponent
    ],
    exports: [
        RouterModule
    ]
})
export class ImportExportModule { }
