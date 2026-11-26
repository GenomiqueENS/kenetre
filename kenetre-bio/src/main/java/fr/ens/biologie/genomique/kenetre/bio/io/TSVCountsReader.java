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

import com.google.common.base.Splitter;
import fr.ens.biologie.genomique.kenetre.util.GuavaCompatibility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class define a counts reader for TSV format.
 *
 * @author Laurent Jourdren
 * @since 2.4
 */
public class TSVCountsReader implements CountsReader {

  private InputStream is;

  @Override
  public Map<String, Integer> read() throws IOException {

    final Map<String, Integer> result = new LinkedHashMap<>();

    String line = null;
    int lineCount = 0;

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.is))) {

      Splitter splitter = Splitter.on('\t').trimResults();

      boolean first = true;
      while ((line = reader.readLine()) != null) {

        line = line.trim();
        lineCount++;

        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }

        if (first) {
          first = false;
          continue;
        }

        List<String> fields = GuavaCompatibility.splitToList(splitter, line);
        if (fields.size() != 2) {
          throw new IOException(
              "Invalid number of fields found line "
                  + lineCount
                  + ", 2 fields are expected: "
                  + line);
        }

        try {
          result.put(fields.get(0), Integer.parseInt(fields.get(1)));
        } catch (NumberFormatException e) {
          throw new IOException("Invalid count found line " + lineCount + ": " + line);
        }
      }
    }

    return result;
  }

  @Override
  public void close() throws IOException {

    this.is.close();
  }

  //
  // Constructors
  //

  /**
   * Public constructor
   *
   * @param is InputStream to use
   */
  public TSVCountsReader(final InputStream is) {

    Objects.requireNonNull(is, "InputStream is null");

    this.is = is;
  }

  /**
   * Public constructor
   *
   * @param file File to use
   * @throws FileNotFoundException if the file does not exists
   */
  public TSVCountsReader(final File file) throws FileNotFoundException {

    Objects.requireNonNull(file, "file argument is null");

    this.is = new FileInputStream(file);
  }

  /**
   * Public constructor.
   *
   * @param filename File to use
   * @throws FileNotFoundException if the file does not exists
   */
  public TSVCountsReader(final String filename) throws FileNotFoundException {

    Objects.requireNonNull(filename, "filename argument is null");

    this.is = new FileInputStream(filename);
  }
}
