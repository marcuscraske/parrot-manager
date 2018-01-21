package com.limpygnome.parrot.component.browser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for opening URLs on native machine securely.
 *
 * This is to ensure no secure data is passed from the internal front-end browser ui to an external URL.
 */
@Service
public class BrowserService
{
    private static final Logger LOG = LoggerFactory.getLogger(BrowserService.class);

    /*
        URLs can only be HTTPS, as apart of supporting a secure / SSL-only only internet.
     */
    private static final Map<String, String> URL_MAP;

    static
    {
        URL_MAP = new HashMap<>();
        URL_MAP.put("github", "https://github.com/limpygnome/parrot-manager");
        URL_MAP.put("keyboardLayoutDocs", "https://github.com/limpygnome/parrot-manager/blob/develop/docs/keyboard-layouts.md");
    }

    /**
     * Opens a known URL, by name, in the user's browser.
     *
     * @param key name of URL
     */
    public void open(String key)
    {
        String url = URL_MAP.get(key);
        if (url != null)
        {
            openLink(url);
        }
    }

    private void openLink(String url)
    {
        try
        {
            // Good practice to only allow https
            URI uri = URI.create(url);
            String schema = uri.getScheme();

            if (!"https".equals(schema))
            {
                throw new SecurityException("invalid url schema for url: " + url);
            }

            // open url
            LOG.info("opening browser - url: {}", url);
            Desktop.getDesktop().browse(uri);
        }
        catch (IOException e)
        {
            LOG.error("failed to open link - url: {}", url, e);
        }
    }

}
