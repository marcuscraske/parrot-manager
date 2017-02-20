package com.limpygnome.parrot.library.dbaction;

import java.util.LinkedList;
import java.util.List;

/**
 * Used to hold a log of actions performed on the database.
 *
 * THis is currently used for merging, and not intended to be general.
 */
public class ActionsLog
{
    private List<Action> actions;

    public ActionsLog()
    {
        this.actions = new LinkedList<>();
    }

    public void add(Action action)
    {
        actions.add(action);
    }

    public List<Action> getActions()
    {
        return actions;
    }

}
