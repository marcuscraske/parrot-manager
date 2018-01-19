package com.limpygnome.parrot.component.settings.event;

import com.limpygnome.parrot.component.settings.Settings;

/**
 * Event for when settings are updated.
 */
public interface SettingsRefreshedEvent
{

    /**
     * Invoked when event occurs.
     *
     * @param settings settings
     */
    void eventSettingsRefreshed(Settings settings);

}
