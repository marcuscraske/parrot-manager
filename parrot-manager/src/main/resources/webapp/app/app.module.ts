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
// TODO: drop home component? not even used anymore?
import { HomeComponent }            from 'app/content/home/home.component'
import { CreateComponent }          from 'app/content/create/create.component'
import { HelpComponent }            from 'app/content/help/help.component'
import { OpenComponent }            from 'app/content/open/open.component'
import { SettingsComponent }        from 'app/content/settings/settings.component'

// Pages - opened database
import { ViewerComponent }          from 'app/content/viewer/viewer.component'
import { GenerateRandomComponent }  from 'app/content/viewer/generate-random/generate-random.component'
import { ViewerEntriesComponent }   from 'app/content/viewer/entries/entries.component'
import { RemoteSyncComponent }      from 'app/content/remote-sync/remote-sync.component'
import { RemoteSyncSshComponent }   from 'app/content/remote-sync-ssh/remote-sync-ssh.component'
import { BackupsComponent }         from 'app/content/backups/backups.component'

const appRoutes: Routes = [
  { path: '',                                       component: OpenComponent },
  { path: 'open',                                   component: OpenComponent },
  { path: 'create',                                 component: CreateComponent },
  { path: 'settings',                               component: SettingsComponent },

  { path: 'viewer',                                 component: ViewerComponent },
  { path: 'remote-sync',                            component: RemoteSyncComponent },
  { path: 'remote-sync/ssh',                        component: RemoteSyncSshComponent },
  { path: 'remote-sync/ssh/:currentNode',           component: RemoteSyncSshComponent },
  { path: 'backups',                                component: BackupsComponent },

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
    ErrorComponent, HomeComponent,
    CreateComponent, OpenComponent, HelpComponent, SettingsComponent,

    // Pages - opened database
    ViewerComponent, GenerateRandomComponent, ViewerEntriesComponent,
    RemoteSyncComponent, RemoteSyncSshComponent,
    BackupsComponent

  ],
  bootstrap: [
    AppComponent
  ]
})

export class AppModule { }