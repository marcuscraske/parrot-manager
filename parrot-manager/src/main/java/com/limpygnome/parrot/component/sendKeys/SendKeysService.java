package com.limpygnome.parrot.component.sendKeys;

import com.limpygnome.parrot.component.MemoryCompoonent;
import com.limpygnome.parrot.component.ui.WebStageInitService;
import com.limpygnome.parrot.component.ui.WebViewStage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.AWTException;
import java.awt.Robot;

/**
 * A service for sending keys to another application.
 */
@Service
public class SendKeysService
{
    private static final Logger LOG = LogManager.getLogger(SendKeysService.class);

    // Components
    @Autowired
    private WebStageInitService initService;

    private MemoryCompoonent memoryCompoonent;

    // State
    // -- Indicates if a listener has been added for when the application is minimized
    private boolean isMinimizeHooked = false;
    // -- Data to be sent when the app is next minimized
    private char[] pendingData = null;

    /**
     * Sends keys once the application is minimized.
     *
     * @param text cloned and stored internally; then wiped once sent
     */
    public synchronized void sendKeys(char[] text)
    {
        // Ensure we know when the app is minimized
        hookStageMinimized();

        // Clone and store text to be sent
        
    }

    private synchronized void hookStageMinimized()
    {
        if (!isMinimizeHooked)
        {
            isMinimizeHooked = true;

            // Hook stage for minify (or dubbed iconified) event
            WebViewStage stage = initService.getStage();
            stage.iconifiedProperty().addListener((observable, oldValue, newValue) ->
            {
                LOG.debug("iconified property changed - old: {}, new: {}", oldValue, newValue);

                synchronized (SendKeysService.this)
                {
                    if (newValue && pendingData != null)
                    {
                        simulateKeys();
                    }
                }
            });
        }
    }

    private synchronized void simulateKeys()
    {
        try
        {
            // Simulate key press for each char
            Robot robot = new Robot();

            for (char key : pendingData)
            {
                robot.keyPress(key);
                robot.keyRelease(key);
            }

            // Wipe the data passed to us
            memoryCompoonent.wipe(pendingData);
            pendingData = null;
        }
        catch (AWTException e)
        {
            LOG.error("failed to send keys", e);
        }
    }

}
