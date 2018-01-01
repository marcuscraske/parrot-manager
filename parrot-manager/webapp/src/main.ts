import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';

console.log("loading app...");

// set environment
if (environment.production)
{
    console.log("running in production");

    enableProdMode();
}
else
{
    console.log("running in dev mode");
}

// wait for global services to be injected variable
var handle = setInterval(() => {

    console.log("checking if runtime ready...");

    var win = (window as any);
    if (win.runtimeService != null && win.runtimeService.isReady())
    {
        console.log("runtime ready, bootstrapping app");

        // load application
        platformBrowserDynamic().bootstrapModule(AppModule);

        // stop interval
        clearInterval(handle);
    }

}, 100);
