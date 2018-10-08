/*
    Takes an epoch timestamp (millisecond) and formats it as an ISO-8601 date:
    https://www.w3schools.com/jsref/jsref_toisostring.asp

    If zero or null is provided, 'never' is returned.
*/

import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'fileNameWithoutExtension'})
export class FileNameWithoutExtension implements PipeTransform
{

    transform(input : string): any
    {
        var result;

        var index = input.lastIndexOf(".");

        if (index > 0)
        {
            result = input.substring(0, index);
        }
        else
        {
            result = input;
        }

        return result;
    }

}
