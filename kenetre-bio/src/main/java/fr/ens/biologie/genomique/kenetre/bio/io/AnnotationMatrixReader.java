package fr.ens.biologie.genomique.kenetre.bio.io;

import fr.ens.biologie.genomique.kenetre.bio.AnnotationMatrix;
import java.io.Closeable;
import java.io.IOException;

/**
 * This interface define an AnnotationMatrix reader.
 *
 * @author Laurent Jourdren
 * @since 2.4
 */
public interface AnnotationMatrixReader extends Closeable {

  /**
   * Read an AnnotationMatrix object.
   *
   * @return an AnnotationMatrix object
   * @throws IOException if an error occurs while reading the file
   */
  AnnotationMatrix read() throws IOException;

  /**
   * Read an AnnotationMatrix object.
   *
   * @param matrix matrix to use for saving data loaded
   * @return an ExpressionMatrix object
   * @throws IOException if an error occurs while reading the file
   */
  AnnotationMatrix read(AnnotationMatrix matrix) throws IOException;
}
