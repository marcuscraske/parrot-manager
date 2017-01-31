import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { RemoteSshFileService } from 'app/service/remoteSshFileService.service'
import { Router } from '@angular/router';

@Component({
    moduleId: module.id,
    templateUrl: 'open-remote-ssh.component.html',
    providers: [RemoteSshFileService]
})
export class OpenRemoteSshComponent {

    public createForm = this.fb.group({
        host: ["", Validators.required],
        port: ["", Validators.required],
        strictKeyChecking : [""],
        user : ["", Validators.required],
        pass : [""],
        remotePath : ["", Validators.required],
        destinationPath : ["", Validators.required],
        privateKey : [""],
        privateKeyPass : [""],
        proxyHost : [""],
        proxyPort : [""],
        proxyType : [""]
    });

    constructor(private remoteSshFileService: RemoteSshFileService, private router: Router, public fb: FormBuilder) { }

    open()
    {
        var form = event.form;

        // Build random token for tracking download status
        var randomToken = "not so random";

        // Create download options
        var options = this.remoteSshFileService.createDownloadOptions(
            randomToken,
            form.value["host"],
            form.value["port"],
            form.value["user"],
            form.value["remotePath"],
            form.value["destinationPath"]
        );

        // Request download in promise

        this.remoteSshFileService.download(options);
    }

    resultFailure(message)
    {
    }

    resultSuccess()
    {
    }

}
