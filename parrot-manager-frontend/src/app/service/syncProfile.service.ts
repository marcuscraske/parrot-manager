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
        var profileId = profile.id;
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

        return json;
    }

    // Converts JSON object into profile
    toProfile(json, type)
    {
        var profile = null;

        if (type == "ssh")
        {
            profile = this.syncSshService.toProfile(json, type);
        }

        return profile;
    }

}
