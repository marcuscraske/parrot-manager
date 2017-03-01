package com.limpygnome.parrot.model.setting;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;

/**
 * Used to hold a setting, which can have an initial default value.
 */
public class SettingsValue<T> implements Serializable
{
    private T value;

    public SettingsValue()
    {
        this.value = null;
    }

    public SettingsValue(T defaultValue)
    {
        this.value = defaultValue;
    }

    public T getValue()
    {
        return value;
    }

    public void setValue(T value)
    {
        this.value = value;
    }

    /**
     * Used by JavaScript invocations, which do not typecast to the generic type (T).
     *
     * @param value long value
     */
    public void setValueLong(long value)
    {
        this.value = (T) (Long) value;
    }

    @JsonIgnore
    @Override
    public String toString()
    {
        return "SettingsValue{" +
                "value=" + value +
                '}';
    }

}
