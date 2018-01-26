package com.limpygnome.parrot.component.recentFile;

import com.limpygnome.parrot.component.file.FileComponent;
import com.limpygnome.parrot.component.session.SessionService;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(RecentFileService.class);

    // Can consider as setting in future...
    private static final int RECENT_FILES_LIMIT = 5;

    @Autowired
    private SessionService sessionService;
    @Autowired
    private FileComponent fileComponent;

    // Recent files
    private LinkedList<RecentFile> recentFiles;

    // Used for persisting recent files
    private ObjectMapper objectMapper;

    // Cache of recent files
    private RecentFile[] cacheRecentFiles;

    public RecentFileService() throws IOException
    {
        recentFiles = new LinkedList<>();
        objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void initialLoad() throws IOException
    {
        reloadFromFile();
    }

    /**
     * Reloads recently used database files from a persisted file.
     *
     * @throws IOException if unable to read persisted file
     */
    public synchronized void reloadFromFile() throws IOException
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

        updateCache();
    }

    public synchronized RecentFile[] fetch()
    {
        return cacheRecentFiles;
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

    public synchronized void delete(RecentFile recentFile) throws IOException
    {
        if (recentFile != null)
        {
            // drop item
            recentFiles.remove(recentFile);
            LOG.debug("deleted recent file - {}", recentFile);

            // persist collection
            saveToFile();
        }
        else
        {
            LOG.warn("attempted to delete null recent file");
        }
    }

    /**
     * clears recent files and persists empty state to preferences file.
     */
    public synchronized void clear()
    {
        recentFiles.clear();

        try
        {
            saveToFile();
        }
        catch (IOException e)
        {
            LOG.error("failed to save recent files", e);
        }
    }

    private synchronized void saveToFile() throws IOException
    {
        File file = getRecentFilesFile();
        RecentFile[] recentFiles = this.recentFiles.toArray(new RecentFile[this.recentFiles.size()]);
        objectMapper.writeValue(file, recentFiles);
        updateCache();

        LOG.debug("persisted recent files");
    }

    private File getRecentFilesFile()
    {
        return fileComponent.resolvePreferenceFile("recent-files.json");
    }

    private void updateCache()
    {
        this.cacheRecentFiles = this.recentFiles.toArray(new RecentFile[this.recentFiles.size()]);
    }

}
