package com.limpygnome.parrot.component.database;

import com.limpygnome.parrot.component.settings.SettingsService;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.event.DatabaseDirtyEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Auto-saves database when dirty, if enabled as global setting.
 */
@Component
public class DatabaseAutoSaveHandler implements DatabaseDirtyEventHandler
{
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private SettingsService settingsService;

    @Override
    public synchronized void eventDatabaseDirtyEventHandler(Database database, boolean dirty)
    {
        if (dirty)
        {
            boolean autoSave = settingsService.getSettings().getAutoSave().getSafeBoolean(true);

            if (autoSave)
            {
                // attempt to save; errors may be hidden, but database will remain dirty
                databaseService.save();
            }
        }
    }

}
