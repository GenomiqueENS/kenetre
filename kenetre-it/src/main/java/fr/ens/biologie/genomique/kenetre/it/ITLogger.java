package fr.ens.biologie.genomique.kenetre.it;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ITLogger {

  /** Format of the log. */
  public static final Formatter LOG_FORMATTER =
      new Formatter() {

        private final DateFormat df =
            new SimpleDateFormat("yyyy.MM.dd kk:mm:ss", IT.DEFAULT_LOCALE);

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

  /**
   * Get the logger.
   *
   * @return the logger
   */
  public static Logger getLogger() {

    return Logger.getLogger("itlogger");
  }

  /**
   * Initialize logger.
   *
   * @param loggerPath log file path
   * @throws IOException if an error occurs while create logger
   */
  public static void initLogger(String loggerPath) throws IOException {

    // Remove default logger
    getLogger().setLevel(Level.OFF);

    // Remove default Handler
    getLogger().removeHandler(getLogger().getParent().getHandlers()[0]);

    // try {
    // initEoulsanRuntimeForExternalApp();
    // } catch (final KenetreException ee) {
    // ee.printStackTrace();
    // }

    Handler fh = null;
    try {
      fh = new FileHandler(loggerPath);

    } catch (final Exception e) {
      throw new IOException(e);
    }

    fh.setFormatter(LOG_FORMATTER);

    getLogger().setLevel(Level.ALL);
    // Remove output console
    getLogger().setUseParentHandlers(false);
    getLogger().addHandler(fh);
  }
}
