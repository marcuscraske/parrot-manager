package com.limpygnome.parrot.service;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.Database;
import com.limpygnome.parrot.model.node.DatabaseNode;
import com.limpygnome.parrot.model.node.EncryptedAesValue;
import com.limpygnome.parrot.model.params.CryptoParams;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

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
    private CryptoParams memoryCryptoParams;
    private CryptoParams fileCryptoParams;
    byte[] iv = new byte[]{ 0x44 };
    byte[] encryptedData = new byte[]{ 0x22 };

    @Before
    public void setup() throws Exception
    {
        service = new DatabaseParserService(controller);

        memoryCryptoParams = new CryptoParams(realController, PASSWORD, CryptographyService.ROUNDS_DEFAULT);
        fileCryptoParams = new CryptoParams(realController, PASSWORD, CryptographyService.ROUNDS_DEFAULT / 2);
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
    public void saveMemoryEncrypted_whenDatabaseWithChild_thenHasExpectedJsonStructure() throws Exception
    {
        // Given
        Database database = createDatabaseWithChild();

        // When
        byte[] data = service.saveMemoryEncrypted(realController, database);

        // Then
        JSONObject json = convertToJson(data);

        // -- Assert properties of main document / JSON element
        assertEquals("Expected base64 string of salt", Base64.toBase64String(memoryCryptoParams.getSalt()), json.get("salt"));
        assertEquals("Expected rounds to be same as memory crypto rounds", memoryCryptoParams.getRounds(), (int) (long) json.get("rounds"));
        assertTrue("Expected root to have children element", json.containsKey("children"));

        // -- Assert child node
        JSONArray array = (JSONArray) json.get("children");
        assertEquals("Expected only one child node", 1, array.size());

        JSONObject jsonChildNode = (JSONObject) array.get(0);
        assertEquals("test", jsonChildNode.get("name"));
        assertEquals(1234L, jsonChildNode.get("modified"));
        assertEquals(Base64.toBase64String(iv), jsonChildNode.get("iv"));
        assertEquals(Base64.toBase64String(encryptedData), jsonChildNode.get("data"));
    }

    @Test
    public void saveFileEncrypted_whenDatabaseWithChild_thenHasExpectedJsonStructure() throws Exception
    {
        // Given
        Database database = createDatabaseWithChild();

        // When
        byte[] data = service.saveFileEncrypted(realController, database);

        // Then
        JSONObject json = convertToJson(data);

        assertEquals("Expected base64 of salt", Base64.toBase64String(fileCryptoParams.getSalt()), json.get("salt"));
        assertEquals("Expected file crypto rounds", fileCryptoParams.getRounds(), (int) (long) json.get("rounds"));
        assertTrue("Expected IV", json.containsKey("iv"));
        assertTrue("Expected (encrypted) data", json.containsKey("data"));
    }

    @Test
    public void save_expectFileExists() throws Exception
    {
        // Given
        File tmp = File.createTempFile("test", null);
        tmp.delete();

        String path = tmp.getAbsolutePath();
        Database database = createDatabaseWithChild();

        assertFalse("Expecting file to not exist at " + path, tmp.exists());

        // When
        service.save(realController, database, path);

        // Then
        assertTrue("Expecting file to exist at " + path, tmp.exists());
    }


    private JSONObject convertToJson(byte[] data) throws Exception
    {
        String rawJson = new String(data, "UTF-8");

        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(rawJson);
        return json;
    }

    private Database createDatabaseWithChild() throws Exception
    {
        Database database = createDatabase();

        EncryptedAesValue encrypted = new EncryptedAesValue(iv, encryptedData);
        DatabaseNode node = new DatabaseNode(database, "test", 1234L, encrypted);
        database.getRoot().getChildren().add(node);

        return database;
    }

    private Database createDatabase() throws Exception
    {
        Database database = service.create(memoryCryptoParams, fileCryptoParams);
        return database;
    }

}