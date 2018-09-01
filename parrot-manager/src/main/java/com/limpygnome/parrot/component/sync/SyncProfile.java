package com.limpygnome.parrot.component.sync;

import java.util.UUID;

public abstract class SyncProfile
{
    private UUID id;
    private String name;

    public SyncProfile(UUID id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
