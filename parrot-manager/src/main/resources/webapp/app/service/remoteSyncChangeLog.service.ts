import { Injectable } from '@angular/core';

@Injectable()
export class RemoteSyncChangeLogService {

    // TODO: check if this is correct for singleton services...
    static changeLog: string = "";

    constructor()
    {
    }

    add(messages)
    {
        // Split result message and log each line
        var lines = messages.split("\n");
        var line;

        for (var i = 0; i < lines.length; i++)
        {
            line = lines[i];
            this.addLine(line);
        }
    }

    addLine(message)
    {
        // Append date to message
        var date = new Date();
        message = date.toLocaleTimeString() + " - " + message;

        // Log message
        console.log(message);

        // Append to changelog
        if (RemoteSyncChangeLogService.changeLog.length > 0)
        {
            RemoteSyncChangeLogService.changeLog += "\n" + message;
        }
        else
        {
            RemoteSyncChangeLogService.changeLog = message;
        }
    }

    getChangeLog()
    {
        return RemoteSyncChangeLogService.changeLog;
    }

    clear()
    {
        RemoteSyncChangeLogService.changeLog = "";
    }

}