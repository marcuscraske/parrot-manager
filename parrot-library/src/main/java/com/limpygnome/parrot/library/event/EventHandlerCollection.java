package com.limpygnome.parrot.library.event;

import java.util.LinkedList;
import java.util.List;

/**
 * Collection for event handlers (observer pattern).
 *
 * @param <T> event class
 */
public abstract class EventHandlerCollection<T>
{
    private List<T> handlers;

    public EventHandlerCollection()
    {
        this.handlers = new LinkedList<>();
    }

    /**
     * Triggers handlers.
     *
     * @param args args to be passed
     */
    public synchronized void trigger(Object... args)
    {
        for (T handler : handlers)
        {
            invoke(handler, args);
        }
    }

    /**
     * Invoked when the event is triggered, which handlers raising the event for a specific handler.
     *
     * @param args args passed
     */
    protected abstract void invoke(T instance, Object... args);

    /**
     * @param handler handler to be added
     */
    public synchronized void add(T handler)
    {
        handlers.add(handler);
    }

    /**
     * @param handler handler to be removed
     */
    public synchronized void remove(T handler)
    {
        handlers.remove(handler);
    }

}
