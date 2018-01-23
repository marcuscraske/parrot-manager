import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { BrowserModule } from "@angular/platform-browser";
import { ReactiveFormsModule } from "@angular/forms";

import { PipesModule } from "app/component/pipes/pipes.module"

import { SettingsComponent } from "app/component/pages/settings/settings.component"

import { ChangePasswordComponent } from "app/component/pages/settings/changePassword/changePassword.component"
import { DatabaseOptimisationComponent } from "app/component/pages/settings/databaseOptimisation/databaseOptimisation.component"

import { RecentFilesComponent } from "app/component/pages/settings/recentFiles/recentFiles.component"
import { KeyboardLayoutsComponent } from "app/component/pages/settings/keyboardLayouts/keyboardLayouts.component"

const routes: Routes = [
    { path: "settings", component: SettingsComponent }
];

@NgModule({
    imports: [
        BrowserModule,
        RouterModule.forChild(routes),
        ReactiveFormsModule,
        PipesModule
    ],
    declarations: [
        SettingsComponent,

        ChangePasswordComponent,
        DatabaseOptimisationComponent,

        RecentFilesComponent,
        KeyboardLayoutsComponent
    ],
    exports: [
        RouterModule
    ]
})
export class SettingsModule { }
