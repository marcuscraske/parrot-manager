package com.limpygnome.parrot.library.event;

import com.limpygnome.parrot.library.db.Database;

public interface DatabaseDirtyEventHandler
{

    /**
     * Invoked when database is dirty due to changes.
     *
     * @param database database instance
     * @param dirty indicates whether database is now dirty
     */
    void eventDatabaseDirtyEventHandler(Database database, boolean dirty);

}
