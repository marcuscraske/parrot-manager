package com.limpygnome.parrot.component.sendKeys;

public class SendKeysChangeEvent
{
    private String nodeId;
    private String encryptedValueId;
    private boolean queued;

    public SendKeysChangeEvent(String nodeId, String encryptedValueId, boolean queued)
    {
        this.nodeId = nodeId;
        this.encryptedValueId = encryptedValueId;
        this.queued = queued;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public String getEncryptedValueId()
    {
        return encryptedValueId;
    }

    public boolean isQueued()
    {
        return queued;
    }
}
