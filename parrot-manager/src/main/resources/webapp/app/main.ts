import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { enableProdMode } from '@angular/core';

import { AppModule } from './app.module';

// Enable production mode
console.log("production mode enabled");
enableProdMode();

// Bootstrap the app
const platform = platformBrowserDynamic();
platform.bootstrapModule(AppModule);
