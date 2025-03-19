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
