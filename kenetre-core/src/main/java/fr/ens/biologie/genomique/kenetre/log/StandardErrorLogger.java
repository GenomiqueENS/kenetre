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
