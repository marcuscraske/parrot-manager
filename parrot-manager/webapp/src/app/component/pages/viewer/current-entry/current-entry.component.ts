import { Component, Renderer, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, Validators, FormGroup } from '@angular/forms';

import { DatabaseService } from 'app/service/database.service'
import { RuntimeService } from 'app/service/runtime.service'
import { RemoteSyncService } from 'app/service/remoteSyncService.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'

@Component({
    selector: 'current-entry',
    templateUrl: 'current-entry.component.html',
    styleUrls: ['current-entry.component.css']
})
export class CurrentEntryComponent
{

    // The current node being changed; passed from parent
    @Input() currentNode : any;

    // The form for the encrypted value
    @Input() updateEntryForm : FormGroup;

    // Functions on parent
    @Output() updateTree = new EventEmitter();
    @Output() updateTreeSelection = new EventEmitter();
    @Output() changeNodeBeingViewed = new EventEmitter();
    @Output() saveValue = new EventEmitter();

    constructor(
        public databaseService: DatabaseService,
        public runtimeService: RuntimeService,
        public remoteSyncService: RemoteSyncService,
        public encryptedValueService: EncryptedValueService,
        public renderer: Renderer,
        public fb: FormBuilder
    ) { }

    navigateToParent()
    {
        var parentNodeId = this.currentNode.getParent().getId();
        console.log("navigating to parent: " + parentNodeId);
        this.changeNodeBeingViewed.emit(parentNodeId);
    }

    deleteCurrentEntry()
    {
        console.log("deleting current entry");

        // Save parent identifier
        var parentNodeId = this.currentNode.getParent().getId();

        // Delete the node
        this.currentNode.remove();

        // Update tree
        this.updateTree.emit();

        // Navigate to parent node
        console.log("navigating to parent node - id: " + parentNodeId);
        this.changeNodeBeingViewed.emit(parentNodeId);
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
        this.currentNode.setName(newName);
        console.log("updateTitle - new name: " + newName);

        // Update tree
        this.updateTree.emit();
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
        var decryptedValue = this.encryptedValueService.getString(this.currentNode);
        currentValue.val(decryptedValue);

        console.log("current value displayed");

        // resize box to fit value
        this.resizeValueTextAreaToFitContent();
    }

    hideValue()
    {
        this.saveValue.emit();
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
