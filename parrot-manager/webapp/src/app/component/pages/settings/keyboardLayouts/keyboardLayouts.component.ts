import { Component } from '@angular/core';

import { SendKeysService } from 'app/service/sendKeys.service'
import { RuntimeService } from 'app/service/runtime.service'

@Component({
    templateUrl: "keyboardLayouts.component.html",
    selector: "keyboardLayouts"
})
export class KeyboardLayoutsComponent
{
    reloadErrorMessages: string;
    testerKeyCodes: string;

    constructor(
        private sendKeysService: SendKeysService,
        private runtimeService: RuntimeService
    ) { }

    clearTester()
    {
        var input = $("#keyboardLayoutTestInput");
        input.val("");
    }

    storeKeyCodePressedTester(event)
    {
        var keyCodes = event.keyCode;

        if (event.ctrlKey)
        {
            keyCodes = "Vk_CONTROL " + keyCodes;
        }

        if (event.altKey)
        {
            keyCodes = "VK_ALT " + keyCodes;
        }

        if (event.shiftKey)
        {
            keyCodes = "VK_SHIFT " + keyCodes;
        }

        if (event.metaKey)
        {
           keyCodes = "VK_META " + keyCodes;
        }

        // update keys pressed
        this.testerKeyCodes = keyCodes;
    }

    generateMappingTester()
    {
        var input = $("#keyboardLayoutTestInput");
        var output = $("#keyboardLayoutTestOutput");

        var character = input.val();
        var text = character + "    " + this.testerKeyCodes;
        output.val(text);

        console.log("updated keyboard layout tester - " + text);
    }

    reloadKeyboardLayouts()
    {
        // build messages for presentation
        var messages = this.sendKeysService.reload();
        var result = "";

        if (messages.length == 0)
        {
            result = "reloaded with no errors";
        }
        else
        {
            for (var i = 0; i < messages.length; i++)
            {
                result += messages[i] + "\n";
            }
        }

        // update messages
        this.reloadErrorMessages = result;

        toastr.success("Reloaded keyboard layouts");
    }

    copyKeyCodes()
    {
        var keyCodes = $("#keyboardLayoutTestOutput").val();
        this.runtimeService.setClipboard(keyCodes);
    }

    sendKeysTest()
    {
        var input = $("#keyboardLayoutSendKeysInput");
        var output = $("#keyboardLayoutSendKeysOutput");

        // focus output
        output.focus();

        // emulate keys
        setTimeout(() => {
            var text = input.val();
            this.sendKeysService.sendTest(text);
        }, 1000);
    }

}
