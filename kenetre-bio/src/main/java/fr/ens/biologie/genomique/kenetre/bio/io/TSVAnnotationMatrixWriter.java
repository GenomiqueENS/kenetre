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

import fr.ens.biologie.genomique.kenetre.bio.AnnotationMatrix;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Objects;

/**
 * This class define an AnnotationMatrix writer for TSV format.
 *
 * @author Laurent Jourdren
 * @since 2.4
 */
public class TSVAnnotationMatrixWriter implements AnnotationMatrixWriter {

  private final OutputStream os;

  @Override
  public void write(final AnnotationMatrix matrix) throws IOException {

    Objects.requireNonNull(matrix, "matrix argument cannot be null");

    write(matrix, matrix.getRowNames());
  }

  @Override
  public void write(final AnnotationMatrix matrix, final Collection<String> rowNamesToWrite)
      throws IOException {

    Objects.requireNonNull(matrix, "matrix argument cannot be null");
    Objects.requireNonNull(rowNamesToWrite, "rowNamesToWrite argument cannot be null");

    try (Writer writer = new OutputStreamWriter(this.os)) {

      StringBuilder sb = new StringBuilder();

      sb.append("Id");
      for (String columnName : matrix.getColumnNames()) {
        sb.append('\t');
        sb.append(columnName);
      }
      sb.append('\n');
      writer.write(sb.toString());

      for (String rowName : rowNamesToWrite) {
        sb.setLength(0);

        sb.append(rowName);

        for (String value : matrix.getRowValues(rowName)) {
          sb.append('\t');
          sb.append(value);
        }
        sb.append('\n');
        writer.write((sb.toString()));
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
  public TSVAnnotationMatrixWriter(final OutputStream os) {

    Objects.requireNonNull(os, "the os argument cannot be null");

    this.os = os;
  }

  /**
   * Public constructor.
   *
   * @param outputFile file to use
   * @throws IOException if an error occurs while creating the file
   */
  public TSVAnnotationMatrixWriter(final File outputFile) throws IOException {

    Objects.requireNonNull(outputFile, "the outputFile argument cannot be null");

    this.os = new FileOutputStream(outputFile);
  }

  /**
   * Public constructor.
   *
   * @param outputFilename name of the file to use
   * @throws IOException if an error occurs while creating the file
   */
  public TSVAnnotationMatrixWriter(final String outputFilename) throws IOException {

    Objects.requireNonNull(outputFilename, "the outputFilename argument cannot be null");

    this.os = new FileOutputStream(outputFilename);
  }
}
