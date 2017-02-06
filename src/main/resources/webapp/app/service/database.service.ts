import { Injectable } from '@angular/core';

import "app/global-vars"

@Injectable()
export class DatabaseService
{

    // Underlying injected POJO
    databaseService: any;

    constructor()
    {
        this.databaseService = (window as any).databaseService;
    }

    create(location, password, rounds) : boolean
    {
        // Create database
        return this.databaseService.create(location, password, rounds);
    }

    open(path, password) : string
    {
        return this.databaseService.open(path, password);
    }

    openWithPrompt(path, successCallback) : void
    {
        if (path != null)
        {
            // Prompt for database password...
            console.log("prompting for database password... - path: " + path);

            bootbox.prompt({
                title: "Enter database password:",
                inputType: "password",
                callback: (password) => {
                    console.log("password entered, opening database file...");
                    this.openWithPassword(path, password, successCallback);
                }
            });
        }
        else
        {
            console.log("no path received from prompt, must have cancelled");
        }
    }

    openWithPassword(path, password, successCallback) : void
    {
        var message;

        if (path != null && password != null)
        {
            // Open database
            console.log("opening database... - path: " + path);
            message = this.databaseService.open(path, password);
        }
        else
        {
            console.log("path or password null, ignoring request to open database file");
            message = "Invalid database password";
        }

        // Invoking callback with message
        successCallback(message);
    }

    save() : string
    {
        return this.databaseService.save();
    }

    close()
    {
        this.databaseService.close();
    }

    getFileName() : string
    {
        return this.databaseService.getFileName();
    }

    isOpen() : boolean
    {
        return this.databaseService.isOpen();
    }

    isDirty() : boolean
    {
        return this.databaseService.isDirty();
    }

    /*
        Retrieves the JSON model of the database.
    */
    getJson() : any
    {
        var database = this.databaseService.getDatabase();
        var json;

        if (database != null)
        {
            // Iterate from root node and build tree
            var rootJsonNode = { "children" : [] };
            var rootDatabaseNode = database.getRoot();

            this.buildNode(rootJsonNode, rootDatabaseNode);

            // Put root node children on tree
            json = rootJsonNode.children;
        }
        else
        {
            json = [ { "id" : "empty-root-node", "text" : "empty" } ];
            console.log("database is not open / null, cannot rebuild tree");
        }

        return json;
    }

    private buildNode(currentJsonNode, databaseNode)
    {
        // Note: this JSON is based upon the expected format for JSTree (module for displaying trees)

        // -- Translate from database to JSON
        var name = databaseNode.getName();

        var newJsonNode = {
            "id" : databaseNode.getId(),
            "text" : name != null ? name : "(unnamed)",
            "children" : []
        };

        currentJsonNode.children.push(newJsonNode);

        // -- Add children
        var children = databaseNode.getChildren();
        var childDatabaseNode;

        for (var i = 0; i < children.length; i++)
        {
            childDatabaseNode = children[i];
            this.buildNode(newJsonNode, childDatabaseNode);
        }
    }

    getNode(id) : any
    {
        var database = this.databaseService.getDatabase();
        var node = null;

        if (database != null)
        {
            node = database.getNode(id);
        }
        else
        {
            console.log("database is not open / null, cannot fetch node");
        }

        return node;
    }

    getDatabase() : any
    {
        var database = this.databaseService.getDatabase();
        return database;
    }

}
