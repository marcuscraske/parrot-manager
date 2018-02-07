package com.limpygnome.parrot.component.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;

@Component
public class StandAloneComponent
{
    private static final Logger LOG = LoggerFactory.getLogger(StandAloneComponent.class);

    // Flag indicating whether app is running in stand-alone mode
    private boolean standalone;

    // Allow --standalone=true as arg to override behaviour (good for dev / temp scenario etc)
    @Value("${standalone:false}")
    private boolean standAloneOverride;

    // Root directory
    private File root;

    public StandAloneComponent()
    {
        // Check whether .stand-alone file exists on classpath
        standalone = (getClass().getResourceAsStream("/.standalone") != null);
    }

    @PostConstruct
    private void checkForOverride()
    {
        if (standAloneOverride)
        {
            standalone = true;
            LOG.info("stand-alone mode enabled - overridden by --standalone arg");
        }
        else if (standalone)
        {
            LOG.info("stand-alone mode enabled");
        }
        else
        {
            LOG.info("stnad-alone mode disabled");
        }
    }

    /**
     * @return indicates whether running in stand-alone mode
     */
    public boolean isStandalone()
    {
        return standalone;
    }

    public synchronized File getRoot()
    {
        if (root == null)
        {
            File result;

            // try to locate working directory
            String userDir = System.getProperty("user.dir");

            if (userDir != null)
            {
                result = new File(userDir);
            } else
            {
                String path = Paths.get(".").toAbsolutePath().normalize().toString();

                if (path != null)
                {
                    result = new File(path);
                } else
                {
                    result = new File("");
                }
            }

            // check path is readable/writable
            if (!result.canWrite() || !result.canRead())
            {
                throw new RuntimeException("unable to use stand-alone directory, must be readable/writable - read: " + result.canRead() + ", write: " + result.canWrite());
            }

            root = result;
            LOG.debug("stand-alone root - path: " + root.getAbsolutePath());
        }

        return root;
    }

}
