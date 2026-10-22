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

import fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;

/**
 * This class define a writer for Nanopore software CSV sample sheet files.
 *
 * @since 0.20
 * @author Laurent Jourdren
 */
public class SampleSheetCSVWriter implements SampleSheetWriter, AutoCloseable {

  private final Writer writer;

  @Override
  public void writer(final SampleSheet sampleSheet) throws IOException {

    this.writer.write(sampleSheet.toCSV());
  }

  @Override
  public void close() throws IOException {
    this.writer.close();
  }

  //
  // Constructors
  //

  /**
   * Public constructor.
   *
   * @param writer Writer to use
   */
  public SampleSheetCSVWriter(final Writer writer) {

    if (writer == null) {
      throw new NullPointerException("The writer is null.");
    }

    this.writer = writer;
  }

  /**
   * Public constructor.
   *
   * @param os OutputStream to use
   */
  public SampleSheetCSVWriter(final OutputStream os) {

    this.writer = new OutputStreamWriter(os);
  }

  /**
   * Public constructor.
   *
   * @param outputFile file to use
   * @throws IOException if an error occurs while creating the file
   */
  public SampleSheetCSVWriter(final File outputFile) throws IOException {

    this.writer = new FileWriter(outputFile, defaultCharset());
  }

  /**
   * Public constructor.
   *
   * @param outputFile file to use
   * @throws IOException if an error occurs while creating the file
   */
  public SampleSheetCSVWriter(final Path outputFile) throws IOException {

    this(outputFile.toFile());
  }

  /**
   * Public constructor.
   *
   * @param outputFilename name of the file to use
   * @throws IOException if an error occurs while creating the file
   */
  public SampleSheetCSVWriter(final String outputFilename) throws IOException {

    this.writer = new FileWriter(outputFilename, defaultCharset());
  }
}
