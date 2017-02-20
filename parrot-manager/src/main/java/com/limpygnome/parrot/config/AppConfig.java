package com.limpygnome.parrot.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring.
 */
@Configuration
@ComponentScan({
        "com.limpygnome.parrot"
})
public class AppConfig
{
}
