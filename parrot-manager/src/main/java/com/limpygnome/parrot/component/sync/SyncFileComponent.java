package com.limpygnome.parrot.component.sync;

import com.limpygnome.parrot.component.file.FileComponent;
import com.limpygnome.parrot.component.sync.SyncOptions;
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
     * @return error message, or null if OK
     */
    public String checkDestinationPath(SyncOptions options)
    {
        String result = null;

        // Check directory exists of local path
        String localPath = fileComponent.resolvePath(options.getDestinationPath());

        File localFile = new File(localPath);
        File parentLocalFile = localFile.getParentFile();

        if (parentLocalFile == null || !parentLocalFile.exists())
        {
            result = "Destination directory does not exist";
        }
        else if (localFile.exists() && (!localFile.canWrite() || !localFile.canRead()))
        {
            result = "Cannot read/write to existing destination path file";
        }

        return result;
    }

}
