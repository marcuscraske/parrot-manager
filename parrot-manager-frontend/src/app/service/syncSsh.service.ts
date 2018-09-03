import { Injectable } from '@angular/core';

@Injectable()
export class SyncSshService
{

    authChain(options, profile, callback)
    {
        if (profile.getType() == "ssh")
        {
            console.log("ssh auth chain - starting");
            this.authChainPromptSshUserPass(options, profile, (options, profile) => {
                this.authChainPromptSshKeyPass(options, profile, callback);
            });
        }
        else
        {
            console.log("ssh auth chain - profile type not handled");
            callback(options, profile);
        }
    }

    private authChainPromptSshUserPass(options, profile, callback)
    {
        if (profile.isPromptUserPass())
        {
            console.log("prompting for user pass...");

            bootbox.prompt({
                title: options.getName() + " - enter SSH user password:",
                inputType: "password",
                callback: (password) => {
                    // Update options
                    profile.setUserPass(password);

                    // Continue next stage in the chain...
                    console.log("continuing to key pass chain...");
                    this.callback(options, profile);
                }
            });
        }
        else
        {
            console.log("skipped user pass prompt, moving to key pass...");
            this.callback(options, profile);
        }
    }

    private authChainPromptSshKeyPass(options, profile, callback)
    {
        if (profile.isPromptKeyPass())
        {
            console.log("prompting for key pass...");

            bootbox.prompt({
                title: options.getName() + " - enter key password:",
                inputType: "password",
                callback: (password) => {
                    // Update options
                    profile.setPrivateKeyPass(password);

                    // Continue next stage in the chain...
                    console.log("auth chain finished, invoking callback");
                    callback(options, profile);
                }
            });
        }
        else
        {
            console.log("skipped prompting user pass, invoking final callback...");
            callback(options, profile);
        }
    }

}
