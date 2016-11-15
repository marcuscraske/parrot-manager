import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppComponent }     from './app.component';

import { TopBarComponent } from './topbar/topbar.component'
import { SidebarComponent } from './sidebar/sidebar.component'
import { HomeComponent } from './home/home.component'

@NgModule({
  imports:      [ BrowserModule ],
  declarations: [ AppComponent, TopBarComponent, SidebarComponent, HomeComponent ],
  bootstrap:    [ AppComponent, TopBarComponent, SidebarComponent, HomeComponent ]
})

export class AppModule { }
