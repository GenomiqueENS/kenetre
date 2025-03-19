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
