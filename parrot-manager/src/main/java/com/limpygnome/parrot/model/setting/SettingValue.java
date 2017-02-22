package com.limpygnome.parrot.model.setting;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;

/**
 * Created by limpygnome on 22/02/17.
 */
public class SettingValue<T> implements Serializable
{
    private T value;

    public SettingValue(T defaultValue)
    {
        this.value = defaultValue;
    }

    public T value()
    {
        return value;
    }

    public void setValue(T value)
    {
        this.value = value;
    }

    @JsonIgnore
    @Override
    public String toString()
    {
        return "SettingValue{" +
                "value=" + value +
                '}';
    }

}
