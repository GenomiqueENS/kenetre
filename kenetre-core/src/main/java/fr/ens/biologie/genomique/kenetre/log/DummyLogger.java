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

import java.io.Serializable;

/**
 * This class implements a dummy logger.
 *
 * @author Laurent Jourdren
 * @since 2.6
 */
public class DummyLogger implements GenericLogger, Serializable {

  private static final long serialVersionUID = 1050015852656451990L;

  @Override
  public void debug(String message) {}

  @Override
  public void info(String message) {}

  @Override
  public void warn(String message) {}

  @Override
  public void error(String message) {}

  @Override
  public void error(Throwable exception) {}

  @Override
  public void flush() {}

  @Override
  public void close() {}
}
