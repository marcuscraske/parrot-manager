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
    }

    /**
     * @return the type of profile; used by front-end for specific behaviour
     */
    public abstract String getType();

    public String getId()
    {
        return id != null ? id.toString() : null;
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

    public String getMachineFilter()
    {
        return machineFilter;
    }

    public void setMachineFilter(String machineFilter)
    {
        this.machineFilter = machineFilter;
    }

}
