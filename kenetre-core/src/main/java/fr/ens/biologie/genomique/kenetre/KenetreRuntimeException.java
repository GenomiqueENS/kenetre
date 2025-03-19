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
