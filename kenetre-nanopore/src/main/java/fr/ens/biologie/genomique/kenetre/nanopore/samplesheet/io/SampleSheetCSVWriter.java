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
