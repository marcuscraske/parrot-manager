/*
    Pipe for transforming (millisecond) epoch timestamps into friendly text.

    This will output 'xxx ago', where xxx is the most sensible time ago.

    If zero or null is provided, 'never' is returned.
*/

import {Pipe, PipeTransform, OnDestroy, ChangeDetectorRef, NgZone} from '@angular/core';

@Pipe({name: 'friendlyTime', pure: false})
export class FriendlyTime implements PipeTransform, OnDestroy
{
    private timer: number;

    private text: string;
    private seconds: number;
    private secondsSince;

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private ngZone: NgZone
    ) {}

    transform(seconds : number): any
    {
        // Unless input changes, do nothing as we'll let timer handle changes to same input,
        // otherwise expression check errors occur

        if (seconds != this.seconds)
        {
            // Setup initial value / replace with new value
            this.seconds = seconds;
            this.text = this.generate(seconds);

            this.clearTimer();
            this.createTimer();
        }

        return this.text;
    }

    generate(input: number)
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

            this.secondsSince = seconds;
        }

        return result;
    }

    createTimer()
    {
        // abort if timer alread
        if (this.timer)
        {
            return;
        }

        // determine when next update will occur
        var nextUpdate;

        if (this.secondsSince < 60)
        {
            nextUpdate = 1;
        }
        else if (this.secondsSince < 60*60)
        {
            nextUpdate = 60;
        }
        else if (this.secondsSince < 60*60*24)
        {
            nextUpdate = 3600;
        }
        else
        {
            // every day
            nextUpdate = 86400;
        }

        // convert to seconds
        nextUpdate *= 1000;

        // prevent change detection from setting up the timeout (performance improvement)
        this.timer = this.ngZone.runOutsideAngular(() => {

            return window.setTimeout(() => {

                this.ngZone.run(() => {
                    // update text
                    this.text = this.generate(this.seconds);

                    // mark as changed
                    this.changeDetectorRef.markForCheck();

                    // prepare to update again
                    this.clearTimer();
                    this.createTimer();
                });

            }, nextUpdate);

        });
    }

    clearTimer()
    {
        if (this.timer)
        {
            window.clearTimeout(this.timer);
            this.timer = null;
        }
    }

    ngOnDestroy()
    {
        this.clearTimer();
    }

}
