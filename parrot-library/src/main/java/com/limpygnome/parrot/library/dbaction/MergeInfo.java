package com.limpygnome.parrot.library.dbaction;

import com.limpygnome.parrot.library.db.DatabaseNode;

/**
 * A unique instance is created for each node merge call, so the call its self knows some information about the merge
 * progress / location etc.
 */
public class MergeInfo {

    public final ActionsLog actionsLog;

    public final String nodePath;

    public MergeInfo(ActionsLog actionsLog, DatabaseNode currentNode)
    {
        this.actionsLog = actionsLog;
        nodePath = currentNode.getPath();
    }

    public void addMergeMessage(String message) {
        actionsLog.add(new Action(nodePath + " - " + message));
    }

}
