package com.limpygnome.parrot.event;

public interface DatabaseChangingEvent
{

    /**
     * Invoked when the database is changed.
     */
    void eventDatabaseChanged(boolean open);

}
