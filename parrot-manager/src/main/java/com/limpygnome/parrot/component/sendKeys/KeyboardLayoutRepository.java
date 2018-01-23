package com.limpygnome.parrot.component.sendKeys;

import com.limpygnome.parrot.component.file.FileComponent;
import com.limpygnome.parrot.component.settings.Settings;
import com.limpygnome.parrot.component.settings.SettingsService;
import com.limpygnome.parrot.lib.io.StringStreamOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

/**
 * Repository of keyboard layouts.
 *
 * These are loaded from both the class-path, 'keyboard-layout' in the local user's parrot directory and
 * 'keyboard-layout' from working directory (in that order).
 */
@Repository
public class KeyboardLayoutRepository
{
    private static final Logger LOG = LoggerFactory.getLogger(KeyboardLayoutRepository.class);

    // Components
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private FileComponent fileComponent;
    @Autowired
    private StringStreamOperations stringStreamOperations;

    // Available keyboard layouts
    private Map<String, KeyboardLayout> layoutMap;

    public KeyboardLayoutRepository()
    {
        layoutMap = new HashMap<>();
    }

    /**
     * Reloads keyboard layouts.
     *
     * @return list of error messages
     */
    @PostConstruct
    public synchronized String[] reload()
    {
        List<String> messages = new LinkedList<>();

        // clear existing layouts
        layoutMap.clear();

        // load layouts
        loadFromClassPath(messages);
        loadFromFileSystem(messages);

        // give back any errors, in case this was manually reloaded
        String[] result = messages.toArray(new String[messages.size()]);
        return result;
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
                if (result == null || layout.isBetterMatch(result, os, locale))
                {
                    result = layout;
                }
            }
        }

        return result;
    }

    public synchronized KeyboardLayout[] getKeyboardLayouts()
    {
        return layoutMap.values().toArray(new KeyboardLayout[layoutMap.size()]);
    }

    public synchronized File getLocalDirectory()
    {
        File result = fileComponent.resolvePreferenceFile("keyboard-layout");
        return result;
    }

    public synchronized File getWorkingDirectory()
    {
        File result = new File("keyboard-layout");
        return result;
    }

    private String currentOs()
    {
        String result = null;

        String os = System.getProperty("os.name");
        if (os != null)
        {
            os = os.toLowerCase();

            if (os.contains("windows"))
            {
                result = "windows";
            }
            else if (os.contains("mac os"))
            {
                result = "mac";
            }
            else if (os.contains("nux") || os.contains("nix") || os.contains("aix"))
            {
                result = "linux";
            }
            else
            {
                LOG.warn("unable to detect current os, keyboard layout may not be as expected - os: {}", os);
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

    private void loadFromClassPath(List<String> messages)
    {
        try
        {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("/keyboard-layout/**");

            for (Resource resource : resources)
            {
                String fileName = resource.getFilename();

                if (fileName != null && fileName.length() > 0)
                {
                    try
                    {
                        InputStream inputStream = resource.getInputStream();
                        load(inputStream, messages, resource.getFilename());
                    }
                    catch (IOException e)
                    {
                        String path = resource.getFile().getAbsolutePath();
                        messages.add("failed to load keyboard layout - path: " + path);
                        LOG.error("failed to load keyboard layout from class-path - path: {}", path, e);
                    }
                }
            }
        }
        catch (IOException e)
        {
            LOG.error("failed to load keyboard layouts from class-path", e);
            messages.add("failed to load keyboard layouts from class-path, refer to logs");
        }
    }

    private void loadFromFileSystem(List<String> messages)
    {
        // load from local user's parrot dir (or create it)
        File localDirectory = getLocalDirectory();

        if (!localDirectory.exists())
        {
            localDirectory.mkdirs();
            messages.add("local preferences directory missing (created)");
        }
        else
        {
            loadFromDirectory(messages, localDirectory);
        }

        // load from working directory (or create it)
        File workingDirectory = getWorkingDirectory();

        if (!workingDirectory.exists())
        {
            workingDirectory.mkdirs();
            messages.add("working directory missing (created)");
        }
        else
        {
            loadFromDirectory(messages, workingDirectory);
        }
    }

    private void loadFromDirectory(List<String> messages, File directory)
    {
        File[] files = directory.listFiles();

        if (files != null)
        {
            for (File file : files)
            {
                try
                {
                    InputStream inputStream = new FileInputStream(file);
                    load(inputStream, messages, file.getAbsolutePath());
                }
                catch (IOException e)
                {
                    String path = file.getAbsolutePath();

                    LOG.error("failed to load keyboard layout - path: {}", path, e);
                    messages.add("failed to load keyboard layout, refer to logs - path: " + path);
                }
            }
        }
        else
        {
            String path = directory.getAbsolutePath();

            LOG.info("keyboard layout directory does not exist or cannot be read - path: {}", path);
            messages.add("directory does not exist or cannot be read - path: " + path);
        }
    }

    private void load(InputStream inputStream, List<String> messages, String nameForLogging) throws IOException
    {
        // convert to string
        String config = stringStreamOperations.readString(inputStream);

        // parse config and add to collection
        try
        {
            KeyboardLayout keyboardLayout = KeyboardLayout.parse(config, messages, nameForLogging);
            layoutMap.put(keyboardLayout.getName(), keyboardLayout);
        }
        catch (IllegalStateException e)
        {
            LOG.error("unexpected parsing issue for keyboard layout - layout: {}", nameForLogging, e);
        }
    }

}
