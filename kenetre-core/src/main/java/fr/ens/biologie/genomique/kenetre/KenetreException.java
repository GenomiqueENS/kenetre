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
