import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

@Component({
    selector: 'messageBox',
    templateUrl: 'messageBox.component.html'
})
export class MessageBoxComponent
{
    @Input() rows : number = 12;
    _messages : string[];
    text : string;

    constructor() { }

    ngAfterViewInit()
    {
        // Scroll to bottom
        var messageBox = $("messageBox textarea");
        if (messageBox.length > 0)
        {
            messageBox.scrollTop(messageBox[0].scrollHeight - messageBox.height());
        }
    }

    @Input()
    set messages(messages: string[])
    {
        this._messages = messages;
        this.updateText();
    }

    get messages(): string[]
    {
        return this._messages;
    }

    updateText()
    {
        // Reset text
        this.text = "";

        if (this._messages != null)
        {
            // Split result message and log each line
            for (var i = 0; i < this._messages.length; i++)
            {
                var line = this._messages[i];
                this.addLine(line);
            }
        }
    }

    addLine(line)
    {
        if (line != null)
        {
            // Append date to message
            var date = new Date();
            var message = date.toLocaleTimeString() + " - " + line;

            // Log message
            console.log(message);

            // Append to changelog
            if (this.text.length > 0)
            {
                this.text += "\n" + message;
            }
            else
            {
                this.text = message;
            }
        }
    }

}
