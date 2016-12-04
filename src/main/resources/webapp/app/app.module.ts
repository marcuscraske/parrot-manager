import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent }     from './app.component';
import { TopBarComponent } from './topbar/topbar.component'

import { ErrorComponent } from './content/error/error.component'
import { HomeComponent } from './content/home/home.component'
import { CreateComponent } from './content/create/create.component'
import { HelpComponent } from './content/help/help.component'

import { NavigatorComponent } from './content/navigator/navigator.component'
import { SidebarComponent } from './content/navigator/sidebar/sidebar.component'
import { ViewerComponent } from './content/navigator/viewer/viewer.component'

const appRoutes: Routes = [
  { path: 'home',                                   component: NavigatorComponent },
  { path: 'viewer',                                 component: NavigatorComponent },
  { path: 'create',                                 component: CreateComponent },

  { path: 'help',                                   component: HelpComponent },
  { path: 'navigator',                              component: NavigatorComponent },
  { path: '**',                                     component: ErrorComponent }
];

@NgModule({
  imports: [
    BrowserModule,
    ReactiveFormsModule,
    RouterModule.forRoot(appRoutes)
  ],
  declarations: [
    AppComponent, TopBarComponent,
    ErrorComponent, HomeComponent, CreateComponent, HelpComponent,
    NavigatorComponent, SidebarComponent, ViewerComponent
  ],
  bootstrap: [
    AppComponent,
    TopBarComponent
  ]
})

export class AppModule { }
