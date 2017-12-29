package com.limpygnome.parrot.component.sendKeys;

import com.limpygnome.parrot.component.file.FileComponent;
import com.limpygnome.parrot.component.settings.Settings;
import com.limpygnome.parrot.component.settings.SettingsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Repository of keyboard layouts.
 *
 * These are loaded from both the class-path, 'keyboard-layout' in the local user's parrot directory and
 * 'keyboard-layout' from working directory (in that order).
 */
@Repository
public class KeyboardLayoutRepository
{
    private static final Logger LOG = LogManager.getLogger(KeyboardLayoutRepository.class);

    @Autowired
    private SettingsService settingsService;
    @Autowired
    private FileComponent fileComponent;

    private Map<String, KeyboardLayout> layoutMap;

    public KeyboardLayoutRepository()
    {
        layoutMap = new HashMap<>();
    }

    @PostConstruct
    public synchronized void refresh()
    {
        layoutMap.clear();
        loadFromClassPath();
        loadFromFileSystem();
    }

    /**
     * @return keyboard layout to be used; determined based on settings, otherwise based on locale and operating system
     */
    public synchronized KeyboardLayout determineBest()
    {
        KeyboardLayout result = null;

        // determine by settings override
        Settings settings = settingsService.getSettings();
        String keyboardName = settings.getKeyboardLayout().getValue();

        if (keyboardName != null)
        {
            result = layoutMap.get(keyboardName);
        }

        // determine by environment
        if (result == null)
        {
            String os = currentOs();
            String locale = currentLocale();

            for (KeyboardLayout layout : layoutMap.values())
            {
                if (result == null || layout.isBetterMatch(layout, os, locale))
                {
                    result = layout;
                }
            }
        }

        return result;
    }

    private String currentOs()
    {
        String result = null;

        String os = System.getProperty("os.name");
        if (os != null)
        {
            os = os.toLowerCase();

            if (os.contains("mac"))
            {
                result = "mac";
            }
            else if (os.contains("win"))
            {
                result = "windows";
            }
            else if (os.contains("nux"))
            {
                result = "linux";
            }
        }

        return result;
    }

    private String currentLocale()
    {
        String result = null;

        Locale locale = Locale.getDefault();
        if (locale != null)
        {
            result = locale.toLanguageTag().toLowerCase();
        }

        return result;
    }

    private void loadFromClassPath()
    {
        try
        {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("/keyboard-layout/**");

            for (Resource resource : resources)
            {
                try
                {
                    InputStream inputStream = resource.getInputStream();
                    load(inputStream, resource.getFilename());
                }
                catch (IOException e)
                {
                    LOG.error("failed to load keyboard layout from class-path - path: {}", resource.getFilename(), e);
                }
            }
        }
        catch (IOException e)
        {
            LOG.error("failed to load keyboard layouts from class-path", e);
        }
    }

    private void loadFromFileSystem()
    {
        // load from local user's parrot dir
        File localDirectory = fileComponent.resolvePreferenceFile("keyboard-layout");
        loadFromDirectory(localDirectory);

        // load from working directory
        File workingDirectory = new File("keyboard-layout");
        loadFromDirectory(workingDirectory);
    }

    private void loadFromDirectory(File directory)
    {
        File[] files = directory.listFiles();

        if (files != null)
        {
            for (File file : files)
            {
                try
                {
                    InputStream inputStream = new FileInputStream(file);
                    load(inputStream, file.getAbsolutePath());
                } catch (IOException e)
                {
                    LOG.error("failed to load keyboard layout - path: {}", file.getAbsolutePath(), e);
                }
            }
        }
        else
        {
            LOG.info("keyboard layout directory does not exist or cannot be read - path: {}", directory.getAbsolutePath());
        }
    }

    private void load(InputStream inputStream, String nameForLogging) throws IOException
    {
        // convert to string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1)
        {
            baos.write(buffer, 0, read);
        }

        byte[] raw = baos.toByteArray();
        String config = new String(raw, "UTF-8");

        // parse config and add to collection
        KeyboardLayout keyboardLayout = KeyboardLayout.parse(config, nameForLogging);
        layoutMap.put(keyboardLayout.getName(), keyboardLayout);
    }


}
