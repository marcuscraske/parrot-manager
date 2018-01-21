package com.limpygnome.parrot.component.clipboard;

import com.limpygnome.parrot.component.settings.Settings;
import com.limpygnome.parrot.component.settings.event.SettingsRefreshedEvent;
import com.limpygnome.parrot.lib.threading.DelayedThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

/**
 * Used to manipulate data on host system's clipboard.
 */
@Service
public class ClipboardService implements SettingsRefreshedEvent
{
    private static final Logger LOG = LoggerFactory.getLogger(ClipboardService.class);

    // Manages thread for wiping clipboard
    private DelayedThread delayedThread;

    // Period in seconds after which to wipe clipboard
    private long wipeClipboardDelay;

    public ClipboardService()
    {
        delayedThread = new DelayedThread();
    }

    /**
     * @param value the value to set on the clipboard
     */
    public void setText(String value)
    {
        // Replace null with empty value
        if (value == null)
        {
            value = "";
        }

        StringSelection selection = new StringSelection(value);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

        if (value.isEmpty())
        {
            LOG.info("clipboard wiped / set to empty");
        }
        else
        {
            LOG.info("copied value to clipboard - length: {}", value.length());

            // queue wiping clipboard
            wipeClipboardAfterSomeTime();
        }
    }

    private void wipeClipboardAfterSomeTime()
    {
        if (wipeClipboardDelay > 0)
        {
            delayedThread.start(() -> setText(null), wipeClipboardDelay);
        }
    }

    @Override
    public void eventSettingsRefreshed(Settings settings)
    {
        wipeClipboardDelay = settings.getWipeClipboardDelay().getSafeLong(0L) * 1000L;
    }

}
