import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { BrowserModule } from "@angular/platform-browser";
import { ReactiveFormsModule } from "@angular/forms";

import { PipesModule } from "app/component/pipes/pipes.module"

import { SettingsComponent } from "app/component/pages/settings/settings.component"
import { ChangePasswordComponent } from "app/component/pages/settings/changePassword/changePassword.component"
import { DatabaseOptimisationComponent } from "app/component/pages/settings/databaseOptimisation/databaseOptimisation.component"
import { GlobalSettingsComponent } from "app/component/pages/settings/globalSettings/globalSettings.component"

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
        GlobalSettingsComponent
    ],
    exports: [
        RouterModule
    ]
})
export class SettingsModule { }
