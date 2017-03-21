package com.limpygnome.parrot.component.database;

import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseOptimizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Pass-through archive for optimisation functions of {@link DatabaseOptimizer}.
 */
@Service
public class DatabaseOptimizerService
{
    private static final Logger LOG = LogManager.getLogger(DatabaseOptimizerService.class);

    // Services
    @Autowired
    private DatabaseService databaseService;

    // Components
    @Autowired
    private DatabaseOptimizer databaseOptimizer;

    /**
     * @see DatabaseOptimizer#deleteAllDeletedNodeHistory(Database)
     */
    public void optimizeDeletedNodeHistory()
    {
        Database database = databaseService.getDatabase();
        databaseOptimizer.deleteAllDeletedNodeHistory(database);

        LOG.info("optimized delete node history");
    }

    /**
     * @see DatabaseOptimizer#deleteAllValueHistory(Database)
     */
    public void optimizeValueHistory()
    {
        Database database = databaseService.getDatabase();
        databaseOptimizer.deleteAllValueHistory(database);

        LOG.info("optimized value history");
    }

}
