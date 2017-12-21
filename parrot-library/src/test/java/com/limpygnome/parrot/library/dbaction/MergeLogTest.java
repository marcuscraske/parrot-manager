package com.limpygnome.parrot.library.dbaction;

import com.limpygnome.parrot.library.db.DatabaseNode;
import com.limpygnome.parrot.library.db.MergeLog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class MergeLogTest
{

    // SUT
    private MergeLog mergeLog;

    // Mock values
    @Mock
    private DatabaseNode databaseNode;

    @Before
    public void setup()
    {
        mergeLog = new MergeLog();
    }

    @Test
    public void getMessages_correctFormat()
    {
        // Given
        given(databaseNode.getPath()).willReturn("test node path");


        // When
        mergeLog.add("test message");
        mergeLog.add(databaseNode, "db node message");

        String result = mergeLog.getMessages("host");

        // Then
        String separator = System.getProperty("line.separator");
        String expected = "host : test message" + separator + "host : test node path : db node message";

        assertEquals("Not in expected format", expected, result);
    }

}