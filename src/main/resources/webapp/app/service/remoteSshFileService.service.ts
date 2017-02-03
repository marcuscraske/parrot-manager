import { Injectable } from '@angular/core';

@Injectable()
export class RemoteSshFileService {

    remoteSshFileService : any;

    constructor()
    {
        this.remoteSshFileService = (window as any).remoteSshFileService;
    }

    createOptions(randomToken, name, host, port, user, remotePath, destinationPath)
    {
        var options = this.remoteSshFileService.createOptions(randomToken, name, host, port, user, remotePath, destinationPath);
        return options;
    }

    createOptionsFromNode(node)
    {
        var options = this.remoteSshFileService.createOptionsFromNode(node);
        return options;
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
