import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent }     from './app.component';
import { TopBarComponent } from './topbar/topbar.component'

import { ErrorComponent } from './content/error/error.component'
import { HomeComponent } from './content/home/home.component'
import { HelpComponent } from './content/help/help.component'

import { NavigatorComponent } from './content/navigator/navigator.component'
import { SidebarComponent } from './content/navigator/sidebar/sidebar.component'
import { ViewerComponent } from './content/navigator/viewer/viewer.component'

const appRoutes: Routes = [
  { path: '',       component: NavigatorComponent },
  { path: '**',     component: ErrorComponent },
  { path: 'help',  component: HelpComponent }
];

@NgModule({
  imports: [
    BrowserModule,
    RouterModule.forRoot(appRoutes)
  ],
  declarations: [
    AppComponent, TopBarComponent,
    ErrorComponent, HomeComponent, HelpComponent,
    NavigatorComponent, SidebarComponent, ViewerComponent
  ],
  bootstrap: [
    AppComponent,
    TopBarComponent
  ]
})

export class AppModule { }
