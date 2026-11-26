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

package fr.ens.biologie.genomique.kenetre.illumina;

import static java.util.Objects.requireNonNull;

import org.apache.commons.csv.CSVRecord;

/**
 * This class define some utility methods for parsing CSV files.
 *
 * @author Laurent Jourdren
 * @since 0.3
 */
public class CSVUtils {

  /**
   * Parse int field.
   *
   * @param record record to parse
   * @param fieldName field name
   * @param defaultValue default value
   * @return parsing result
   */
  public static int parseInt(CSVRecord record, String fieldName, int defaultValue) {

    requireNonNull(record);
    requireNonNull(fieldName);

    if (!record.isSet(fieldName)) {
      return defaultValue;
    }

    String value = record.get(fieldName);

    if (value == null) {
      return defaultValue;
    }

    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Parse long field.
   *
   * @param record record to parse
   * @param fieldName field name
   * @param defaultValue default value
   * @return parsing result
   */
  public static long parseLong(CSVRecord record, String fieldName, long defaultValue) {

    requireNonNull(record);
    requireNonNull(fieldName);

    if (!record.isSet(fieldName)) {
      return defaultValue;
    }

    String value = record.get(fieldName);

    if (value == null) {
      return defaultValue;
    }

    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Parse double field.
   *
   * @param record record to parse
   * @param fieldName field name
   * @param defaultValue default value
   * @return parsing result
   */
  public static double parseDouble(CSVRecord record, String fieldName, double defaultValue) {

    requireNonNull(record);
    requireNonNull(fieldName);

    if (!record.isSet(fieldName)) {
      return defaultValue;
    }

    String value = record.get(fieldName);

    if (value == null) {
      return defaultValue;
    }

    try {
      return Double.parseDouble(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Parse String field.
   *
   * @param record record to parse
   * @param fieldName field name
   * @param defaultValue default value
   * @return parsing result
   */
  public static String parseString(CSVRecord record, String fieldName, String defaultValue) {

    requireNonNull(record);
    requireNonNull(fieldName);

    if (!record.isSet(fieldName)) {
      return defaultValue;
    }

    String value = record.get(fieldName);

    if (value == null) {
      return defaultValue;
    }

    return value;
  }
}
