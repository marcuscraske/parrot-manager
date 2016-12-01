package com.limpygnome.parrot.model.dbaction;

import com.limpygnome.parrot.model.db.DatabaseNode;

/**
 * A unique instance is created for each node merge call, so the call its self knows some information about the merge
 * progress / location etc.
 */
public class MergeInfo {

    public final ActionsLog actionsLog;

    public final String nodePath;

    public MergeInfo(MergeInfo parent, DatabaseNode currentNode)
    {
        this.actionsLog = parent.actionsLog;
        nodePath = (parent != null ? parent.nodePath : "") + "/" + currentNode.getName();
    }

    public MergeInfo(ActionsLog actionsLog, DatabaseNode currentNode)
    {
        this.actionsLog = actionsLog;
        nodePath = "/" + currentNode.getName();
    }

    public void addMergeMessage(String message) {
        actionsLog.add(new Action(nodePath + " - " + message));
    }

}
