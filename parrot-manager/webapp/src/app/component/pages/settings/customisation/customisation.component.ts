import { Component, Input } from '@angular/core';
import { ThemeService } from 'app/service/theme.service'

@Component({
    templateUrl: "customisation.component.html",
    selector: "customisation"
})
export class CustomisationComponent
{
    @Input()
    globalSettingsForm: any;

    constructor(
        public themeService: ThemeService
    ) {}

}
