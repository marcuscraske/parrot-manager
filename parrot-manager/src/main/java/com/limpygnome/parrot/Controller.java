package com.limpygnome.parrot;

import com.limpygnome.parrot.model.params.CryptoParamsFactory;
import com.limpygnome.parrot.service.DatabaseService;
import com.limpygnome.parrot.service.RandomGeneratorService;
import com.limpygnome.parrot.service.RemoteSshFileService;
import com.limpygnome.parrot.service.RuntimeService;
import com.limpygnome.parrot.service.CryptographyService;
import com.limpygnome.parrot.service.DatabaseIOService;
import com.limpygnome.parrot.ui.WebViewStage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The facade for holding instances of common (shared) runtime services.
 *
 * TODO: get rid of accessors, should be no need for it; we'll almost just want an auto-wiring bean/instance for
 * inecting beans into stage
 */
@Component
public class Controller
{
    // Services
    @Autowired
    private CryptographyService cryptographyService;
    @Autowired
    private DatabaseIOService databaseIOService;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private RandomGeneratorService randomGeneratorService;
    @Autowired
    private RemoteSshFileService remoteSshFileService;
    @Autowired
    private RuntimeService runtimeService;

    // Components
    @Autowired
    private CryptoParamsFactory cryptoParamsFactory;

    // Properties
    private boolean developmentMode;

    public Controller()
    {
        this.developmentMode = false;
    }

    public Controller(boolean developmentMode)
    {
        this.developmentMode = developmentMode;
    }

    /**
     * Attaches this controller instance to a stage. Can only be done once.
     */
    public void attach(WebViewStage stage)
    {
        // Pass stage onto services which need to know about it
        runtimeService.setStage(stage);

        // Inject required services into front-end
        stage.exposeJsObject("runtimeService", runtimeService);
        stage.exposeJsObject("databaseService", databaseService);
        stage.exposeJsObject("randomGeneratorService", randomGeneratorService);
        stage.exposeJsObject("remoteSshFileService", remoteSshFileService);
    }

    public DatabaseService getDatabaseService()
    {
        return databaseService;
    }

    public RandomGeneratorService getRandomGeneratorService()
    {
        return randomGeneratorService;
    }

    public RemoteSshFileService getRemoteSshFileService()
    {
        return remoteSshFileService;
    }

    public RuntimeService getRuntimeService()
    {
        return runtimeService;
    }

    public CryptographyService getCryptographyService() {
        return cryptographyService;
    }

    public DatabaseIOService getDatabaseIOService() {
        return databaseIOService;
    }

    public CryptoParamsFactory getCryptoParamsFactory()
    {
        return cryptoParamsFactory;
    }

    /**
     * @return indicates if running in development mode
     */
    public boolean isDevelopmentMode()
    {
        return developmentMode;
    }

}
