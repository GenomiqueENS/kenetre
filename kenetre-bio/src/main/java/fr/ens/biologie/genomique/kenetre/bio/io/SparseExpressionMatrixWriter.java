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

package fr.ens.biologie.genomique.kenetre.bio.io;

import fr.ens.biologie.genomique.kenetre.bio.ExpressionMatrix;
import fr.ens.biologie.genomique.kenetre.bio.Matrix;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Objects;

/**
 * This class define an ExpressionMatrix writer for sparse format.
 *
 * @author Laurent Jourdren
 * @since 2.0
 */
public class SparseExpressionMatrixWriter implements ExpressionMatrixWriter {

  static final String HEADER = "gene\tcell\tcount";

  private final OutputStream os;

  @Override
  public void write(ExpressionMatrix matrix) throws IOException {

    Objects.requireNonNull(matrix, "matrix argument cannot be null");

    write(matrix, matrix.getRowNames());
  }

  @Override
  public void write(final ExpressionMatrix matrix, final Collection<String> rowNamesToWrite)
      throws IOException {

    Objects.requireNonNull(matrix, "matrix argument cannot be null");
    Objects.requireNonNull(rowNamesToWrite, "rowNamesToWrite argument cannot be null");

    try (Writer writer = new OutputStreamWriter(this.os)) {

      // Write header
      writer.write(HEADER + '\n');

      for (Matrix.Entry<Double> e : matrix.nonZeroValues()) {

        if (!rowNamesToWrite.contains(e.getRowName())) {
          continue;
        }

        double d = e.getValue();

        if (!(Double.isNaN(d) || Double.isInfinite(d)) && Math.floor(d) - d == 0.0) {

          int intValue = (int) d;

          writer.write(e.getRowName() + '\t' + e.getColumnName() + '\t' + intValue + '\n');

        } else {
          writer.write(e.getRowName() + '\t' + e.getColumnName() + '\t' + d + '\n');
        }
      }
    }
  }

  @Override
  public void close() throws IOException {

    this.os.close();
  }

  //
  // Constructors
  //

  /**
   * Public constructor.
   *
   * @param os OutputStream to use
   */
  public SparseExpressionMatrixWriter(final OutputStream os) {

    Objects.requireNonNull(os, "os argument cannot be null");

    this.os = os;
  }

  /**
   * Public constructor.
   *
   * @param outputFile file to use
   * @throws IOException if an error occurs while creating the file
   */
  public SparseExpressionMatrixWriter(final File outputFile) throws IOException {

    Objects.requireNonNull(outputFile, "os argument cannot be null");

    this.os = new FileOutputStream(outputFile);
  }

  /**
   * Public constructor.
   *
   * @param outputFilename name of the file to use
   * @throws IOException if an error occurs while creating the file
   */
  public SparseExpressionMatrixWriter(final String outputFilename) throws IOException {

    Objects.requireNonNull(outputFilename, "os argument cannot be null");

    this.os = new FileOutputStream(outputFilename);
  }
}
