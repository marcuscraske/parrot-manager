import { Injectable } from '@angular/core';

@Injectable()
export class RandomGeneratorService {

    randomGeneratorService : any;

    constructor()
    {
        this.randomGeneratorService = (window as any).randomGeneratorService;
    }

    generate(useNumbers : boolean, useUppercase : boolean, useLowercase : boolean, useSpecialChars : boolean, minLength : number, maxLength : number) : string
    {
        var result = this.randomGeneratorService.generate(useNumbers, useUppercase, useLowercase, useSpecialChars, minLength, maxLength);
        return result;
    }

}
