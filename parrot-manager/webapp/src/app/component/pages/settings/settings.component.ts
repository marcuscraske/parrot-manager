import { Component } from '@angular/core';

import { DatabaseService } from 'app/service/database.service'

@Component({
    templateUrl: "settings.component.html",
    styleUrls: ["settings.component.css"]
})
export class SettingsComponent
{
    public currentTab: string = "globalSettings";

    constructor(
        public databaseService: DatabaseService
    ) { }

}
