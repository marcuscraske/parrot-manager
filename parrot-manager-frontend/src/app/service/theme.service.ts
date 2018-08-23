import { Injectable } from '@angular/core';

@Injectable()
export class ThemeService
{

    set(name)
    {
        // determine path to new theme css resource
        var href = (name != null && name.length > 0 ? "assets/themes/" + name + ".css" : "");

        // update path
        var themeStylesheet = $("#theme");

        if (themeStylesheet.length == 0)
        {
            $("head").append("<link id='theme' rel='stylesheet' type='text/css' />");
        }

        $("#theme").attr("href", href);

        console.log("updated theme - name: " + name + ", href: " + href);
    }

}
