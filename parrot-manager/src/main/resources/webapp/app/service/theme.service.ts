import { Injectable } from '@angular/core';

@Injectable()
export class ThemeService
{

    set(name)
    {
        // determine path to new theme css resource
        var href = (name != null && name.length > 0 ? "assets/" + name + "-theme.css" : "";

        // update path
        $("#theme").attr("href", href);

        console.log("updated theme - name: " + name + ", href: " + href);
    }

}