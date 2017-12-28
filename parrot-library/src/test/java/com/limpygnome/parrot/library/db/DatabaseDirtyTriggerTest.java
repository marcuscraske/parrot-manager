package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.event.DatabaseDirtyEventHandler;
import com.limpygnome.parrot.library.event.EventHandlerCollection;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseDirtyTriggerTest
{
    @InjectMocks
    private Database database;

    @Mock
    private DatabaseDirtyEventHandler dirtyEventHandler;

    @Test
    public void getDirtyEventHandlers_isInstance()
    {
        // when
        EventHandlerCollection<DatabaseDirtyEventHandler> collection = database.getDirtyEventHandlers();

        // then
        assertNotNull(collection);
    }

    @Test
    public void setDirty_whenTrue_handlerParamsAsExpected()
    {
        // given
        database.getDirtyEventHandlers().add(dirtyEventHandler);

        // when
        database.setDirty(true);

        // then
        verify(dirtyEventHandler).eventDatabaseDirtyEventHandler(database, true);
    }

    @Test
    public void setDirty_whenFalse_handlerParamsAsExpected()
    {
        // given
        database.getDirtyEventHandlers().add(dirtyEventHandler);

        // when
        database.setDirty(false);

        // then
        verify(dirtyEventHandler).eventDatabaseDirtyEventHandler(database, false);
    }

}
