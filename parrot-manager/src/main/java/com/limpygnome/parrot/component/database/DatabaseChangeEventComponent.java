package com.limpygnome.parrot.component.database;

import com.limpygnome.parrot.component.ui.WebStageInitService;
import com.limpygnome.parrot.event.DatabaseChangingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Used to raise DOM events for when the database is changing.
 *
 * This should run last, so that the UI is ready for any major changes.
 */
@Order(99999)
@Component
public class DatabaseChangeEventComponent implements DatabaseChangingEvent
{
    @Autowired
    private WebStageInitService webStageInitService;

    @Override
    public void eventDatabaseChanged(boolean open)
    {
        webStageInitService.triggerEvent("document", "database." + (open ? "open" : "closed"), null);
    }

}
