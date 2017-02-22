package com.limpygnome.parrot.model.setting;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;

/**
 * Created by limpygnome on 22/02/17.
 */
public class Settings implements Serializable
{
    private SettingValue<Long> automaticBackupsRetained;

    public Settings()
    {
        this.automaticBackupsRetained = new SettingValue(30);
    }

    public SettingValue<Long> automaticBackupsRetained()
    {
        return automaticBackupsRetained;
    }

    @JsonIgnore
    @Override
    public String toString()
    {
        return "Settings{" +
                "automaticBackupsRetained=" + automaticBackupsRetained +
                '}';
    }

}
