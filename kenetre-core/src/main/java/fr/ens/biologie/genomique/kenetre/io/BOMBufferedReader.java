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
