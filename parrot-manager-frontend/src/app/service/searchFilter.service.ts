import { Injectable } from '@angular/core';

import "app/global-vars"

@Injectable()
export class SearchFilterService
{

    /*
        Filters the database JSON by the provided name.
    */
    filterByName(json, name)
    {
        // rebuild new single-level tree with discovered items
        var result = [];
        this.filterByNameRecursive(result, name, json);
        console.log(JSON.stringify(result));
        return result;
    }

    private filterByNameRecursive(result, name, currentElements)
    {
        for (var i = 0; i < currentElements.length; i++)
        {
            var element = currentElements[i];
            var children = element.children;
            var elementName = element.text;

            // check if name matches filter
            if (elementName != null && elementName.includes(name))
            {
                // wipe child elements and append
                element.children = [];
                result.push(element);
            }

            // search child nodes
            if (children != null && children.length > 0)
            {
                this.filterByNameRecursive(result, name, children);
            }
        }
    }

}
