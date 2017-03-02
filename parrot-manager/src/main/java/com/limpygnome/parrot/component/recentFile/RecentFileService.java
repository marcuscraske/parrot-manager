package com.limpygnome.parrot.component.recentFile;

import com.limpygnome.parrot.component.common.FileComponent;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Stores and retrieves recently opened files.
 */
@Service
public class RecentFileService
{
    // Can consider as setting in future...
    private static final int RECENT_FILES_LIMIT = 5;

    @Autowired
    private FileComponent fileComponent;

    private LinkedList<RecentFile> recentFiles;
    private ObjectMapper objectMapper;

    public RecentFileService()
    {
        recentFiles = new LinkedList<>();
        objectMapper = new ObjectMapper();

        loadFromFile();
    }

    public synchronized void loadFromFile() throws IOException
    {
    }

    private synchronized void saveToFile() throws IOException
    {
        File file = getRecentFilesFile();
        objectMapper.writeValue(file, recentFiles);
    }

    private File getRecentFilesFile()
    {
        return fileComponent.resolvePreferenceFile("recent-files.json");
    }

    public synchronized RecentFile[] fetch()
    {

    }

    public synchronized void add(RecentFile recentFile)
    {
        // Add to front of queue, but remove last element if we're over the limit of items
        recentFiles.addFirst(recentFile);

        if (recentFiles.size() > RECENT_FILES_LIMIT)
        {
            recentFiles.removeLast();
        }

        // Persist queue state
        saveToFile();
    }

}
