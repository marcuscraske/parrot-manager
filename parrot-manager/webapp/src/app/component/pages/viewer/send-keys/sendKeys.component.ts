import { Component, Input, EventEmitter, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subscription } from "rxjs";
import { TimerObservable } from "rxjs/observable/TimerObservable";

import { SendKeysService } from 'app/service/sendKeys.service'

@Component({
    selector: 'send-keys',
    templateUrl: 'sendKeys.component.html',
    styleUrls: ['sendKeys.component.css'],
    providers: [SendKeysService]
})
export class SendKeysComponent
{

    // The encrypted value being displayed
    @Input() encryptedValue : any;

    // Holds state as to whether this button's value is queued to be sent as keys
    public isQueued : boolean;

    // Used to hold a subscription for checking if this button/encrypted value is still queued for being sent
    private queueSubscription : Subscription;

    constructor(
        private changeDetection: ChangeDetectorRef,
        private sendKeysService: SendKeysService
    ) {
        this.isQueued = false;
    }

    sendKeys()
    {
        if (this.encryptedValue != null)
        {
            console.log("sending keys...");
            this.sendKeysService.send(this.encryptedValue);
            toastr.info("Click in a different application to send value as keys...");

            // Unsubscribe existing (if it exists)
            if (this.queueSubscription)
            {
                this.queueSubscription.unsubscribe();
            }

            // Subscribe for queue changes
            var timer = TimerObservable.create(0, 100);
            this.queueSubscription = timer.subscribe(() => this.updateIsQueued());
        }
        else
        {
            console.log("empty value, cannot send keys");
            toastr.error("Cannot send empty value as keys...");
        }
    }

    updateIsQueued()
    {
        // Update value
        this.isQueued = this.sendKeysService.isQueued(this.encryptedValue);

        // Unsubscribe once sent
        if (!this.isQueued)
        {
            this.queueSubscription.unsubscribe();
        }

        this.changeDetection.markForCheck();

        console.log("polled for send keys - " + this.isQueued);
    }

    ngOnDestroy()
    {
        if (this.queueSubscription != null)
        {
            this.queueSubscription.unsubscribe();
        }
    }

}
