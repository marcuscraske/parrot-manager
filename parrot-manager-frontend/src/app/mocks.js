/*
    Simple JS file for mocking injected dependencies during runtime.
*/


{
    console.log("loaded mocks");

    if (window.settingsService == null)
    {
        window.settingsService = (function(){
            return {
                getSettings : function() {
                    return {
                        getRecentFilesEnabled : function() { return { getValue : function() { return "true" } } },
                        getRecentFilesOpenLastOnStartup : function() { return { getValue : function() { return "true" } } },
                        getAutomaticBackupsOnSave : function() { return { getValue : function() { return "true" } } },
                        getAutomaticBackupsRetained : function() { return { getValue : function() { return "20" } } },
                        getAutomaticBackupDelay : function() { return { getValue : function() { return "20" } } },
                        getRemoteSyncInterval : function() { return { getValue : function() { return "60" } } },
                        getRemoteSyncIntervalEnabled : function() { return { getValue : function() { return "true" } } },
                        getRemoteSyncOnOpeningDatabase : function() { return { getValue : function() { return "true" } } },
                        getRemoteSyncOnChange : function() { return { getValue : function() { return "true" } } },
                        getRemoteSyncOnChangeDelay : function() { return { getValue : function() { return "60" } } },
                        getTheme : function() { return { getValue : function() { return "" } } },
                        getSaveWindowState : function() { return { getValue : function() { return "true" } } },
                        getInactivityTimeout : function() { return { getValue : function() { return "0" } } },
                        getWipeClipboardDelay : function() { return { getValue : function() { return "0" } } },
                        getAutoSave : function() { return { getValue : function() { return "false" } } },
                        getMergeLogShowDetail : function() { return { getValue : function() { return "false" } } },
                        getKeyboardLayout : function() { return { getValue : function() { return "" } } },
                        getRemoteBackupsRetained : function() { return { getValue : function() { return "true" } } },
                        getIgnoreJavaVersion : function() { return { getValue : function() { return "false" } } }
                    }
                }
            }
        })();
    }


    if (window.randomGeneratorService == null)
    {
        window.randomGeneratorService = (function(){

            return {

                generate : function() { return "random string" }

            }

        })();
    }


    if (window.databaseService == null)
    {
        window.databaseService = (function(){

            return {
                isOpen: function() { return false; },
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
                                        getName : function() { return "child 1" },
                                        getChildren : function() { return [] },
                                        getLocalProperty : function() { return "false" }
                                    },

                                    {
                                        getId : function() { return "66aaf8d3-05fc-4bc7-afe5-d04e2cbaa26c" },
                                        getName : function() { return "child 2" },
                                        getChildren : function() { return [] },
                                        getLocalProperty : function() { return "false" }
                                    },

                                    {
                                        getId : function() { return "97245ef0-d33c-4b7c-8f2f-dba4641f713c" },
                                        getName : function() { return "child 3" },
                                        getChildren : function() { return [] },
                                        getLocalProperty : function() { return "false" }
                                    }

                                ] },
                                isRoot : function() { return false },
                                getLocalProperty : function() { return "false" },
                                getByName: function() { return null },
                                getValue: function() { return null }
                            }
                        },

                        getNode: function(nodeId) {
                            return {
                               getId : function() { return "d1b07836-83d0-486e-b329-4c41d317295d" },
                               getName : function() { return "(unnamed)" },
                               getLastModified: function() { return 1484723619 },
                               getDecryptedValueString: function() { return "mock decrypted value\ntest\nline3\nline4\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na" },
                               getChildCount: function() { return 2 },
                               getChildren : function() { return [

                                    {
                                        getId : function() { return "a1b07836-83d0-486e-b329-4c41d317295d" },
                                        getName : function() { return "child node" },
                                        getLocalProperty : function() { return "false" }
                                    },
                                    {
                                        getId : function() { return "b1b07836-83d0-486e-b329-4c41d317295d" },
                                        getName : function() { return "child node 2" },
                                        getLocalProperty : function() { return "false" }
                                    }

                               ] },
                               isRoot : function() { return false },
                               setValueString : function() {},
                               getLocalProperty : function() { return "false" },
                               getValue: function() { return null }
                           }
                        }
                    }
                }
            };

        })();

    }


    if (window.recentFileService == null)
    {
        window.recentFileService = (function(){

            return {

               fetch : function() { return []; },
               clear : function() { }

            }

        })();
    }


    if (window.encryptedValueService == null)
    {
        window.encryptedValueService = (function(){

            return {

               asString : function() { return "decrypted value test" },
               fromString : function() { }

            }

        })();
    }

    if (window.backupService == null)
    {
        window.backupService = (function(){

            return {

               create : function() { return true },
               fetch : function() { return [] },
               isBackupOpen: function() { return true; }

            }

        })();
    }

    if (window.buildInfoService == null)
    {
        window.buildInfoService = (function(){

            return {

               getBuildInfo : function() { return "fake build info" },
               isJavaOutdated: function() { return true; }

            }

        })();
    }


    if (window.runtimeService == null)
    {
        window.runtimeService = (function(){
            return {
                exit: function() { alert("tried to exit"); },
                changeHeight: function() { },
                pickFile: function() { return "test.parrot" },
                isDevelopmentMode: function() { return true; },
                refreshPage: function() { location.reload(); },
                isReady: function() { return true },
                isStandalone: function() { return true }
            }
        })();
    }

    if (window.sendKeysService == null)
    {
        window.sendKeysService = (function(){
            return {
                getKeyboardLayout: function() {
                    return {
                        getName: function() { return "test l"}
                    }
                },
                getKeyboardLayouts: function()
                {
                    return [
                        {
                            getName: function(){ return "test keyboard" }
                        }
                    ];
                }
            }
        })();
    }

    if (window.remoteSyncResultsService == null)
    {
        window.remoteSyncResultsService = (function(){
            return {
                getResults: function() { return [] },
                clear: function() { },
                getResultsAsText: function() { "test text" }
            }
        })();
    }

}
