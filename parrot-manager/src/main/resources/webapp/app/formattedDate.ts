/*
    Takes an epoch timestamp (millisecond) and formats it as an ISO-8601 date:
    https://www.w3schools.com/jsref/jsref_toisostring.asp

    If zero or null is provided, 'never' is returned.
*/

import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'formattedDate', pure: false})
export class FormattedDate implements PipeTransform
{

    transform(input : number): any
    {
        var result;

        if (input == null || input == 0)
        {
            result = "never";
        }
        else
        {
            result = new Date(input).toISOString();
        }

        return result;
    }

}
