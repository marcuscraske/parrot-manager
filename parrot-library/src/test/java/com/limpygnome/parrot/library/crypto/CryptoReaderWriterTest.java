package com.limpygnome.parrot.library.crypto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertArrayEquals;

@RunWith(MockitoJUnitRunner.class)
public class CryptoReaderWriterTest
{
    // SUT
    private CryptoReaderWriter cryptoReaderWriter;

    // Test data
    private CryptoParams cryptoParams;

    @Before
    public void setup() throws Exception
    {
        char[] password = "foobar".toCharArray();

        // Setup crypto params
        CryptoParamsFactory factory = new CryptoParamsFactory();
        cryptoParams = factory.create(password, 123, 0);

        // Setup SUT
        cryptoReaderWriter = new CryptoReaderWriter();
    }

    @Test
    public void encryptDecrypt_nullValue() throws Exception
    {
        encryptDecryptTest(null);
    }

    @Test
    public void encryptDecrypt_givenValue() throws Exception
    {
        byte[] value = { 0x11, 0x22, 0x33, 0x44, 0x55 };
        encryptDecryptTest(value);
    }

    private void encryptDecryptTest(byte[] input) throws Exception
    {
        // Encrypt the input
        EncryptedValue value = cryptoReaderWriter.encrypt(cryptoParams, input);

        // Decrypt it
        byte[] decrypted = cryptoReaderWriter.decrypt(cryptoParams, value);

        // Assert that values are same
        assertArrayEquals("Decrypted value should be same as originally encrypted value", input, decrypted);
    }

}
