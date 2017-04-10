package com.limpygnome.parrot.component.ui;

import com.limpygnome.parrot.component.backup.BackupService;
import com.limpygnome.parrot.component.buildInfo.BuildInfoService;
import com.limpygnome.parrot.component.database.DatabaseOptimizerService;
import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.database.EncryptedValueService;
import com.limpygnome.parrot.component.randomGenerator.RandomGeneratorService;
import com.limpygnome.parrot.component.recentFile.RecentFileService;
import com.limpygnome.parrot.component.remote.RemoteSshFileService;
import com.limpygnome.parrot.component.runtime.RuntimeService;
import com.limpygnome.parrot.component.settings.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The facade for holding instances of common (shared) runtime services.
 *
 * TODO: get rid of accessors, should be no need for it; we'll almost just want an auto-wiring bean/instance for
 * inecting beans into stage
 */
@Service
public class WebStageInitService
{
    // Services
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private RandomGeneratorService randomGeneratorService;
    @Autowired
    private RemoteSshFileService remoteSshFileService;
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

    // Properties
    @Value("${development:false}")
    private Boolean developmentMode;

    // Stage
    private WebViewStage stage;

    /**
     * Attaches this controller instance to a stage. Can only be done once.
     */
    public void attach(WebViewStage stage)
    {
        // Pass stage onto services which need to know about it
        this.stage = stage;

        // Inject required objects into front-end
        // WARNING: due to JavaFX "bug", never pass newly constructed instances here

        // -- Services
        stage.exposeJsObject("settingsService", settingsService);
        stage.exposeJsObject("runtimeService", runtimeService);
        stage.exposeJsObject("databaseService", databaseService);
        stage.exposeJsObject("randomGeneratorService", randomGeneratorService);
        stage.exposeJsObject("remoteSshFileService", remoteSshFileService);
        stage.exposeJsObject("backupService", backupService);
        stage.exposeJsObject("recentFileService", recentFileService);
        stage.exposeJsObject("encryptedValueService", encryptedValueService);
        stage.exposeJsObject("buildInfoService", buildInfoService);
        stage.exposeJsObject("databaseOptimizerService", databaseOptimizerService);
    }

    /**
     * @return indicates if running in development mode
     */
    public boolean isDevelopmentMode()
    {
        return developmentMode;
    }

    /**
     * @return the stage associated with this Spring context
     */
    public WebViewStage getStage()
    {
        return stage;
    }


    public DatabaseService getDatabaseService()
    {
        return databaseService;
    }

    public RuntimeService getRuntimeService()
    {
        return runtimeService;
    }

    public EncryptedValueService getEncryptedValueService()
    {
        return encryptedValueService;
    }

}
