package com.limpygnome.parrot.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to keep references to data, used by the front-end, to avoid being garbage collected.
 *
 * This opens up the potential of memory leaks.
 *
 * Notes:
 * - This service is consumed by other services and is not injected.
 * - All data is wiped when opening/closing a database.
 */
@Service
public class SessionService
{
    private static final Logger LOG = LogManager.getLogger(SessionService.class);

    private Map<String, Object> store;

    public SessionService()
    {
        store = new HashMap<>();
    }

    public void reset()
    {
        store.clear();
        LOG.debug("wiped session data");
    }

    public void put(String key, Object value)
    {
        store.put(key, value);
        LOG.debug("added value - key: {}", key);
    }

    public Object get(String key)
    {
        return store.get(key);
    }

    public Object remove(String key)
    {
        Object object = store.remove(key);
        LOG.debug("removed value - key: {}", key);
        return object;
    }

}
