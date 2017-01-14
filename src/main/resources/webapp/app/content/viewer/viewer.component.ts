import { Component } from '@angular/core';
@Component({
    moduleId: module.id,
    selector: 'viewer',
    templateUrl: 'viewer.component.html',
    styleUrls: ['viewer.component.css']
})
export class ViewerComponent {

    constructor()
    {
        var data = [
            {
                name: 'node1', id: 1,
                children: [
                    { name: 'child1', id: 2 },
                    { name: 'child2', id: 3 }
                ]
            },
            {
                name: 'node2', id: 4,
                children: [
                    { name: 'child3', id: 5 }
                ]
            }
        ];

        $(function(){

            $('#tree1').jstree({
                core: {
                    check_callback: true
                },
                dnd : {
                },
                plugins: [ "dnd" ]
            });

        });
    }

}
