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

package fr.ens.biologie.genomique.kenetre;

/**
 * This class define an KenetreException exception
 *
 * @author Laurent Jourdren
 * @since 1.0
 */
public class KenetreException extends Exception {

  private static final long serialVersionUID = 2877759048606105665L;

  /** Create a new Aozan3Exception. */
  public KenetreException() {

    super();
  }

  /**
   * Create a new KenetreException with a message.
   *
   * @param message the message
   */
  public KenetreException(final String message) {

    super(message);
  }

  /**
   * Create a new Aozan3Exception with a message and a cause.
   *
   * @param message the message
   * @param cause the cause
   */
  public KenetreException(String message, Throwable cause) {

    super(message, cause);
  }

  /**
   * Create a new Aozan3Exception with a cause.
   *
   * @param cause the cause
   */
  public KenetreException(Throwable cause) {

    super(cause);
  }
}
