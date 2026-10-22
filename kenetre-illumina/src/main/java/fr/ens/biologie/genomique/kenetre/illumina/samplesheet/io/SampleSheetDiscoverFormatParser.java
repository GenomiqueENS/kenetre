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

package fr.ens.biologie.genomique.kenetre.illumina.samplesheet.io;

import static fr.ens.biologie.genomique.kenetre.illumina.samplesheet.io.SampleSheetReaderUtils.trimFields;

import fr.ens.biologie.genomique.kenetre.KenetreRuntimeException;
import fr.ens.biologie.genomique.kenetre.illumina.samplesheet.SampleSheet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class allow to discover the format of a samplesheet.
 *
 * @author Laurent Jourdren
 * @since 2.0
 */
public class SampleSheetDiscoverFormatParser implements SampleSheetParser {

  private SampleSheetParser parser;
  private final List<List<String>> cache = new ArrayList<List<String>>();

  private final List<String> sampleSheetV1Hearder =
      Arrays.asList(
          "FCID",
          "Lane",
          "SampleID",
          "SampleRef",
          "Index",
          "Description",
          "Control",
          "Recipe",
          "Operator",
          "SampleProject");

  private final List<String> normalizedSampleSheetV1Hearder =
      normalizeHeader(this.sampleSheetV1Hearder);

  @Override
  public void parseLine(final List<String> fields) throws IOException {

    trimFields(fields);

    if (fields.isEmpty()) {
      return;
    }

    if (this.parser != null) {
      this.parser.parseLine(fields);
      return;
    }

    if (fields.get(0).startsWith("[")) {

      this.parser = new SampleSheetV2Parser();
      this.parser.parseLine(fields);
    } else {

      if (this.normalizedSampleSheetV1Hearder.equals(normalizeHeader(fields))) {
        this.parser = new SampleSheetV1Parser();
        this.parser.parseLine(this.sampleSheetV1Hearder);
      }
    }

    if (this.parser != null) {

      // Parse the cache
      for (List<String> fieldsCached : this.cache) {
        this.parser.parseLine(fieldsCached);
      }

      // Clear the cache
      this.cache.clear();

      return;
    }

    this.cache.add(new ArrayList<String>(fields));
  }

  @Override
  public SampleSheet getSampleSheet() {

    if (this.parser == null) {
      throw new KenetreRuntimeException(
          "Unable to discovert the format of the samplesheet to read");
    }

    return this.parser.getSampleSheet();
  }

  //
  // Other methods
  //

  /**
   * Normalize the header of a samplesheet.
   *
   * @param list a list with the field names of a samplesheet
   * @return a list with the normalized field names
   */
  private static List<String> normalizeHeader(final List<String> list) {

    if (list == null) {
      throw new NullPointerException("list argument cannot be null");
    }

    final List<String> result = new ArrayList<String>(list);

    // Convert the bcl2fastq 1 samplesheet header to lower case
    for (int i = 0; i < result.size(); i++) {

      final String value = result.get(i);

      if (value != null) {
        result.set(i, value.trim().replaceAll("_", "").toLowerCase());
      }
    }

    return result;
  }
}
