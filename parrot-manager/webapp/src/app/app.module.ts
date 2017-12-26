// AngularJS
import { NgModule, ErrorHandler }   from '@angular/core';
import { BrowserModule }            from '@angular/platform-browser';
import { ReactiveFormsModule }      from '@angular/forms';
import { RouterModule, Routes }     from '@angular/router';

// Global
import { ErrorWatcherHandler }      from 'app/error-watcher-handler';
import { AppComponent }             from 'app/app.component';
import { TopBarComponent }          from 'app/component/topbar/topbar.component'
import { NotificationsComponent }   from 'app/component/notifications/notifications.component'

// Modules
import { PipesModule }              from 'app/component/pipes/pipes.module'
import { SettingsModule }           from 'app/component/pages/settings/settings.module'

// Pages
import { ErrorComponent }           from 'app/component/pages/error/error.component'
import { CreateComponent }          from 'app/component/pages/create/create.component'
import { HelpComponent }            from 'app/component/pages/help/help.component'
import { OpenComponent }            from 'app/component/pages/open/open.component'

// Pages - opened database
import { ViewerComponent }          from 'app/component/pages/viewer/viewer.component'
import { SearchBoxComponent }       from 'app/component/pages/viewer/search-box/search-box.component'
import { GenerateRandomComponent }  from 'app/component/pages/viewer/generate-random/generate-random.component'
import { CurrentEntryComponent }    from 'app/component/pages/viewer/current-entry/current-entry.component'
import { ViewerEntriesComponent }   from 'app/component/pages/viewer/entries/entries.component'
import { ToggleValueComponent }     from 'app/component/pages/viewer/toggle-value/toggleValue.component'
import { CopyClipboardComponent }   from 'app/component/pages/viewer/copy-clipboard/copyClipboard.component'
import { SendKeysComponent }        from 'app/component/pages/viewer/send-keys/sendKeys.component'
import { HistoryComponent }         from 'app/component/pages/viewer/history/history.component'

import { RemoteSyncComponent }      from 'app/component/pages/remote-sync/remote-sync.component'
import { RemoteSyncSshComponent }   from 'app/component/pages/remote-sync-ssh/remote-sync-ssh.component'
import { BackupsComponent }         from 'app/component/pages/backups/backups.component'

const appRoutes: Routes = [
  { path: '',                                       component: OpenComponent },
  { path: 'open',                                   component: OpenComponent },
  { path: 'create',                                 component: CreateComponent },

  { path: 'viewer',                                 component: ViewerComponent },
  { path: 'remote-sync',                            component: RemoteSyncComponent },
  { path: 'remote-sync/ssh',                        component: RemoteSyncSshComponent },
  { path: 'remote-sync/ssh/:currentNode',           component: RemoteSyncSshComponent },
  { path: 'backups',                                component: BackupsComponent },

  { path: 'help',                                   component: HelpComponent },
  { path: '**',                                     component: ErrorComponent }
];

@NgModule({
  providers: [
    { provide: ErrorHandler, useClass: ErrorWatcherHandler }
  ],
  imports: [
    BrowserModule,
    ReactiveFormsModule,
    RouterModule.forRoot(appRoutes),
    SettingsModule,
    PipesModule
  ],
  declarations: [

    // Global
    AppComponent, TopBarComponent, NotificationsComponent,

    // Pages
    ErrorComponent,
    CreateComponent, OpenComponent, HelpComponent,
    RemoteSyncComponent, RemoteSyncSshComponent,
    BackupsComponent,

    // Pages - viewer (and components)
    // TODO: try to put most of this into a module
    ViewerComponent, SearchBoxComponent,
    GenerateRandomComponent, CurrentEntryComponent, ViewerEntriesComponent, HistoryComponent,
    ToggleValueComponent, CopyClipboardComponent, SendKeysComponent

  ],
  bootstrap: [
    AppComponent
  ]
})

export class AppModule { }
