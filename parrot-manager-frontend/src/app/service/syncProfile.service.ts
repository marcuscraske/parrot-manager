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
        var nativeProfile = this.syncProfileService.fetchById(nodeId);
        var profile = this.toJson(nativeProfile);
        return profile;
    }

    save(profile)
    {
        var nativeProfile = this.toNative(profile);
        this.syncProfileService.save(nativeProfile);
    }

    delete(profileId)
    {
        this.syncProfileService.delete(profileId);
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

        if (json == null)
        {
            console.error("toJson - unhandled profile type: " + (profile != null ? profile.type : "(null profile)"));
        }

        return json;
    }

    // Converts JSON object into profile
    toNative(json)
    {
        var profile = null;

        if (json != null)
        {
            if (json.type == "ssh")
            {
                profile = this.syncSshService.toNative(json);
            }
        }

        return profile;
    }

}
