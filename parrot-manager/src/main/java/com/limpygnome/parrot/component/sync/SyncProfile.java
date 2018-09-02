package com.limpygnome.parrot.component.sync;

import java.util.UUID;

public abstract class SyncProfile
{
    // Mandatory
    private UUID id;
    private String name;

    // Options
    private String machineFilter;


    public SyncProfile()
    {
        this.id = UUID.randomUUID();
    }

    public SyncProfile(UUID id, String name)
    {
        this.id = id;
        this.name = name;
    }

    /**
     * @return the type of profile; used by front-end for specific behaviour
     */
    public abstract String getType();

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot set profile to null ID");
        }

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

    public String getMachineFilter()
    {
        return machineFilter;
    }

    public void setMachineFilter(String machineFilter)
    {
        this.machineFilter = machineFilter;
    }

}
