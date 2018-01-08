package com.limpygnome.parrot.config;

import com.limpygnome.parrot.lib.io.StringStreamOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Configuration for Spring.
 */
@Configuration
@ComponentScan({
        "com.limpygnome.parrot"
})
public class AppConfig
{

    /**
     * @return property source config to allow optional injected values, using @Value annotation
     */
    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
    {
        PropertySourcesPlaceholderConfigurer config = new PropertySourcesPlaceholderConfigurer();
        config.setIgnoreResourceNotFound(true);
        return config;
    }

    @Bean
    public StringStreamOperations stringStreamOperations()
    {
        return new StringStreamOperations();
    }

}
