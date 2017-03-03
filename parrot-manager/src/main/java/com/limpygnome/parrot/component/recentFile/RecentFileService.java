package com.limpygnome.parrot.component.recentFile;

import com.limpygnome.parrot.component.common.FileComponent;
import com.limpygnome.parrot.component.session.SessionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Stores and retrieves recently opened files.
 */
@Service
public class RecentFileService
{
    private static final Logger LOG = LogManager.getLogger(RecentFileService.class);

    // Can consider as setting in future...
    private static final int RECENT_FILES_LIMIT = 5;

    @Autowired
    private SessionService sessionService;
    @Autowired
    private FileComponent fileComponent;

    private LinkedList<RecentFile> recentFiles;
    private ObjectMapper objectMapper;

    public RecentFileService() throws IOException
    {
        recentFiles = new LinkedList<>();
        objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void initialLoad() throws IOException
    {
        loadFromFile();
    }

    public synchronized void loadFromFile() throws IOException
    {
        File file = getRecentFilesFile();

        if (file.exists())
        {
            RecentFile[] recentFiles = objectMapper.readValue(file, RecentFile[].class);
            Arrays.stream(recentFiles).forEach(recentFile -> this.recentFiles.add(recentFile));
            LOG.debug("recent files loaded - count: {}", recentFiles.length);
        }
        else
        {
            LOG.debug("no recent files preference file found, skipped loading");
        }
    }

    private synchronized void saveToFile() throws IOException
    {
        File file = getRecentFilesFile();
        RecentFile[] recentFiles = this.recentFiles.toArray(new RecentFile[this.recentFiles.size()]);
        objectMapper.writeValue(file, recentFiles);

        LOG.debug("persisted recent files");
    }

    private File getRecentFilesFile()
    {
        return fileComponent.resolvePreferenceFile("recent-files.json");
    }

    public synchronized RecentFile[] fetch()
    {
        RecentFile[] recentFiles = this.recentFiles.toArray(new RecentFile[this.recentFiles.size()]);

        // Persist to session service to prevent GC
        sessionService.put("recentFiles", recentFiles);

        return recentFiles;
    }

    public synchronized void add(RecentFile recentFile) throws IOException
    {
        // Only update the list if the first/most recent item was not already this item. As a lot of users
        // will most likely open the same file, it's best to not change anything and reduce I/O...
        if (recentFiles.isEmpty() || !recentFiles.getFirst().equals(recentFile))
        {
            // Remove in case it already exists
            recentFiles.remove(recentFile);

            // Add to front of queue, but remove last element if we're over the limit of items
            recentFiles.addFirst(recentFile);

            if (recentFiles.size() > RECENT_FILES_LIMIT)
            {
                recentFiles.removeLast();
            }

            LOG.debug("added new recent file - {}", recentFile);

            // Persist queue state
            saveToFile();
        }
    }

}
