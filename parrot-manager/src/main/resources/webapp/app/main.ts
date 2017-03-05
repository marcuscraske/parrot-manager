import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { enableProdMode } from '@angular/core';

import { AppModule } from './app.module';

// Determine if running in production mode
var isDevelopment = (window as any).developmentMode;

if (isDevelopment)
{
    console.log("development mode enabled");
}
else
{
    console.log("production mode enabled");
    enableProdMode();
}

// Bootstrap the app
const platform = platformBrowserDynamic();
platform.bootstrapModule(AppModule);
