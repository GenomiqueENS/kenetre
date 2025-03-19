package fr.ens.biologie.genomique.kenetre.log;

import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class define an abstract logger for the GenericLogger implementations that use
 * java.util.logging API.
 *
 * @author Laurent Jourdren
 */
public abstract class AbstractJavaUtilLoggingLogger implements GenericLogger {

  private static final String DEFAULT_LOG_LEVEL = "INFO";

  /** Format of the log. */
  public static final Formatter LOG_FORMATTER =
      new Formatter() {

        private final DateFormat df = new SimpleDateFormat("yyyy.MM.dd kk:mm:ss", Locale.US);

        @Override
        public String format(final LogRecord record) {
          return record.getLevel()
              + "\t"
              + this.df.format(new Date(record.getMillis()))
              + "\t"
              + record.getMessage()
              + "\n";
        }
      };

  private final String loggerName;

  /**
   * Get the Java logger. Compatibility method.
   *
   * @return the Java logger
   */
  public Logger getLogger() {
    return Logger.getLogger(this.loggerName);
  }

  @Override
  public void debug(String message) {
    getLogger().fine(message);
  }

  @Override
  public void info(String message) {
    getLogger().info(message);
  }

  @Override
  public void warn(String message) {
    getLogger().warning(message);
  }

  @Override
  public void error(String message) {
    getLogger().severe(message);
  }

  @Override
  public void error(Throwable exception) {
    getLogger().severe(exception.getMessage());
  }

  @Override
  public void flush() {

    Handler[] handlers = getLogger().getHandlers();

    if (handlers != null && handlers.length > 0) {
      getLogger().getHandlers()[0].flush();
    }
  }

  @Override
  public void close() {

    Handler[] handlers = getLogger().getHandlers();

    if (handlers != null && handlers.length > 0) {
      getLogger().removeHandler(getLogger().getHandlers()[0]);
    }
  }

  //
  // Abstract methods
  //

  protected abstract Handler createHandler(Map<String, String> conf) throws KenetreException;

  //
  // Constructor
  //

  public AbstractJavaUtilLoggingLogger(String loggerName, Map<String, String> conf)
      throws KenetreException {

    requireNonNull(loggerName);

    // Set empty conf if null
    if (conf == null) {
      conf = Collections.emptyMap();
    }

    this.loggerName = loggerName;

    // Get Log level
    String logLevelName = conf.get("log.level");
    if (logLevelName == null) {
      logLevelName = DEFAULT_LOG_LEVEL;
    }
    Level logLevel = Level.parse(logLevelName.toUpperCase());

    final Logger logger = getLogger();

    logger.setLevel(Level.OFF);

    // Remove default Handler
    logger.removeHandler(logger.getParent().getHandlers()[0]);

    // Set default log level
    logger.setLevel(logLevel);

    final Handler fh = createHandler(conf);
    fh.setFormatter(LOG_FORMATTER);

    logger.setUseParentHandlers(false);

    // Remove default Handler
    logger.removeHandler(logger.getParent().getHandlers()[0]);

    logger.addHandler(fh);
  }
}
