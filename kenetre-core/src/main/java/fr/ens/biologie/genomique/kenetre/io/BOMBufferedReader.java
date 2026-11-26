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

package fr.ens.biologie.genomique.kenetre.io;

import static fr.ens.biologie.genomique.kenetre.util.StringUtils.removeUTF8BOM;
import static java.nio.charset.Charset.defaultCharset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class define a Buffered reader that reader the BOM character at the beginning of the file if
 * exists.
 *
 * @author Laurent Jourdren
 * @since 0.24
 */
public class BOMBufferedReader extends BufferedReader {

  private int lineCount;

  @Override
  public String readLine() throws IOException {

    String s = super.readLine();
    this.lineCount++;

    if (this.lineCount == 1) {
      s = removeUTF8BOM(s);
    }

    return s;
  }

  //
  // Constructors
  //

  /**
   * Public constructor.
   *
   * @param is InputStream to use
   */
  public BOMBufferedReader(final InputStream is) {

    super(new InputStreamReader(is, defaultCharset()));
  }

  /**
   * Public constructor.
   *
   * @param file File to use
   * @throws IOException if the file does not exists
   */
  public BOMBufferedReader(final File file) throws IOException {

    super(new FileReader(file, defaultCharset()));
  }

  /**
   * Public constructor.
   *
   * @param path File to use
   * @throws IOException if the file does not exists
   */
  public BOMBufferedReader(final Path path) throws IOException {

    super(new InputStreamReader(Files.newInputStream(path), defaultCharset()));
  }

  /**
   * Public constructor.
   *
   * @param filename File to use
   * @throws IOException if an error occurs while reading the file
   */
  public BOMBufferedReader(final String filename) throws IOException {

    super(new FileReader(filename, defaultCharset()));
  }

  /**
   * Public constructor.
   *
   * @param in Reader to use
   */
  public BOMBufferedReader(Reader in) {

    super(in);
  }
}
