package com.limpygnome.parrot.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DatabaseTest {

    // SUT
    private Database database;

    @Before
    public void setup() throws Exception
    {
        database = new Database();
    }

    @Test
    public void encryptDecryptTest() throws Exception
    {
        final String MAGIC_WORD = "hello :)";
        final byte[] MAGIC_WORD_RAW = MAGIC_WORD.getBytes("UTF-8");

        byte[] encrypted = database.encrypt(MAGIC_WORD_RAW);
        byte[] decrypted = database.decrypt(encrypted);

        String result = new String(decrypted, 0, decrypted.length, "UTF-8");
        Assert.assertEquals("Expected result to be the magic word", MAGIC_WORD, result);
    }

}
