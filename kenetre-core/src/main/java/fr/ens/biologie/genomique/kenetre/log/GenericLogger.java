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
