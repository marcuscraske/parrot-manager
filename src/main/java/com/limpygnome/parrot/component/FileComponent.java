package com.limpygnome.parrot.component;

/**
 * Used to resolve paths and perform common operations for file related functions.
 *
 * TODO: unit test
 */
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

}
