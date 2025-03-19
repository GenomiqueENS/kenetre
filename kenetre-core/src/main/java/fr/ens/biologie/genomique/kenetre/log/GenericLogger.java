package fr.ens.biologie.genomique.kenetre.log;

import fr.ens.biologie.genomique.kenetre.util.StringUtils;

/**
 * This interface define a generic logger for Eoulsan.
 *
 * @author Laurent Jourdren
 * @since 2.6
 */
public interface GenericLogger {

  /**
   * Log a debug message.
   *
   * @param message message to log
   */
  void debug(String message);

  /**
   * Log an info message.
   *
   * @param message message to log
   */
  void info(String message);

  /**
   * Log a warning message.
   *
   * @param message message to log
   */
  void warn(String message);

  /**
   * Log an error message.
   *
   * @param message message to log
   */
  void error(String message);

  /**
   * Log an error message.
   *
   * @param exception exception to log
   */
  default void error(Throwable exception) {

    error(exception, false);
  }

  /**
   * Log an error message.
   *
   * @param exception exception to log
   */
  default void error(Throwable exception, boolean logStackTrace) {

    error(
        exception != null
            ? exception.getMessage()
                + (logStackTrace ? "\n" + StringUtils.stackTraceToString(exception) : "")
            : "exception is null");
  }

  /** Flush log entries. */
  void flush();

  /** Close the logger */
  void close();
}
