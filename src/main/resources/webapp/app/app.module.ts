// AngularJS
import { NgModule }                 from '@angular/core';
import { BrowserModule }            from '@angular/platform-browser';
import { ReactiveFormsModule }      from '@angular/forms';
import { RouterModule, Routes }     from '@angular/router';

// Global
import { AppComponent }             from 'app/app.component';
import { TopBarComponent }          from 'app/topbar/topbar.component'

// Pages
import { ErrorComponent }           from 'app/content/error/error.component'
import { HomeComponent }            from 'app/content/home/home.component'
import { CreateComponent }          from 'app/content/create/create.component'
import { OpenComponent }            from 'app/content/open/open.component'
import { OpenRemoteSshComponent }   from 'app/content/open-remote-ssh/open-remote-ssh.component'
import { HelpComponent }            from 'app/content/help/help.component'

// Viewer
import { ViewerComponent }          from 'app/content/viewer/viewer.component'
import { GenerateRandomComponent }  from 'app/content/viewer/generate-random/generate-random.component'
import { ViewerEntriesComponent }   from 'app/content/viewer/entries/entries.component'

const appRoutes: Routes = [
  { path: '',                                       component: OpenComponent },
  { path: 'open',                                   component: OpenComponent },
  { path: 'open/remote/ssh',                        component: OpenRemoteSshComponent },
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

    // Global
    AppComponent, TopBarComponent,

    // Pages
    ErrorComponent, HomeComponent, CreateComponent, OpenComponent, OpenRemoteSshComponent, HelpComponent,

    // Viewer
    ViewerComponent, GenerateRandomComponent, ViewerEntriesComponent

  ],
  bootstrap: [
    AppComponent
  ]
})

export class AppModule { }
