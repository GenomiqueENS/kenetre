package fr.ens.biologie.genomique.kenetre.log;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * This class define a logger using the java.util.logging API that write log on standard error.
 *
 * @author Laurent Jourdren
 */
public class StandardErrorLogger extends AbstractJavaUtilLoggingLogger {

  @Override
  protected Handler createHandler(Map<String, String> conf) throws KenetreException {

    Handler result =
        new StreamHandler(System.err, new SimpleFormatter()) {

          // Force flush after each log
          @Override
          public synchronized void publish(final LogRecord record) {
            super.publish(record);
            flush();
          }
        };

    if (conf.containsKey("log.level")) {
      String logLevelName = conf.get("log.level");
      Level logLevel = Level.parse(logLevelName.toUpperCase());
      result.setLevel(logLevel);
    }

    return result;
  }

  //
  // Constructor
  //

  /**
   * Constructor.
   *
   * @param loggerName name of the logger
   * @param conf configuration
   * @throws KenetreException if an error occurs while creating the logger
   */
  public StandardErrorLogger(String loggerName, Map<String, String> conf) throws KenetreException {
    super(loggerName, conf);
  }
}
