import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppComponent }     from './app.component';

import { SidebarComponent } from './sidebar/sidebar.component';
import { HomeComponent }    from './home/home.component';

@NgModule({
  imports:      [ BrowserModule ],
  declarations: [ AppComponent, SidebarComponent, HomeComponent ],
  bootstrap:    [ AppComponent ]
})

export class AppModule { }
