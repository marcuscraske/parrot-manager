import { Component } from '@angular/core';

import { DatabaseService } from 'app/service/database.service'

@Component({
    moduleId: module.id,
    templateUrl: "settings.component.html",
    styleUrls: ["settings.component.css"]
})
export class SettingsComponent
{
    public currentTab: string = "globalSettings";

    constructor(
        private databaseService: DatabaseService
    ) { }

}
