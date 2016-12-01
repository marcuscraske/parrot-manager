package com.limpygnome.parrot.model.dbaction;

import com.limpygnome.parrot.model.db.DatabaseNode;

/**
 * A unique instance is created for each node merge call, so the call its self knows some information about the merge
 * progress / location etc.
 */
public class MergeInfo {

    public final ActionsLog actionsLog;

    public final String nodePathParent;

    public MergeInfo(MergeInfo parent, DatabaseNode parentNode, ActionsLog actionsLog) {
        this.actionsLog = actionsLog;

        nodePathParent = (parent != null ? parent.nodePathParent : "") + "/" + parentNode.getName();
    }

    public MergeInfo(ActionsLog actionsLog, DatabaseNode child)
    {
        this.actionsLog = actionsLog;
    }

    public MergeInfo(MergeInfo parent, DatabaseNode child)
    {
        this.actionsLog = parent.actionsLog;
    }

    public void addMergeMessage(String message) {
        actionsLog.add(new Action(nodePath + " - " + message));
    }

    /**
     * @param currentNode the current node, a child of the parent node of this info
     * @return the path of the node
     */
    public String getNodePath(DatabaseNode currentNode)
    {
        return nodePathParent + "/" + currentNode.getName();
    }

}
