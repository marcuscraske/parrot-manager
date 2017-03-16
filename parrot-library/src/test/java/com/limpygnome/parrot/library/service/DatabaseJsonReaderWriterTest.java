package com.limpygnome.parrot.library.service;

//@RunWith(MockitoJUnitRunner.class)
public class DatabaseJsonReaderWriterTest
{

//    private static final char[] PASSWORD = "unit test password".toCharArray();
//    private static final long LAST_MODIFIED = 1234L;
//
//    @Spy
//    private CryptographyService cryptographyService;
//
//    // Mock objects
//    @Spy
//    @InjectMocks
//    private Controller controller;
//
//    // SUT
//    @InjectMocks
//    private DatabaseReaderWriter service;
//
//    // Objects
//    private CryptoParams memoryCryptoParams;
//    private CryptoParams fileCryptoParams;
//    private byte[] iv = new byte[]{ 0x44 };
//    private byte[] encryptedData = new byte[]{ 0x22 };
//
//    @Before
//    public void setup() throws Exception
//    {
//        memoryCryptoParams = new CryptoParams(controller, PASSWORD, CryptographyService.ROUNDS_DEFAULT, LAST_MODIFIED);
//        fileCryptoParams = new CryptoParams(controller, PASSWORD, CryptographyService.ROUNDS_DEFAULT / 2, LAST_MODIFIED);
//    }
//
//    @Test
//    public void create_returnsUsableInstance() throws Exception
//    {
//        // Given/When
//        Database database = createDatabase();
//
//        // Then
//        assertNotNull(database);
//    }
//
//    @Test
//    public void saveMemoryEncrypted_whenDatabaseWithChild_thenHasExpectedRootStructure() throws Exception
//    {
//        // Given
//        Database database = createDatabaseWithChildren();
//
//        // When
//        byte[] data = service.saveMemoryEncrypted(controller, database);
//
//        // Then
//        JSONObject json = convertToJson(data);
//
//        // -- Assert properties of main document / JSON element
//        assertEquals("Expected base64 string of salt", Base64.toBase64String(memoryCryptoParams.getSalt()), json.get("cryptoParams.salt"));
//        assertEquals("Expected rounds to be same as memory crypto rounds", memoryCryptoParams.getRounds(), (int) (long) json.get("cryptoParams.rounds"));
//        assertEquals("Expected last modified to be same as memory crypto last modified", memoryCryptoParams.getLastModified(), (long) json.get("cryptoParams.modified"));
//        assertTrue("Expected root to have children element", json.containsKey("children"));
//    }
//
//    @Test
//    public void saveMemoryEncrypted_whenDatabaseWithChildren_thenHasExpectedSubChildStructure() throws Exception
//    {
//        // Given
//        Database database = createDatabaseWithChildren();
//
//        // When
//        byte[] data = service.saveMemoryEncrypted(controller, database);
//
//        // Then
//        JSONObject json = convertToJson(data);
//
//        JSONArray array = (JSONArray) json.get("children");
//        assertEquals("Expected only one child db", 1, array.size());
//
//        // -- Check child under root
//        JSONObject jsonChildNode = (JSONObject) array.get(0);
//        assertEquals("test", jsonChildNode.get("name"));
//        assertEquals(1234L, jsonChildNode.get("modified"));
//        assertEquals(Base64.toBase64String(iv), jsonChildNode.get("iv"));
//        assertEquals(Base64.toBase64String(encryptedData), jsonChildNode.get("data"));
//    }
//
//    @Test
//    public void saveMemoryEncrypted_whenDatabaseWithChildren_thenHasExpectedSubChildOfChildStructure() throws Exception
//    {
//        // Given
//        Database database = createDatabaseWithChildren();
//
//        // When
//        byte[] data = service.saveMemoryEncrypted(controller, database);
//
//        // Then
//        JSONObject json = convertToJson(data);
//
//        JSONArray array = (JSONArray) json.get("children");
//        JSONObject firstChild = (JSONObject) array.get(0);
//
//        // -- Check child under child under under root
//        JSONArray arrayOfChild = (JSONArray) firstChild.get("children");
//
//        JSONObject jsonChildNode = (JSONObject) arrayOfChild.get(0);
//        assertEquals("test2", jsonChildNode.get("name"));
//        assertEquals(9876L, jsonChildNode.get("modified"));
//        assertEquals(Base64.toBase64String(iv), jsonChildNode.get("iv"));
//        assertEquals(Base64.toBase64String(encryptedData), jsonChildNode.get("data"));
//    }
//
//    @Test
//    public void saveFileEncrypted_whenDatabaseWithChildren_thenHasExpectedJsonStructure() throws Exception
//    {
//        // Given
//        Database database = createDatabaseWithChildren();
//
//        // When
//        byte[] data = service.saveFileEncrypted(controller, database);
//
//        // Then
//        JSONObject json = convertToJson(data);
//
//        assertEquals("Expected base64 of salt", Base64.toBase64String(fileCryptoParams.getSalt()), json.get("cryptoParams.salt"));
//        assertEquals("Expected file crypto rounds", fileCryptoParams.getRounds(), (int) (long) json.get("cryptoParams.rounds"));
//        assertEquals("Expected modified epoch", fileCryptoParams.getLastModified(), (long) json.get("cryptoParams.modified"));
//        assertTrue("Expected IV", json.containsKey("iv"));
//        assertTrue("Expected (encrypted) data", json.containsKey("data"));
//    }
//
//    @Test
//    public void save_whenDatabaseWithChildren_thenExpectFileExists() throws Exception
//    {
//        // Given
//        File tmp = File.createTempFile("test", null);
//        tmp.delete();
//
//        String path = tmp.getAbsolutePath();
//        Database database = createDatabaseWithChildren();
//
//        assertFalse("Expecting file to not exist at " + path, tmp.exists());
//
//        // When
//        service.save(controller, database, path);
//
//        // Then
//        assertTrue("Expecting file to exist at " + path, tmp.exists());
//    }
//
//    @Test(expected = IOException.class)
//    public void save_whenDatabaseWithChildrenButPathNonExistent_thenThrowsIOException() throws Exception
//    {
//        // Given
//        File nonExistentPath = new File("/non-existent/path/qwertyuiop");
//        assertFalse(nonExistentPath.exists());
//
//        Database database = createDatabaseWithChildren();
//
//        // When
//        service.save(controller, database, nonExistentPath.getAbsolutePath());
//    }
//
//    @Test
//    public void openMemoryEncrypted_worksAsExpected() throws Exception
//    {
//        // Given
//        Database database = createDatabaseWithChildren();
//        byte[] memoryEncrypted = service.saveMemoryEncrypted(controller, database);
//
//        // When
//        Database databaseOpened = service.openMemoryEncrypted(memoryEncrypted, PASSWORD, fileCryptoParams);
//
//        // Then
//        assertEquals("Databases should be exactly the same", databaseOpened, database);
//    }
//
//    @Test
//    public void openFileEncrypted_worksAsExpected() throws Exception
//    {
//        // Given
//        Database database = createDatabaseWithChildren();
//        byte[] fileEncrypted = service.saveFileEncrypted(controller, database);
//
//        // When
//        Database databaseOpened = service.openFileEncrypted(controller, fileEncrypted, PASSWORD);
//
//        // Then
//        assertEquals("Databases should be exactly the same", databaseOpened, database);
//    }
//
//    @Test
//    public void open_worksAsExpected() throws Exception
//    {
//        // Given
//        File tmp = File.createTempFile("test", null);
//        tmp.delete();
//
//        String path = tmp.getAbsolutePath();
//        Database database = createDatabaseWithChildren();
//
//        assertFalse("Expecting file to not exist at " + path, tmp.exists());
//        service.save(controller, database, path);
//        assertTrue("Expecting file to exist at " + path, tmp.exists());
//
//        // When
//        Database databaseOpened = service.open(controller, path, PASSWORD);
//
//        // Then
//        assertEquals("Databases should be exactly the same", databaseOpened, database);
//    }
//
//
//    private JSONObject convertToJson(byte[] data) throws Exception
//    {
//        String rawJson = new String(data, "UTF-8");
//
//        JSONParser jsonParser = new JSONParser();
//        JSONObject json = (JSONObject) jsonParser.parse(rawJson);
//        return json;
//    }
//
//    private Database createDatabaseWithChildren() throws Exception
//    {
//        Database database = createDatabase();
//
//        // Add some deleted children to root
//        DatabaseNode nodeRoot = database.getRoot();
//        nodeRoot.getDeletedChildren().add(UUID.randomUUID());
//
//        // Add child under root
//        EncryptedAesValue encrypted = new EncryptedAesValue(iv, encryptedData);
//        DatabaseNode node = new DatabaseNode(database, UUID.randomUUID(), "test", 1234L, encrypted);
//        node.getDeletedChildren().add(UUID.randomUUID());
//        node.getDeletedChildren().add(UUID.randomUUID());
//        nodeRoot.add(node);
//
//
//        // Add a child to our child...
//        DatabaseNode node2 = new DatabaseNode(database, UUID.randomUUID(), "test2", 9876L, encrypted);
//        node2.getDeletedChildren().add(UUID.randomUUID());
//        node2.getDeletedChildren().add(UUID.randomUUID());
//        node2.getDeletedChildren().add(UUID.randomUUID());
//        node.add(node2);
//
//        return database;
//    }
//
//    private Database createDatabase() throws Exception
//    {
//        Database database = service.create(memoryCryptoParams, fileCryptoParams);
//        return database;
//    }

}