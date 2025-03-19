package fr.ens.biologie.genomique.kenetre.bio.io;

import fr.ens.biologie.genomique.kenetre.bio.ExpressionMatrix;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

/**
 * This interface define an ExpressionMatrix writer.
 *
 * @author Laurent Jourdren
 * @since 2.0
 */
public interface ExpressionMatrixWriter extends Closeable {

  /**
   * Write an ExpressionMatrix object.
   *
   * @param matrix matrix to write
   * @throws IOException if an error occurs while writing the file
   */
  void write(ExpressionMatrix matrix) throws IOException;

  /**
   * Write an ExpressionMatrix object.
   *
   * @param matrix matrix to write
   * @param rowNamesToWrite row names to write
   * @throws IOException if an error occurs while writing the file
   */
  void write(ExpressionMatrix matrix, final Collection<String> rowNamesToWrite) throws IOException;
}
