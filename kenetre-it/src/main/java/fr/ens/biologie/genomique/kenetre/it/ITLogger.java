/*
 *                  Eoulsan development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public License version 2.1 or
 * later and CeCILL-C. This should be distributed with the code.
 * If you do not have a copy, see:
 *
 *      http://www.gnu.org/licenses/lgpl-2.1.txt
 *      http://www.cecill.info/licences/Licence_CeCILL-C_V1-en.txt
 *
 * Copyright for this code is held jointly by the Genomic platform
 * of the Institut de Biologie de l'École normale supérieure and
 * the individual authors. These should be listed in @author doc
 * comments.
 *
 * For more information on the Eoulsan project and its aims,
 * or to join the Eoulsan Google group, visit the home page
 * at:
 *
 *      http://outils.genomique.biologie.ens.fr/eoulsan
 *
 */

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
