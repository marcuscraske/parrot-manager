package com.limpygnome.parrot.config;

import com.limpygnome.parrot.library.crypto.CryptoParamsFactory;
import com.limpygnome.parrot.library.db.DatabaseMerger;
import com.limpygnome.parrot.library.db.DatabaseOptimizer;
import com.limpygnome.parrot.library.io.DatabaseJsonReaderWriter;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to inject instances of POJOs, which can be used as singletons, as beans, from the parrot library.
 */
@Configuration
public class LibraryBeansConfig
{

    @Bean
    public DatabaseReaderWriter databaseReaderWriter()
    {
        return new DatabaseJsonReaderWriter();
    }

    @Bean
    public CryptoParamsFactory cryptoParamsFactory()
    {
        return new CryptoParamsFactory();
    }

    @Bean
    public DatabaseMerger databaseMerger()
    {
        return new DatabaseMerger();
    }

    @Bean
    public DatabaseOptimizer databaseOptimizer()
    {
        return new DatabaseOptimizer();
    }

}
