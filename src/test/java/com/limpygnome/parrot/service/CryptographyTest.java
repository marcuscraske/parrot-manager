package com.limpygnome.parrot.service;

import com.limpygnome.parrot.model.db.EncryptedAesValue;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CryptographyTest {

    // SUT
    private CryptographyService service;

    @Before
    public void setup() throws Exception
    {
        service = new CryptographyService();
    }

    @Test
    public void encryptDecryptTest1() throws Exception
    {
        // We expect empty string in this instance...
        encryptDecryptTest(null);
    }

    @Test
    public void encryptDecryptTest2() throws Exception
    {
        encryptDecryptTest("");
    }

    @Test
    public void encryptDecryptTest3() throws Exception
    {
        encryptDecryptTest("hello :) 123456789 foo bar test string");
    }

    @Test
    public void encryptDecryptTest4() throws Exception
    {
        // 500 chars test
        encryptDecryptTest("N8eIeLcuL7uhBukF8HAR1tS22IR5e307lUBZxINuvHED9FmY2DYwAfVgWVJOpLQr7TPbY5HD51q1hfHvmkCka3M0JZGuQzNfTLwhbcUHLOcPaTkG8U6BWAlBz0zohLZ3g6Po9vhMIGFwiCetDXLA0FVIwr07ENL10vCRVtO68z1O87qXMf3HFxhAE8RNOiS0Oq2Kv1miWtnDHrgmeje1ShpX6ZtPjtkwZVJBOe1nDUyHsYG5yr5nHPPCXNxRrl9cyPevvQ7JYXwht0X2lsPaZJN6AQ7QHUvTcb9W6tSUpnmt0NuaKj65OP1BZtwn26Cl9RQWge0tCuU31ZDFzFAPTSSqsbNCCsAJT20QJybYoZcqGJpOjvT2JikhnXF1wmBklEUNjRYeiHqTawAt4v0VHNooNg79Ehh9Qi9ky2IAgROABKvUt6JtrqCrSJYAWpVe5qPO2fFyDRBh5pagCCXX3pWFGPDkUOz2p7U77t26sV90NmV0K1hX");
    }

    private void encryptDecryptTest(final String MAGIC_WORD) throws Exception
    {
        final byte[] MAGIC_WORD_RAW = (MAGIC_WORD != null ? MAGIC_WORD.getBytes("UTF-8") : null);

        SecretKey secretKey = service.createSecretKey("test password".toCharArray(), service.generateRandomSalt(), CryptographyService.ROUNDS_DEFAULT);

        EncryptedAesValue encrypted = service.encrypt(secretKey, MAGIC_WORD_RAW);
        System.out.println("encrypted: " + Base64.getEncoder().encode(encrypted.getValue()) + " (iv: " + Base64.getEncoder().encode(encrypted.getIv()) + ")");

        byte[] decrypted = service.decrypt(secretKey, encrypted);
        System.out.println("decrypted: " + Base64.getEncoder().encode(decrypted));

        String result = new String(decrypted, 0, decrypted.length, "UTF-8");
        System.out.println("decrypted str: " + result);

        Assert.assertEquals("Expected result to be the magic word", MAGIC_WORD == null ? "" : MAGIC_WORD, result);
    }

}
