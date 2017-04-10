package com.limpygnome.parrot.component.buildInfo;

import javafx.scene.web.WebView;
import org.springframework.stereotype.Service;

/**
 * Provides information about the current build of this password manager.
 */
@Service
public class BuildInfoService
{

    /**
     * @return string with lots of build information
     */
    public String getBuildInfo()
    {
        StringBuilder buffer = new StringBuilder();

        String newLineSeparator = System.getProperty("line.separator");

        appendApplicationVersion(buffer, newLineSeparator);
        appendCurrentPlatform(buffer, newLineSeparator);
        appendCurrentJavaRuntime(buffer, newLineSeparator);
        appendJavaFxWebViewUserAgent(buffer, newLineSeparator);

        return buffer.toString();
    }

    private void appendApplicationVersion(StringBuilder buffer, String newLineSeparator)
    {
        String implementationVersion = getClass().getPackage().getImplementationVersion();
        buffer.append("Version: ").append(implementationVersion).append(newLineSeparator);
    }

    private void appendCurrentPlatform(StringBuilder buffer, String newLineSeparator)
    {
        buffer.append("Platform: ").append(System.getProperty("os.name")).append(newLineSeparator);
        buffer.append("Platform version: ").append(System.getProperty("os.version")).append(newLineSeparator);
        buffer.append("Platform architecture: ").append(System.getProperty("os.arch")).append(newLineSeparator);
    }

    private void appendCurrentJavaRuntime(StringBuilder buffer, String newLineSeparator)
    {
        buffer.append("Java vendor: ").append(System.getProperty("java.vendor")).append(newLineSeparator);
        buffer.append("Java version: ").append(System.getProperty("java.version")).append(newLineSeparator);
    }

    private void appendJavaFxWebViewUserAgent(StringBuilder buffer, String newLineSeparator)
    {
        WebView webView = new WebView();
        String userAgent = webView.getEngine().getUserAgent();
        buffer.append("JavaFX webkit version: ").append(userAgent).append(newLineSeparator);
    }

}
