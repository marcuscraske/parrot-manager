import { Component, Input } from '@angular/core';

import { SendKeysService } from 'app/service/sendKeys.service'
import { RuntimeService } from 'app/service/runtime.service'
import { ClipboardService } from 'app/service/clipboard.service'
import { BrowserService } from 'app/service/browser.service'

@Component({
    templateUrl: "keyboardLayouts.component.html",
    selector: "keyboardLayouts",
    providers: [BrowserService]
})
export class KeyboardLayoutsComponent
{
    reloadErrorMessages: string;
    testerKeyCodes: string;

    @Input()
    globalSettingsForm: any;

    constructor(
        public sendKeysService: SendKeysService,
        public runtimeService: RuntimeService,
        public clipboardService: ClipboardService,
        public browserService: BrowserService
    ) { }

    storeKeyCodePressedTester(event)
    {
        // clear tester
        var input = $("#keyboardLayoutTestInput");
        input.val("");

        // build key codes for mapping
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

        // store key codes
        this.testerKeyCodes = keyCodes;
    }

    generateMappingTester()
    {
        var input = $("#keyboardLayoutTestInput");
        var output = $("#keyboardLayoutTestOutput");

        // build mapping
        var character = input.val();
        var mapping = character + "    " + this.testerKeyCodes;

        // append to end as new line
        var newText = output.val();
        if (newText.length > 0)
        {
            newText += "\n";
        }
        newText += mapping;

        output.val(newText);

        // scroll to bottom
        output.scrollTop(output[0].scrollHeight - output.height());

        console.log("updated keyboard layout tester, added: " + mapping);
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
        this.clipboardService.setText(keyCodes);
    }

    clearKeyCodes()
    {
        var output = $("#keyboardLayoutTestOutput");
        output.val("");
    }

    sendKeysTest()
    {
        var input = $("#keyboardLayoutSendKeysInput");
        var output = $("#keyboardLayoutSendKeysOutput");

        // focus output
        output.val("");
        output.focus();

        // emulate keys
        setTimeout(() => {
            var text = input.val();
            this.sendKeysService.sendTest(text);
        }, 1000);
    }

    trackChildrenKeyboardLayouts(index, layout)
    {
        return layout ? layout.getName() : null;;
    }

}
