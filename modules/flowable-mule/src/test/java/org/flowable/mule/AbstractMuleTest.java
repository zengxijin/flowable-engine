package org.flowable.mule;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.common.impl.interceptor.CommandConfig;
import org.flowable.engine.impl.ProcessEngineImpl;
import org.flowable.engine.impl.db.DbSqlSession;
import org.flowable.engine.impl.interceptor.Command;
import org.flowable.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.interceptor.CommandExecutor;
import org.junit.Assert;
import org.mule.tck.junit4.FunctionalTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMuleTest extends FunctionalTestCase {

  protected static Logger log = LoggerFactory.getLogger(AbstractMuleTest.class);

  protected static final String EMPTY_LINE = "                                                                                           ";
  private static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList("ACT_GE_PROPERTY", "ACT_ID_PROPERTY");

  /**
   * Each test is assumed to clean up all DB content it entered. After a test method executed, this method scans all tables to see if the DB is completely clean. It throws AssertionFailed in case the
   * DB is not clean. If the DB is not clean, it is cleaned by performing a create a drop.
   */
  protected void assertAndEnsureCleanDb(ProcessEngine processEngine) throws Exception {
    log.debug("verifying that db is clean after test");
    Map<String, Long> tableCounts = processEngine.getManagementService().getTableCount();
    StringBuilder outputMessage = new StringBuilder();
    for (String tableName : tableCounts.keySet()) {
      String tableNameWithoutPrefix = tableName.replace(processEngine.getProcessEngineConfiguration().getDatabaseTablePrefix(), "");
      if (!TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.contains(tableNameWithoutPrefix)) {
        Long count = tableCounts.get(tableName);
        if (count != 0L) {
          outputMessage.append("  " + tableName + ": " + count + " record(s) ");
        }
      }
    }
    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "DB NOT CLEAN: \n");
      log.error(EMPTY_LINE);
      log.error(outputMessage.toString());

      log.info("dropping and recreating db");

      CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getCommandExecutor();
      CommandConfig config = new CommandConfig().transactionNotSupported();
      commandExecutor.execute(config, new Command<Object>() {
        public Object execute(CommandContext commandContext) {
          DbSqlSession session = commandContext.getDbSqlSession();
          session.dbSchemaDrop();
          session.dbSchemaCreate();
          return null;
        }
      });

      Assert.fail(outputMessage.toString());

    } else {
      log.info("database was clean");
    }
  }

}
