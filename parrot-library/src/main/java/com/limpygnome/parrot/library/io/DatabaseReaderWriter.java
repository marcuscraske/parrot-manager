package com.limpygnome.parrot.library.io;

import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.db.Database;

import java.io.File;

/**
 * Created by limpygnome on 21/02/17.
 */
public interface DatabaseReaderWriter
{

    Database open(String path, char[] password) throws Exception;

    Database openFileEncrypted(byte[] encryptedData, char[] password) throws Exception;

    Database openMemoryEncrypted(byte[] rawData, char[] password, CryptoParams fileCryptoParams) throws Exception;

    byte[] saveMemoryEncrypted(Database database) throws Exception;

    byte[] saveFileEncrypted(Database database) throws Exception;

    void save(Database database, String path) throws Exception;

    void save(Database database, File file) throws Exception;

}
