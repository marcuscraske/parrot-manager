import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { BrowserModule } from "@angular/platform-browser";
import { ReactiveFormsModule } from "@angular/forms";

import { PipesModule } from "app/component/pipes/pipes.module"

import { SyncComponent } from "app/component/pages/sync/sync.component"
import { SyncSshComponent } from "app/component/pages/sync/ssh/syncSsh.component"

const routes: Routes = [
    { path: "sync",                     component: SyncComponent },
    { path: "sync/ssh",                 component: SyncSshComponent }
    { path: "sync/ssh/:currentNode",    component: SyncSshComponent }
];

@NgModule({
    imports: [
        BrowserModule,
        RouterModule.forChild(routes),
        ReactiveFormsModule,
        PipesModule
    ],
    declarations: [
        SyncComponent,
        SyncSshComponent
    ],
    exports: [
        RouterModule
    ]
})
export class SyncModule { }
