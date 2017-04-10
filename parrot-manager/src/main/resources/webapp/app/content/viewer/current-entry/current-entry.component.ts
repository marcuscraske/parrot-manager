import { Component, Renderer, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, Validators, FormGroup } from '@angular/forms';

import { DatabaseService } from 'app/service/database.service'
import { RuntimeService } from 'app/service/runtime.service'
import { EncryptedValueService } from 'app/service/encryptedValue.service'

@Component({
    moduleId: module.id,
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
    @Output() continueActionWithPromptForDirtyValue = new EventEmitter();
    @Output() saveValue = new EventEmitter();

    constructor(
        private databaseService: DatabaseService,
        private runtimeService: RuntimeService,
        private encryptedValueService: EncryptedValueService,
        private renderer: Renderer,
        public fb: FormBuilder
    ) {
    }

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
        console.log("navigating to parent node...");
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
        // Update name
        var newName = event.target.value;
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

    // Refreshes the decrypted value, if in edit mode (used for when generating random number)
    refreshValue()
    {
        var field = $("#currentValue");
        var isEditMode = field.data("edit");

        if (isEditMode == true)
        {
            this.displayValue();
        }
    }

    // Displays decrypted value / switches to edit mode
    displayValue()
    {
        var currentValue = $("#currentValue");

        // Set the value
        var decryptedValue = this.encryptedValueService.getString(this.currentNode);
        currentValue.val(decryptedValue);
        console.log("populated value field with actual decrypted value");

        // Resize box to fit value
        this.resizeValueTextAreaToFitContent();

        // Set param for edit mode
        currentValue.data("edit", true);
    }

    // Hides the decrypted value / switches out of edit mode
    hideValue()
    {
        var field = $("#currentValue");

        this.continueActionWithPromptForDirtyValue.emit(() => {
            // Reset to empty and resize
            field.val("");
            this.resizeValueTextAreaToFitContent();

            // Switch out of edit mode
            field.data("edit", false);
        });
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
