package com.limpygnome.parrot.component.sync;

import com.limpygnome.parrot.component.file.FileComponent;
import com.limpygnome.parrot.component.sync.SyncOptions;
import com.limpygnome.parrot.library.log.Log;
import com.limpygnome.parrot.library.log.LogItem;
import com.limpygnome.parrot.library.log.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class SyncFileComponent
{
    @Autowired
    private FileComponent fileComponent;

    /**
     * Checks whether destination path of provided options is valid.
     *
     * @param options the options to be checked
     * @param log issues are appended to provided log
     * @return true = valid, false = not valid
     */
    public boolean isDestinationPathValid(SyncOptions options, Log log)
    {
        boolean success = true;

        // Check directory exists of local path
        String localPath = fileComponent.resolvePath(options.getDestinationPath());

        File localFile = new File(localPath);
        File parentLocalFile = localFile.getParentFile();

        if (parentLocalFile == null || !parentLocalFile.exists())
        {
            log.add(new LogItem(LogLevel.ERROR, false, "Destination directory does not exist"));
            success = false;
        }
        else if (localFile.exists() && (!localFile.canWrite() || !localFile.canRead()))
        {
            log.add(new LogItem(LogLevel.ERROR, false, "Cannot read/write to existing destination path file"));
            success = false;
        }

        // Build result
        return success;
    }

}
