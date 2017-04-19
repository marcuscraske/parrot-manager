import { ErrorHandler } from "@angular/core";

export class ErrorWatcherHandler implements ErrorHandler
{

    handleError(error: any)
    {
        var message = error.message != null ? error.message : "(unknown)";
        var stack = error.stack != null ? error.stack : "(unknown)";

        console.error("angular caught exception - message: " + message + "\n\nstack:\n" + stack);

        errorWatcherShow(message, stack);
    }

}
