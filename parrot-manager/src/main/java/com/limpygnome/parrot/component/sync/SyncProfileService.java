package com.limpygnome.parrot.component.sync;

import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.event.DatabaseChangingEvent;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages remote sync profiles.
 */
//@Service
public class SyncProfileService implements DatabaseChangingEvent
{
    private static final Logger LOG = LoggerFactory.getLogger(SyncProfileService.class);

    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private Map<String, SyncHandler> handlers;

    private Map<UUID, SyncProfile> profiles;
    private SyncProfile[] cachedProfiles;

    public SyncProfileService()
    {
        this.profiles = new HashMap<>();
        this.cachedProfiles = new SyncProfile[0];
    }

    @Override
    public synchronized void eventDatabaseChanged(boolean open)
    {
        if (open)
        {
            refresh();
        }
        else
        {
            profiles.clear();
        }
    }

    /**
     * Refreshes available profiles by reading raw data from persistence/database.
     */
    public synchronized void refresh()
    {
        // Wipe old
        profiles.clear();

        // Deserialize and add nodes
        DatabaseNode remoteSyncNode = remoteSyncNode();
        for (DatabaseNode node : remoteSyncNode.getChildren())
        {
            boolean found = false;
            Iterator<SyncHandler> it = handlers.values().iterator();
            while (!found && it.hasNext())
            {
                SyncProfile profile = it.next().deserialize(node);
                if (profile != null)
                {
                    profiles.put(profile.getId(), profile);
                }
            }

            if (!found)
            {
                LOG.warn("could not load remote sync profile - id: {}, name: {}", node.getId(), node.getName());
            }
        }
    }

    /**
     * @return all available profiles, can be empty
     */
    public synchronized SyncProfile[] fetch()
    {
        return cachedProfiles;
    }

    /**
     * @param profile profile to be saved
     */
    public synchronized void save(SyncProfile profile)
    {
        // Serialize into database node
        DatabaseNode node = null;
        Iterator<SyncHandler> handler = handlers.values().iterator();
        while (node == null && handler.hasNext())
        {
            node = handler.next().serialize(profile);
        }

        // Save to hidden rmeote sync node
        remoteSyncNode().add(node);
    }

    /**
     * @param id the profile to be removed
     */
    public synchronized void remove(String id)
    {
        UUID uuid = UUID.fromString(id);

        // Drop from profiles
        profiles.remove(id);

        // Drop from database
        DatabaseNode childNode = remoteSyncNode().getById(uuid);
        if (childNode != null)
        {
            childNode.remove();
            LOG.info("removed remote sync node - id: {}", id);
        }
        else
        {
            LOG.warn("unable to remote remote sync node, not found - id: {}", id);
        }
    }

    private synchronized void refreshCache()
    {
        this.cachedProfiles = profiles.values().toArray(new SyncProfile[profiles.size()]);
    }

    private DatabaseNode remoteSyncNode()
    {
        Database database = databaseService.getDatabase();
        DatabaseNode remoteSync = database.getRoot().getByName("remote-sync");
        return remoteSync;
    }

}
