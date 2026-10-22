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

package fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.io;

import static java.nio.charset.Charset.defaultCharset;

import com.google.common.base.Splitter;
import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * This class reads a Nanopore sample sheet file in CSV format.
 *
 * @since 0.20
 * @author Laurent Jourdren
 */
public class SampleSheetCSVReader extends AbstractSampleSheetReader implements AutoCloseable {

  private final BufferedReader reader;

  @Override
  public SampleSheet read() throws IOException {

    final SampleSheetParser parser = newSampleSheetParser();

    int lineCount = 0;
    String line = null;
    while ((line = this.reader.readLine()) != null) {

      lineCount++;
      if (line.isBlank()) {
        continue;
      }

      List<String> fields = Splitter.on(',').trimResults().splitToList(line);

      parser.parseLine(fields, lineCount);
    }

    try {
      return parser.getSampleSheet();
    } catch (KenetreException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void close() throws IOException {

    this.reader.close();
  }

  //
  // Constructors
  //

  /**
   * Public constructor
   *
   * @param is InputStream to use
   */
  public SampleSheetCSVReader(final InputStream is) {

    if (is == null) {
      throw new NullPointerException("InputStream is null");
    }

    this.reader = new BufferedReader(new InputStreamReader(is, defaultCharset()));
  }

  /**
   * Public constructor
   *
   * @param file File to use
   * @throws IOException if the file does not exists
   */
  public SampleSheetCSVReader(final File file) throws IOException {

    if (file == null) {
      throw new NullPointerException("File is null");
    }

    if (!file.isFile()) {
      throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
    }

    this.reader = new BufferedReader(new FileReader(file, defaultCharset()));
  }

  /**
   * Public constructor
   *
   * @param path File to use
   * @throws IOException if the file does not exists
   */
  public SampleSheetCSVReader(final Path path) throws IOException {

    this.reader = Files.newBufferedReader(path);
  }

  /**
   * Public constructor
   *
   * @param filename File to use
   * @throws IOException if an error occurs while reading the file
   */
  public SampleSheetCSVReader(final String filename) throws IOException {

    if (filename == null) {
      throw new NullPointerException("Filename is null");
    }

    final File file = new File(filename);

    if (!file.isFile()) {
      throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
    }

    this.reader = new BufferedReader(new FileReader(file, defaultCharset()));
  }
}
