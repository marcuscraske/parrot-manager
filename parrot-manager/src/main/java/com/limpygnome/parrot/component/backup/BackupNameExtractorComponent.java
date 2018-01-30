package com.limpygnome.parrot.component.backup;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BackupNameExtractorComponent
{
    private static final Pattern BACKUP_NAME_MATCHER = Pattern.compile("^\\.(.+)\\.[0-9]+$");

    /**
     * Extracts the actual database name.
     *
     * This is used to detect whether the current database is actually a backup.
     *
     * First the current database's file name is taken and the first dot and ending number is excluded. This is
     * used to check whether a file, in the same directory, exists. If it does, that file is returned. Otherwise
     * no file is returned.
     *
     * @param currentFile current database path
     * @return actual database path, or null
     */
    public File extract(File currentFile)
    {
        File result = null;

        String fileName = currentFile.getName();
        Matcher matcher = BACKUP_NAME_MATCHER.matcher(fileName);

        if (matcher.matches() && matcher.groupCount() > 0)
        {
            String actualFileName = matcher.group(1);
            File parentFile = currentFile.getParentFile();
            File possibleDatabase = new File(parentFile, actualFileName);
            if (possibleDatabase.exists())
            {
                result = possibleDatabase;
            }
        }

        return result;
    }

}
