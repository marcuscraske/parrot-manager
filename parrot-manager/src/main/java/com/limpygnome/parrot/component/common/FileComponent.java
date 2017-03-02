package com.limpygnome.parrot.component.common;

import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Used to resolve paths and perform common operations for file related functions.
 *
 * TODO: unit test
 */
@Component
public class FileComponent
{

    /**
     * Used to resolve path shortcuts.
     *
     * ~/ will be converted to home path
     * 
     * @param path the raw path
     * @return fully resolved path
     */
    public String resolvePath(String path)
    {
        if (path.startsWith("~/") && path.length() > 2)
        {
            // TODO: test on windows, could be flawed...
            String homeDirectory = System.getProperty("user.home");
            String pathSeparator = System.getProperty("file.separator");

            path = homeDirectory + pathSeparator + path.substring(2);
        }

        return path;
    }

    /**
     * Builds the full path to a preference file.
     *
     * @param fileName file name
     * @return the full path
     */
    public File resolvePreferenceFile(String fileName)
    {
        // TODO: need to consider windows
        String homeDir = System.getProperty("user.home");
        File result = new File(homeDir + "/.config/parrot-manager/" + fileName);
        return result;
    }

}
