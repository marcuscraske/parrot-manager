package com.limpygnome.parrot.component.sendKeys;

import com.limpygnome.parrot.component.database.EncryptedValueService;
import com.limpygnome.parrot.component.ui.WebStageInitService;
import com.limpygnome.parrot.component.ui.WebViewStage;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    @Autowired
    private EncryptedValueService encryptedValueService;

    // State
    // -- Indicates if a listener has been added for when the application is minimized
    private boolean isMinimizeHooked = false;
    // -- Data to be sent when the app is next minimized
    private String pendingData = null;

    /**
     * Sends keys once the application is minimized.
     *
     * @param encryptedValue the value to be sent
     */
    public synchronized void send(EncryptedValue encryptedValue) throws Exception
    {
        LOG.info("queueing sending keys");

        // Fetch and store decrypted value
        pendingData = encryptedValueService.asString(encryptedValue);

        // Ensure we know when the app is minimized
        hookStageMinimized();
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

                if (newValue)
                {
                    simulateKeys();
                }
            });

            stage.focusedProperty().addListener((observable, oldValue, newValue) ->
            {
                LOG.debug("foused property changed - old: {}, new: {}", oldValue, newValue);

                if (!newValue)
                {
                    simulateKeys();
                }
            });

            LOG.debug("iconified hooked");
        }
    }

    private synchronized void simulateKeys()
    {
        if (pendingData != null)
        {
            try
            {
                LOG.debug("simulating keys...");

                // Simulate key press for each char
                Robot robot = new Robot();
                int keyCode;
                for (char key : pendingData.toCharArray())
                {
                    keyCode = KeyEvent.getExtendedKeyCodeForChar(key);
                    robot.keyPress(keyCode);
                    robot.keyRelease(keyCode);
                }

                // Wipe stored data
                pendingData = null;

                LOG.debug("finished simulating keys");
            } catch (AWTException e)
            {
                LOG.error("failed to send keys", e);
            }
        }
        else
        {
            LOG.debug("skipped simulating keys, pending data is empty");
        }
    }

}
