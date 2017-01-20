/*
    Simple JS file for mocking injected dependencies during Parrot runtime.
*/

if (window.runtimeService == null)
{
    window.runtimeService = (function(){
        return {
            exit: function() { alert("tried to exit"); },
            changeHeight: function() { },
            pickFile: function() { return "test.parrot" }
        }
    })();
}

if (window.databaseService == null)
{
    window.databaseService = (function(){

        return {
            isOpen: function() { return true; },
            isDirty: function() { return true; },
            getFileName: function() { return "test.parrot"; },
            create: function() { return true; },
            open: function() { return "test error"; },
            save: function() { return "test save error"; },
            close: function() { },
            getDatabase: function()
            {
                return {
                    getRoot : function() {
                        return {
                            getId : function() { return "d1b07836-83d0-486e-b329-4c41d317295d" },
                            getName : function() { return "hello" },
                            getChildren : function() { return [

                                {
                                    getId : function() { return "a1b07836-83d0-486e-b329-4c41d317295d" },
                                    getName : function() { return "hello" },
                                    getChildren : function() { return [] }
                                }

                            ] }
                        }
                    },

                    getNode: function(nodeId) {
                        return {
                           getId : function() { return "d1b07836-83d0-486e-b329-4c41d317295d" },
                           getName : function() { return "hello" },
                           getLastModified: function() { return 1484723619 },
                           getFormattedLastModified: function() { return "XX-XX-XXXX XX:XX:XX" },
                           getDecryptedValueString: function() { return "mock decrypted value" },
                           getChildCount: function() { return 2 },
                           getChildren : function() { return [

                                {
                                    getId : function() { return "a1b07836-83d0-486e-b329-4c41d317295d" },
                                    getName : function() { return "child node" }
                                },
                                {
                                    getId : function() { return "b1b07836-83d0-486e-b329-4c41d317295d" },
                                    getName : function() { return "child node 2" }
                                }

                           ] },
                           isRoot : function() { return true }
                       }
                    }
                }
            }
        };

    })();

}
