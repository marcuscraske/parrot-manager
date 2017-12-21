/*
    Pipe for transforming (millisecond) epoch timestamps into friendly text.

    This will output 'xxx ago', where xxx is the most sensible time ago.

    If zero or null is provided, 'never' is returned.
*/

import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'friendlyTime', pure: false})
export class FriendlyTime implements PipeTransform
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
            var current = new Date().getTime();
            var duration = (current - input);

            var seconds = Math.floor(duration / 1000);
            var minutes = Math.floor(seconds / 60);
            var hours = Math.floor(minutes / 60);
            var days = Math.floor(hours / 24);

            var totalSeconds = seconds % 60;
            var totalMinutes = minutes % 60;
            var totalHours = hours % 24;
            var totalDays = days;

            var result;

            if (totalDays > 0)
            {
                result = totalDays + " day" + (totalDays > 1 ? "s" : "") + " ago";
            }
            else if (totalHours > 0)
            {
                result = totalHours + " hour" + (totalHours > 1 ? "s" : "") + " ago";
            }
            else if (totalMinutes > 0)
            {
                result = totalMinutes + " min" + (totalMinutes > 1 ? "s" : "") + " ago";
            }
            else
            {
                result = totalSeconds + " sec" + (totalSeconds > 1 ? "s" : "") + " ago";
            }
        }

        return result;
    }

}
