package com.limpygnome.parrot.component.ui;

import com.limpygnome.parrot.component.backup.BackupService;
import com.limpygnome.parrot.component.browser.BrowserService;
import com.limpygnome.parrot.component.buildInfo.BuildInfoService;
import com.limpygnome.parrot.component.clipboard.ClipboardService;
import com.limpygnome.parrot.component.database.DatabaseOptimizerService;
import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.importExport.ImportExportService;
import com.limpygnome.parrot.component.sync.SyncProfileService;
import com.limpygnome.parrot.component.sync.SyncResultService;
import com.limpygnome.parrot.component.sync.SyncThreadService;
import com.limpygnome.parrot.component.ui.preferences.WindowPreferencesInitService;
import com.limpygnome.parrot.lib.database.EncryptedValueService;
import com.limpygnome.parrot.component.randomGenerator.RandomGeneratorService;
import com.limpygnome.parrot.component.recentFile.RecentFileService;
import com.limpygnome.parrot.component.sync.SyncService;
import com.limpygnome.parrot.component.runtime.RuntimeService;
import com.limpygnome.parrot.component.sendKeys.SendKeysService;
import com.limpygnome.parrot.component.settings.SettingsService;
import com.limpygnome.parrot.lib.WebViewDebug;
import com.limpygnome.parrot.lib.init.WebViewInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * The facade for holding instances of common (shared) runtime services.
 */
@Service
public class WebStageInitService
{
    private static final Logger LOG = LoggerFactory.getLogger(WebStageInitService.class);

    // Services
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private RandomGeneratorService randomGeneratorService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private BackupService backupService;
    @Autowired
    private RecentFileService recentFileService;
    @Autowired
    private EncryptedValueService encryptedValueService;
    @Autowired
    private BuildInfoService buildInfoService;
    @Autowired
    private DatabaseOptimizerService databaseOptimizerService;
    @Autowired
    private SendKeysService sendKeysService;
    @Autowired
    private ImportExportService importExportService;
    @Autowired
    private ClipboardService clipboardService;
    @Autowired
    private BrowserService browserService;
    @Autowired
    private SyncService syncService;
    @Autowired
    private SyncProfileService syncProfileService;
    @Autowired
    private SyncResultService syncResultService;

    @Autowired(required = false)
    private WebViewDebug webViewDebug;

    @Qualifier("dev")
    @Autowired(required = false)
    private WebViewInit webViewInitDev;
    @Qualifier("default")
    @Autowired
    private WebViewInit webViewInitDefault;

    @Autowired
    private WindowPreferencesInitService windowPreferencesInitService;

    // Stage
    private WebViewStage stage;

    /**
     * Attaches this controller instance to a stage. Can only be done once.
     */
    public void attach(WebViewStage stage)
    {
        this.stage = stage;

        // Inject required objects into front-end
        // WARNING: due to JavaFX "bug", never pass newly constructed instances here

        stage.exposeJsObject("settingsService", settingsService);
        stage.exposeJsObject("runtimeService", runtimeService);
        stage.exposeJsObject("databaseService", databaseService);
        stage.exposeJsObject("randomGeneratorService", randomGeneratorService);
        stage.exposeJsObject("backupService", backupService);
        stage.exposeJsObject("recentFileService", recentFileService);
        stage.exposeJsObject("encryptedValueService", encryptedValueService);
        stage.exposeJsObject("buildInfoService", buildInfoService);
        stage.exposeJsObject("databaseOptimizerService", databaseOptimizerService);
        stage.exposeJsObject("sendKeysService", sendKeysService);
        stage.exposeJsObject("importExportService", importExportService);
        stage.exposeJsObject("clipboardService", clipboardService);
        stage.exposeJsObject("browserService", browserService);
        stage.exposeJsObject("syncService", syncService);
        stage.exposeJsObject("syncProfileService", syncProfileService);
        stage.exposeJsObject("syncResultService", syncResultService);

        // Runtime is now ready
        runtimeService.setReady(true);
    }

    /**
     * @return the stage associated with this Spring context
     */
    public WebViewStage getStage()
    {
        return stage;
    }

    /**
     * @return current instance of database service
     */
    public DatabaseService getDatabaseService()
    {
        return databaseService;
    }

    /**
     * @return current instance of runtime service
     */
    public RuntimeService getRuntimeService()
    {
        return runtimeService;
    }

    /**
     * @return instance for debugging web view
     */
    public WebViewDebug getWebViewDebug()
    {
        return webViewDebug;
    }

    /**
     * @return instance for initializing web view
     */
    public WebViewInit getWebViewInit()
    {
        return webViewInitDev != null ? webViewInitDev : webViewInitDefault;
    }

    /**
     * @see {@link WebViewStage#triggerEvent(String, String, Object)}
     */
    public void triggerEvent(String domElement, String eventName, Object eventData)
    {
        if (stage != null)
        {
            LOG.debug("triggering web event - domElement={} eventName={} objectData={}", domElement, eventName, eventData);
            stage.triggerEvent(domElement, eventName, eventData);
        }
        else
        {
            LOG.warn("not triggering event as stage is not setup - eventName={}", eventName);
        }
    }

    WindowPreferencesInitService getWindowPreferencesInitService()
    {
        return windowPreferencesInitService;
    }

}
