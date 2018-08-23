import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';

console.log("loading app...");

// rewrite url to root /
// TODO only needed due to bug in angular cli 1.7 beta
window.history.pushState({}, 'home', '/');

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

// startup app
try
{
    platformBrowserDynamic().bootstrapModule(AppModule);
}
catch (e)
{
    console.error(e);
}
