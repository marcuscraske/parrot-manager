import { Component, Renderer } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DatabaseService } from 'app/service/database.service'

@Component({
    moduleId: module.id,
    selector: 'generate-random',
    templateUrl: 'generate-random.component.html',
    providers: [DatabaseService]
})
export class GenerateRandomComponent
{

    // Form of options for random value generator
    public randomOptions;

    constructor(private databaseService: DatabaseService, private renderer: Renderer, public fb: FormBuilder)
    {
        // Set default values
        // TODO: read/save in database eventually
        this.randomOptions = this.fb.group({
             useNumbers: ["true"],
             useUppercase: ["true"],
             useLowercase: ["true"],
             useSpecialChars: ["true"],
             minLength: ["8"],
             maxLength: ["12"]
        });
    }

    generate(event)
    {
    }

}
