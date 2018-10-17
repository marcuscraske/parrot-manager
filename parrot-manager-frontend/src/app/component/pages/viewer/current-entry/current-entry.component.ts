import { Component, Renderer, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, Validators, FormGroup } from '@angular/forms';

import { ViewerService } from 'app/service/ui/viewer.service'
import { DatabaseService } from 'app/service/database.service'
import { DatabaseNodeService } from 'app/service/databaseNode.service'
import { RuntimeService } from 'app/service/runtime.service'
import { SyncService } from 'app/service/sync.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'

import { DatabaseNode } from "app/model/databaseNode"

@Component({
    selector: 'current-entry',
    templateUrl: 'current-entry.component.html',
    styleUrls: ['current-entry.component.css']
})
export class CurrentEntryComponent
{

    // The current node being changed; passed from parent
    @Input() currentNode : DatabaseNode;

    // The form for the encrypted value
    @Input() updateEntryForm : FormGroup;

    // Functions on parent
    @Output() updateTree = new EventEmitter();
    @Output() updateTreeSelection = new EventEmitter();
    @Output() changeNodeBeingViewed = new EventEmitter();
    @Output() saveValue = new EventEmitter();

    constructor(
        private viewerService: ViewerService,
        public databaseService: DatabaseService,
        public databaseNodeService: DatabaseNodeService,
        public runtimeService: RuntimeService,
        public syncService: SyncService,
        public encryptedValueService: EncryptedValueService,
        public renderer: Renderer,
        public fb: FormBuilder
    ) { }

    navigateToParent()
    {
        var parentNodeId = this.currentNode.parentId;
        console.log("navigating to parent: " + parentNodeId);
        this.changeNodeBeingViewed.emit(parentNodeId);
    }

    deleteCurrentEntry()
    {
        var nodeId = this.currentNode.id;
        console.log("deleting current entry - id: " + nodeId);

        var parentNodeId = this.currentNode.parentId;

        // Navigate to parent node
        console.log("navigating to parent node - id: " + parentNodeId);
        this.changeNodeBeingViewed.emit(parentNodeId);

        // Delete the node
        this.databaseNodeService.delete(nodeId);

        // Update tree
        this.updateTree.emit();
    }

    preUpdateName(event)
    {
        var field = event.target;
        var currentValue = field.value;

        // Wipe the name of unnamed nodes
        if (currentValue == "(unnamed)")
        {
            field.value = "";
        }
    }

    updateName(event)
    {
        var newName = event.target.value;

        // Prevent naming 'remote-sync'
        if (newName == "remote-sync")
        {
            toastr.error("Cannot name entry 'remote-sync', this is a reserved name.");
            return;
        }

        // Update name
        var nodeId = this.currentNode.id;
        this.databaseNodeService.setName(nodeId, newName);
        console.log("updateTitle - new name: " + newName);

        // Update tree
        this.updateTree.emit();

        // Update rest of view
        this.viewerService.changed();
    }

    postUpdateName(event)
    {
        var field = event.target;

        // Reset name to "(unnamed)" if empty
        if (field.value.length == 0)
        {
            field.value = "(unnamed)";
        }
    }

    // Displays decrypted value / switches to edit mode
    displayValue()
    {
        var currentValue = $("#currentValue");

        // decrypt value for current node
        var decryptedValue = this.encryptedValueService.getString(this.currentNode.id, null);
        currentValue.val(decryptedValue);

        console.log("current value displayed");

        // resize box to fit value
        this.resizeValueTextAreaToFitContent();
    }

    hideValue()
    {
        console.log("saving current value");

        // Fetch values; set to empty string if null, never allow null
        var currentValue = $("#currentValue");
        var value = currentValue.val();
        if (value == null)
        {
            value = "";
        }

        var decryptedValue = this.encryptedValueService.getString(this.currentNode.id, null);
        if (decryptedValue == null)
        {
            decryptedValue = "";
        }

        // Update value if changed
        var isChanged = value != decryptedValue;

        if (isChanged)
        {
            this.encryptedValueService.setString(this.currentNode.id, value);
        }

        // Reset form as untouched
        this.updateEntryForm.reset();

        // Update rest of view
        this.viewerService.changed();

        // Resize value box to fit content
        console.log("current value hidden");
        this.resizeValueTextAreaToFitContent();
    }

    // Resets edit mode when leaving text box of current value
    resetValueMode()
    {
        var currentValue = $("#currentValue");
        currentValue.data("edit", false);
    }

    // Resize field to fit value/content
    resizeValueTextAreaToFitContent()
    {
        var field = $("#currentValue")[0];

        // Resize box to fit content; reset to avoid inf. growing box
        field.style.height = "0px";
        field.style.height = field.scrollHeight + "px";
    }

}
