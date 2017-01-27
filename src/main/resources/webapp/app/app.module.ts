import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent }     from './app.component';
import { TopBarComponent } from './topbar/topbar.component'

import { ErrorComponent } from './content/error/error.component'
import { HomeComponent } from './content/home/home.component'
import { CreateComponent } from './content/create/create.component'
import { OpenComponent } from './content/open/open.component'
import { HelpComponent } from './content/help/help.component'

import { ViewerComponent } from './content/viewer/viewer.component'
import { GenerateRandomComponent } from './content/viewer/generate-random/generate-random.component'

const appRoutes: Routes = [
  { path: '',                                       component: ViewerComponent },
  { path: 'open',                                   component: OpenComponent },
  { path: 'create',                                 component: CreateComponent },

  { path: 'viewer',                                 component: ViewerComponent },

  { path: 'help',                                   component: HelpComponent },
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
    ErrorComponent, HomeComponent, CreateComponent, OpenComponent, HelpComponent,
    ViewerComponent, GenerateRandomComponent
  ],
  bootstrap: [
    AppComponent
  ]
})

export class AppModule { }
