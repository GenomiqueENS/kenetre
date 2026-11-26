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
 * A nestable Kenetre runtime exception. This class came from from BioJava Code. In Eoulsan, checked
 * exceptions are generally preferred to RuntimeExceptions, but RuntimeExceptions can be used as a
 * fall-back if you are implementing an interface which doesn't support checked exceptions. If you
 * do this, please document this clearly in the implementing class.
 *
 * @since 0.8
 * @author Laurent Jourdren
 * @author Matthew Pocock
 * @author Thomas Down
 */
public class KenetreRuntimeException extends RuntimeException {

  //
  // Constructors
  //

  private static final long serialVersionUID = -5805999860690016234L;

  /**
   * Create a new AozanRuntimeException with a message and a cause.
   *
   * @param message the message
   * @param cause the cause
   */
  public KenetreRuntimeException(String message, Throwable cause) {

    super(message, cause);
  }

  /**
   * Create a new AozanRuntimeException with a cause.
   *
   * @param cause the cause
   */
  public KenetreRuntimeException(Throwable cause) {

    super(cause);
  }

  /**
   * Create a new AozanRuntimeException with a message.
   *
   * @param message the message
   */
  public KenetreRuntimeException(final String message) {
    // setMessage(message);
    super(message);
  }

  /** Create a new AozanRuntimeException. */
  public KenetreRuntimeException() {
    super();
  }
}
