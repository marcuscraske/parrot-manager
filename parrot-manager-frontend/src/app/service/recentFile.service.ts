import { Injectable } from '@angular/core';
import { RecentFile } from 'app/model/recentFile'

@Injectable()
export class RecentFileService {

    recentFileService : any;

    constructor()
    {
        this.recentFileService = (window as any).recentFileService;
    }

    fetch() : RecentFile[]
    {
        var results = [];
        var files = this.recentFileService.fetch();
        if (files != null && files.length > 0)
        {
            for (var i = 0; i < files.length; i++)
            {
                var file = files[i];
                var result = new RecentFile(file.getFileName(), file.getFullPath());
                results.push(result);
            }
        }
        return results;
    }

    isAny() : boolean
    {
        var recentFiles = this.fetch();
        return recentFiles.length > 0;
    }

    delete(path)
    {
        this.recentFileService.delete(path);
    }

    clear()
    {
        this.recentFileService.clear();
    }

}
