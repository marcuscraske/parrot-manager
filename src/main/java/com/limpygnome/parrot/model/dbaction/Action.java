package com.limpygnome.parrot.model.dbaction;

/**
 * Used to hold information about an action performed on the database.
 */
public class Action {

    private String action;

    public Action(String action)
    {
        this.action = action;
    }

    public String getAction()
    {
        return action;
    }

}
