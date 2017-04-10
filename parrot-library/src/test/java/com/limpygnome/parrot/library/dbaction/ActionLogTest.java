package com.limpygnome.parrot.library.dbaction;

import com.limpygnome.parrot.library.db.DatabaseNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ActionLogTest
{

    // SUT
    private ActionLog actionLog;

    // Mock values
    @Mock
    private DatabaseNode databaseNode;

    @Before
    public void setup()
    {
        actionLog = new ActionLog();
    }

    @Test
    public void getMessages_correctFormat()
    {
        // Given
        given(databaseNode.getPath()).willReturn("test node path");


        // When
        actionLog.add("test message");
        actionLog.add(databaseNode, "db node message");

        String result = actionLog.getMessages();

        // Then
        String separator = System.getProperty("line.separator");
        String expected = "test message" + separator + "test node path : db node message";

        assertEquals("Not in expected format", expected, result);
    }

}