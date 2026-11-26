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

import java.io.IOException;
import java.util.List;

public class SampleSheetReaderUtils {

  public static final void trimFields(final List<String> fields) throws IOException {

    if (fields == null) {
      throw new IOException("The fields are null");
    }

    // Trim fields
    for (int i = 0; i < fields.size(); i++) {
      final String val = fields.get(i);
      if (val == null) {
        throw new IOException("Found null field.");
      }
      fields.set(i, val.trim());
    }
  }

  public static final void checkFields(final List<String> fields) throws IOException {

    if (fields.size() == 10) {
      return;
    }

    if (fields.size() < 10) {
      throw new IOException("Invalid number of field (" + fields.size() + "), 10 excepted.");
    }

    for (int i = 10; i < fields.size(); i++) {
      if (!"".equals(fields.get(i).trim())) {
        throw new IOException("Invalid number of field (" + fields.size() + "), 10 excepted.");
      }
    }
  }

  public static final int parseLane(final String s) throws IOException {

    if (s == null) {
      return 0;
    }

    final double d;
    try {
      d = Double.parseDouble(s);

    } catch (NumberFormatException e) {
      throw new IOException("Invalid lane number: " + s);
    }

    final int result = (int) d;

    if (d - result > 0) {
      throw new IOException("Invalid lane number: " + s);
    }

    return result;
  }

  //
  // Constructor
  //

  /** Private constructor. */
  private SampleSheetReaderUtils() {}
}
