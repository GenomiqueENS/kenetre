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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class define a TSV count writer.
 *
 * @author Laurent Jourdren
 * @since 2.4
 */
public class TSVCountsWriter implements CountsWriter {

  private final OutputStream os;

  @Override
  public void write(Map<String, Integer> counts) throws IOException {

    Objects.requireNonNull(counts, "counts argument cannot be null");

    try (Writer writer = new OutputStreamWriter(this.os)) {
      final List<String> keysSorted = new ArrayList<>(counts.keySet());
      Collections.sort(keysSorted);

      writer.write("Id\tCount\n");
      for (String key : keysSorted) {
        writer.write(key + "\t" + counts.get(key) + "\n");
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
  public TSVCountsWriter(final OutputStream os) {

    Objects.requireNonNull(os, "os argument cannot be null");

    this.os = os;
  }

  /**
   * Public constructor.
   *
   * @param outputFile file to use
   * @throws IOException if an error occurs while creating the file
   */
  public TSVCountsWriter(final File outputFile) throws IOException {

    Objects.requireNonNull(outputFile, "outputFile argument cannot be null");

    this.os = new FileOutputStream(outputFile);
  }

  /**
   * Public constructor.
   *
   * @param outputFilename name of the file to use
   * @throws IOException if an error occurs while creating the file
   */
  public TSVCountsWriter(final String outputFilename) throws IOException {

    Objects.requireNonNull(outputFilename, "outputFilename argument cannot be null");

    this.os = new FileOutputStream(outputFilename);
  }
}
