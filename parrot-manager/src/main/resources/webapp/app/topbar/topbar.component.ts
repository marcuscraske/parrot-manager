import { Component, Renderer } from '@angular/core';
import { Router } from '@angular/router';

import { RuntimeService } from 'app/service/runtime.service'
import { DatabaseService } from 'app/service/database.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'

import "app/global-vars"

@Component({
  moduleId: module.id,
  selector: 'topbar',
  templateUrl: 'topbar.component.html',
  styleUrls: ['topbar.component.css'],
  providers: [RuntimeService, DatabaseService, EncryptedValueService]
})
export class TopBarComponent
{

    private nativeExitListener: Function;

    constructor(
        private runtimeService: RuntimeService,
        private databaseService: DatabaseService,
        private router: Router,
        private renderer: Renderer
    )
    {
        // Hook for exit event on document (for when user triggers close on native window)
        this.nativeExitListener = renderer.listenGlobal("document", "nativeExit", (event) => {
            console.log("native exit event raised, attempting to close database...");
            this.attemptToCloseDatabase(true);
        });
    }

    ngOnDestroy()
    {
        // Dispose events
        this.nativeExitListener();
    }

    /*
        Used to attempt to close the database.

        If the database is dirty, this will invoke another function to prompt the user for an action.

        isExit - indicates if the invocation should exit the application, should the user choose the close option(s)
    */
    attemptToCloseDatabase(isExit)
    {
        var isDirty = this.databaseService.isDirty();

        if (isDirty)
        {
            console.log("database is dirty, showing close dialogue...");
            this.triggerCloseDialogue(isExit);
        }
        else
        {
            console.log("database is not dirty, proceeding to close database...");
            this.closeDatabase(isExit);
        }
    }

    /*
        Used to prompt the user when closing the database, or exiting the application, in regards to a dirty
        database.

        isExit - indicates if the invocation should exit the application, should the user choose the close option(s)
    */
    triggerCloseDialogue(isExit)
    {
        bootbox.dialog({
            message: "Your database has unsaved changes...",
            buttons: {
                cancel: {
                    label: "Cancel",
                    className: "btn-default",
                    callback: () => { this.handleCloseDatabaseButton("cancel", isExit); }
                },
                exit: {
                    label: "Close",
                    className: "btn-default",
                    callback: () => { this.handleCloseDatabaseButton("close", isExit); }
                },
                saveAndExit: {
                    label: "Save and Close",
                    className: "btn-primary",
                    callback: () => { this.handleCloseDatabaseButton("save-close", isExit); }
                }
            }
        });
    }

    /*
        Handles the result of the close dialogue.

        result - the result from the dialogue (text of button clicked)
        isExit - indicates if the invocation should exit the application, should the user choose the close option(s)
    */
    handleCloseDatabaseButton(result, isExit)
    {
        var close = false;

        switch (result)
        {
            case "cancel":
                console.log("user has chosen abort option");
                break;

            case "close":
                console.log("user has chosen close with unsaved changes");
                close = true;
                break;

            case "save-close":
                console.log("user has chosen close and exit");
                close = true;

                // Perform save of database; if it fails, do not close...
                if (!this.saveDatabase(true))
                {
                    close = false;
                }
                break;

            default:
                console.error("unknown option from close dialogue - " + result);
                break;
        }

        // Handle close action
        if (close)
        {
            // Close database...
            this.closeDatabase(isExit);
        }
    }

    /*
        Closes the database.
    */
    closeDatabase(isExit)
    {
        console.log("closing database...");
        this.databaseService.close();

        // Check if to exit application...
        if (isExit)
        {
            console.log("exiting application...");
            this.runtimeService.exit();
        }
        else
        {
            console.log("redirecting to open page...");
            this.router.navigate(["/open"]);
        }
    }

    /*
        Attempts to save the database. If an error occurs, the user is prompted to either try again or cancel.

        When exiting, the following buttons are given when failure: cancel, ignore (exits app)
        When not exiting, the following buttons are given when failure: close

        Returns true if success, otherwise false if unable to save / dialogue is shown.

        - isExit - indicates if this is during exit
    */
    saveDatabase(isExit)
    {
        console.log("saving database...");

        var result = this.databaseService.save();
        var isSuccess;

        if (result != null)
        {
            console.log("failed to save database, displaying prompt to user - reason: " + result);

            // Build buttons based on exiting or not...
            var buttons;

            if (isExit)
            {
                buttons = {
                    ignore: {
                        label: "Ignore and Exit",
                        className: "btn-default",
                        callback: () => { this.runtimeService.exit(); }
                    },
                    cancel: {
                        label: "Cancel",
                        className: "btn-primary"
                    }
                };
            }
            else
            {
                buttons = {
                    close: {
                        label: "Close",
                        className: "btn-primary"
                    }
                };
            }

            // Show dialogue with error...
            bootbox.dialog({
                title: "Failed to save database...",
                message: result,
                buttons: buttons
            });
        }
        else
        {
            isSuccess = true;
            console.log("successfully saved database");
        }

        return isSuccess;
    }

}
