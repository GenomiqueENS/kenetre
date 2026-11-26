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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * This class allow to automatically detect the format of an expression matrix file.
 *
 * @author Laurent Jourdren
 * @since 2.4
 */
public class ExpressionMatrixFormatFinderInputStream extends InputStream {

  public enum MatrixFormat {
    TSV,
    SPARSE,
    MARKET_MATRIX,
    UNKNOWN
  }

  private InputStream is;

  private static final int CACHE_SIZE = 100000;
  private static final int MAX_LINES_TO_READ = 1;

  private MatrixFormat format;
  private boolean testFormatDone;

  private int cacheIndex;
  private byte[] cache;

  /**
   * Find the matrix format.
   *
   * @return the matrix format
   * @throws IOException if an error occurs while finding the version
   */
  private MatrixFormat findMatrixFormat() throws IOException {

    final byte[] readed = new byte[CACHE_SIZE];

    int count = is.read(readed);

    if (count != CACHE_SIZE) {
      this.cache = new byte[count];
      System.arraycopy(readed, 0, this.cache, 0, count);
    } else {
      this.cache = readed;
    }

    try (final BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                new ByteArrayInputStream(this.cache), Charset.defaultCharset()))) {

      String line;
      int lineCount = 0;

      while (((line = reader.readLine()) != null) && lineCount < MAX_LINES_TO_READ) {

        line = line.trim();

        if ("".equals(line) || line.startsWith("#")) {
          continue;
        }

        if (line.startsWith(SparseExpressionMatrixWriter.HEADER)) {
          return MatrixFormat.SPARSE;
        }

        if (line.startsWith(MarketMatrixExpressionMatrixReader.MAGIC_KEY)) {
          return MatrixFormat.MARKET_MATRIX;
        }

        if (line.startsWith("\t")) {
          return MatrixFormat.TSV;
        }

        lineCount++;
      }
    }

    return MatrixFormat.UNKNOWN;
  }

  /**
   * Get the format of the data to read.
   *
   * @return The format of the data to read
   * @throws IOException if an error occurs while reading data
   */
  public MatrixFormat getExpressionMatrixFormat() throws IOException {

    if (!this.testFormatDone) this.format = findMatrixFormat();

    return this.format;
  }

  /**
   * Get the ExpressionMatrixReader for the data.
   *
   * @return the ExpressionMatrixReader for the data
   * @throws IOException if an error occurs while reading data
   */
  public ExpressionMatrixReader getExpressionMatrixReader() throws IOException {

    switch (getExpressionMatrixFormat()) {
      case TSV:
        return new TSVExpressionMatrixReader(this);

      case SPARSE:
        return new SparseExpressionMatrixReader(this);

      case MARKET_MATRIX:
        return new MarketMatrixExpressionMatrixReader(this);

      case UNKNOWN:
      default:
        throw new IOException("Unknown Design format");
    }
  }

  //
  // InputStream methods
  //

  @Override
  public int read() throws IOException {

    if (this.cacheIndex == -1) {
      return -1;
    }

    if (this.cacheIndex < this.cache.length) return this.cache[this.cacheIndex++];

    return this.is.read();
  }

  @Override
  public void close() throws IOException {

    this.is.close();
    this.cacheIndex = -1;
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   *
   * @param is InputStream to read
   */
  public ExpressionMatrixFormatFinderInputStream(final InputStream is) {

    if (is == null) {
      throw new NullPointerException("The inputStream is null");
    }

    this.is = is;
  }
}
