package com.limpygnome.parrot.service;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.Database;
import com.limpygnome.parrot.model.node.DatabaseNode;
import com.limpygnome.parrot.model.node.EncryptedAesValue;
import com.limpygnome.parrot.model.params.CryptoParams;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseParserServiceTest {

    private static final char[] PASSWORD = "unit test password".toCharArray();

    // SUT
    private DatabaseParserService service;

    // Mock dependencies
    @Mock
    private Controller controller;

    // Objects
    private Controller realController = new Controller();

    @Before
    public void setup()
    {
        service = new DatabaseParserService(controller);
    }

    @Test
    public void create_returnsUsableInstance() throws Exception
    {
        // Given/When
        Database database = createDatabase();

        // Then
        assertNotNull(database);
    }

    @Test
    public void saveMemoryEncrypted_whenDatabaseWithChildren_thenHasExpectedJsonStructure() throws Exception
    {
        // Given
        Database database = createDatabase();
        EncryptedAesValue encrypted = new EncryptedAesValue(new byte[]{ 0x44 }, new byte[]{ 0x22 });
        DatabaseNode node = new DatabaseNode(database, "test", 1234L, encrypted);
        database.getRoot().getChildren().add(node);

        // When
        byte[] data = service.saveMemoryEncrypted(realController, database);

        // Then
        String rawJson = new String(data, "UTF-8");

        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(rawJson);

        assertTrue(json.containsKey("salt"));
        assertTrue(json.containsKey("rounds"));
        assertTrue(json.containsKey("children"));
    }

    private Database createDatabase() throws Exception
    {
        CryptoParams cryptoParams = new CryptoParams(realController, PASSWORD, CryptographyService.ROUNDS_DEFAULT);
        Database database = service.create(cryptoParams, cryptoParams);
        return database;
    }

}