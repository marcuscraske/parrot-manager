import { Injectable } from '@angular/core';

@Injectable()
export class RandomGeneratorService {

    randomGeneratorService : any;

    constructor()
    {
        this.randomGeneratorService = (window as any).runtimeService;
    }

    generate(useNumbers, useUppercase, useLowercase, useSpecialChars, minLength, maxLength) : string
    {
        return this.randomGeneratorService.generate(useNumbers, useUppercase, useLowercase, useSpecialChars, minLength, maxLength);
    }

}
