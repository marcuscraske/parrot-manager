import { Injectable } from '@angular/core';
import { SyncSshService } from 'app/service/syncSsh.service'

@Injectable()
export class SyncProfileService
{
    syncProfileService : any;

    constructor(
        private syncSshService: SyncSshService
    ) {
        this.syncProfileService = (window as any).syncProfileService;
    }

    createTemporaryProfile(type)
    {
        return this.syncProfileService.createTemporaryProfile(type);
    }

    fetch()
    {
        var profiles = this.syncProfileService.fetch();

        // Convert to proper JSON objects
        var result = [];

        for (var i = 0; i < profiles.length; i++)
        {
            var profile = profiles[i];
            var json = this.toJson(profile);
            if (json != null)
            {
                result.push(json);
            }
        }

        return result;
    }

    fetchById(nodeId)
    {
        var profile = this.syncProfileService.fetchById(nodeId);
        var json = this.toJson(profile);
        return json;
    }

    save(profile)
    {
        this.syncProfileService.save(profile);
    }

    delete(profile)
    {
        this.syncProfileService.delete(profile.getId());
    }

    // Converts provided profile into JSON object
    toJson(profile)
    {
        var json = null;

        if (profile != null)
        {
            if (profile.getType() == "ssh")
            {
                json = this.syncSshService.toJson(profile);
            }
        }

        return json;
    }

    // Converts JSON object into profile
    toProfile(json, type)
    {
        var profile = this.createTemporaryProfile(type);

        console.log("#### profile : " + json);

        profile.setName(json["name"]);
        profile.setHost(json["host"]);
        profile.setPort(json["port"]);
        profile.setUser(json["user"]);
        profile.setRemotePath(json["remotePath"]);

        profile.setStrictHostChecking(json["strictHostChecking"]);
        profile.setUserPass(json["userPass"]);
        profile.setPrivateKeyPath(json["privateKeyPath"]);
        profile.setPrivateKeyPass(json["privateKeyPass"]);
        profile.setProxyHost(json["proxyHost"]);
        profile.setProxyPort(json["proxyPort"]);
        profile.setProxyType(json["proxyType"]);
        profile.setPromptUserPass(json["promptUserPass"]);
        profile.setPromptKeyPass(json["promptKeyPass"]);
        profile.setMachineFilter(json["machineFilter"]);

        return profile;
    }

}
