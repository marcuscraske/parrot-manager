/*
    Simple JS file for mocking injected dependencies during Parrot runtime.
*/

if (window.runtimeService == null)
{
    window.runtimeService = (function(){
        return {
            exit : function() { alert("tried to exit"); },
            changeHeight: function() { }
        }
    })();
}

if (window.databaseService == null)
{
    window.databaseService = (function(){

        return {
            isOpen: function() { return true; },
            getFileName: function() { return "test.parrot"; },
            create: function() { return true; }
        };

    })();

}
