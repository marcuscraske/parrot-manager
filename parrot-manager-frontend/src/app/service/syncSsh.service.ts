import { Injectable } from '@angular/core';
import { SshSyncProfile } from 'app/model/sshSyncProfile'

@Injectable()
export class SyncSshService
{

    toJson(nativeProfile)
    {
        var profile = new SshSyncProfile();
        profile.id = nativeProfile.getId();
        profile.name = nativeProfile.getName();
        profile.host = nativeProfile.getHost();
        profile.port = nativeProfile.getPort();
        profile.user = nativeProfile.getUser();
        profile.remotePath = nativeProfile.getRemotePath();
        profile.userPass = nativeProfile.getUserPass();
        profile.privateKeyPath = nativeProfile.getPrivateKeyPath();
        profile.privateKeyPass = nativeProfile.getPrivateKeyPass();
        profile.proxyHost = nativeProfile.getProxyHost();
        profile.proxyPort = nativeProfile.getProxyPort();
        profile.proxyType = nativeProfile.getProxyType();
        profile.promptUserPass = nativeProfile.isPromptUserPass();
        profile.promptKeyPass = nativeProfile.isPromptKeyPass();
        profile.strictHostChecking = nativeProfile.isStrictHostChecking();
        return profile;
    }

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
                    callback(options, profile);
                }
            });
        }
        else
        {
            console.log("skipped ssh user pass prompt, moving to key pass...");
            callback(options, profile);
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
                    console.log("ssh auth chain finished, invoking callback");
                    callback(options, profile);
                }
            });
        }
        else
        {
            console.log("skipped prompting ssh user pass, invoking final callback...");
            callback(options, profile);
        }
    }

}
