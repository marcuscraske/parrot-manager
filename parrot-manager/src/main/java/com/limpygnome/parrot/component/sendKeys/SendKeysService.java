package com.limpygnome.parrot.component.sendKeys;

import com.limpygnome.parrot.component.database.EncryptedValueService;
import com.limpygnome.parrot.component.ui.WebStageInitService;
import com.limpygnome.parrot.component.ui.WebViewStage;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.security.Key;
import java.util.UUID;
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
    // -- The encrypted value pending being sent
    private UUID pendingEncryptedValueId = null;

    /**
     * Sends keys once the application is minimized.
     *
     * @param encryptedValue the value to be sent
     */
    public synchronized void send(EncryptedValue encryptedValue) throws Exception
    {
        if (encryptedValue != null)
        {
            // Reset if same key
            if (pendingEncryptedValueId != null && pendingEncryptedValueId.equals(encryptedValue.getId()))
            {
                pendingData = null;
                pendingEncryptedValueId = null;

                LOG.info("reset pending send keys");
            }
            else
            {
                LOG.info("queueing sending keys");

                // Fetch and store decrypted value and id
                pendingEncryptedValueId = encryptedValue.getId();
                pendingData = encryptedValueService.asString(encryptedValue);

                // Ensure we know when the app is minimized
                hookStageMinimized();
            }
        }
        else
        {
            pendingData = null;
            pendingEncryptedValueId = null;

            LOG.debug("null encrypted value, not sending keys");
        }
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

                /*
                    Delay needed for some window managers to switch windows completely / gain focus etc.

                    This should be more than sufficient time, even for slow machines. If this ever becomes an issue,
                    the delay/time can be moved to a global setting.
                 */

                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e) { }

                // Simulate key press for each char
                Robot robot = new Robot();
                for (char key : pendingData.toCharArray())
                {
                    type(robot, key);
                }

                // Wipe stored data
                pendingEncryptedValueId = null;
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

    /**
     * @return indicates if the specified value is pending to be sent as keys
     */
    public synchronized boolean isQueued(EncryptedValue value)
    {
        return pendingEncryptedValueId != null && value != null && pendingEncryptedValueId.equals(value.getId());
    }

    // TODO does this work with different keyboard layouts besides en-GB?
    private synchronized void type(Robot robot, char key)
    {
        switch (key)
        {
            case '`': type(robot, KeyEvent.VK_BACK_QUOTE);  break;
            case '-': type(robot, KeyEvent.VK_MINUS);       break;
            case '=': type(robot, KeyEvent.VK_EQUALS);      break;
            case '~': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_NUMBER_SIGN); break;
            case '!': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_1); break;
            case '@': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_QUOTE); break;
            case '#': type(robot, KeyEvent.VK_NUMBER_SIGN); break;
            case '$': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_4); break;
            case '%': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_5); break;
            case '^': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_6); break;
            case '&': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_7); break;
            case '*': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_8); break;
            case '(': type(robot, KeyEvent.VK_LEFT_PARENTHESIS); break;
            case ')': type(robot, KeyEvent.VK_RIGHT_PARENTHESIS); break;
            case '_': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_MINUS);  break;
            case '+': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_EQUALS); break;
            case '\t': type(robot, KeyEvent.VK_TAB);        break;
            case '\n': type(robot, KeyEvent.VK_ENTER);      break;
            case '[': type(robot, KeyEvent.VK_OPEN_BRACKET); break;
            case ']': type(robot, KeyEvent.VK_CLOSE_BRACKET); break;
            case '\\': type(robot, KeyEvent.VK_BACK_SLASH); break;
            case '{': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET); break;
            case '}': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET); break;
            case '|': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH); break;
            case ';': type(robot, KeyEvent.VK_SEMICOLON);   break;
            case ':': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON);       break;
            case '\'': type(robot, KeyEvent.VK_QUOTE);      break;
            case '"': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_2);    break;
            case ',': type(robot, KeyEvent.VK_COMMA);       break;
            case '<': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA); break;
            case '>': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD); break;
            case '.': type(robot, KeyEvent.VK_PERIOD);      break;
            case '/': type(robot, KeyEvent.VK_SLASH);       break;
            case '?': type(robot, KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH); break;
            case ' ': type(robot, KeyEvent.VK_SPACE);       break;

            default:
                // A-Z
                if (key >= 65 && key <= 90)
                {
                    type(robot, KeyEvent.VK_SHIFT, KeyEvent.getExtendedKeyCodeForChar(key));
                }
                else
                {
                    // last ditch attempt...
                    type(robot, KeyEvent.getExtendedKeyCodeForChar(key));
                }
                break;
        }
    }

    private synchronized void type(Robot robot, int... keyCodes)
    {
        // press all the keys down
        for (int keyCode : keyCodes)
        {
            robot.keyPress(keyCode);
        }

        // release them all
        for (int keyCode : keyCodes)
        {
            robot.keyRelease(keyCode);
        }
    }

}
