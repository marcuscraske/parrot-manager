import { Injectable } from '@angular/core';

@Injectable()
export class RemoteSshFileService {

    remoteSshFileService : any;

    constructor()
    {
        this.remoteSshFileService = (window as any).remoteSshFileService;
    }

    createDownloadOptions(randomToken, host, port, user, remotePath, destinationPath)
    {
        return this.remoteSshFileService.createDownloadOptions(randomToken, host, port, user, remotePath, destinationPath);
    }

    getStatus(randomToken)
    {
        var result = this.remoteSshFileService.getStatus(randomToken);
        return result;
    }

    download(options)
    {
        var result = this.remoteSshFileService.download(options);
        return result;
    }

}
