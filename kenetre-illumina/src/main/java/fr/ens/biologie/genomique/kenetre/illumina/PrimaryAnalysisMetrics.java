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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 * This class allow to parse the PrimaryAnalysisMetrics csv file.
 *
 * @author Laurent Jourdren
 * @since 0.3
 */
public class PrimaryAnalysisMetrics {

  private enum Headers {
    Metric,
    Unit,
    Value;
  }

  private float averageQ30 = Float.NaN;
  private float totalYield = Float.NaN;
  private float totalReadsPF = Float.NaN;
  private float loadingConcentrationPercent = Float.NaN;

  //
  // Getters
  //

  /**
   * Get the average Q30 of the run.
   *
   * @return the averageQ30 the average Q30 of the run
   */
  public float getAverageQ30() {
    return averageQ30;
  }

  /**
   * Get the total yield of the run.
   *
   * @return the total yield of the run
   */
  public float getTotalYield() {
    return totalYield;
  }

  /**
   * Get the total passing filter read number of the run.
   *
   * @return the total passing filter read number of the run
   */
  public float getTotalReadsPF() {
    return totalReadsPF;
  }

  /**
   * Get the percent of loading concentration.
   *
   * @return the percent of loading concentration
   */
  public float getLoadingConcentrationPercent() {
    return loadingConcentrationPercent;
  }

  //
  // Parsing utility method
  //

  private static float multiplyFactor(String s) {

    if (s == null || s.trim().isEmpty()) {
      return 1.0f;
    }

    switch (s.trim().charAt(0)) {
      case '%':
        return 0.01f;

      case 'M':
        return 1_000_000f;

      case 'G':
        return 1_000_000_000f;

      default:
        return 1f;
    }
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   *
   * @param file input file
   * @throws IOException if an error occurs while reading the input file
   */
  public PrimaryAnalysisMetrics(File file) throws IOException {

    this(new FileInputStream(file));
  }

  /**
   * Public constructor.
   *
   * @param in input stream
   * @throws IOException if an error occurs while reading the input file
   */
  public PrimaryAnalysisMetrics(InputStream in) throws IOException {

    Objects.requireNonNull(in);

    try (Reader reader = new InputStreamReader(in)) {

      for (CSVRecord record :
          CSVFormat.RFC4180
              .builder()
              .setIgnoreEmptyLines(true)
              .setHeader(Headers.class)
              .setSkipHeaderRecord(true)
              .build()
              .parse(reader)) {

        String metric = record.get(Headers.Metric).toLowerCase().trim();
        String value = record.get(Headers.Value).toLowerCase().trim();
        String unit = record.get(Headers.Unit);

        switch (metric) {
          case "average %q30":
          case "≥ q30":
            this.averageQ30 = Float.parseFloat(value) * multiplyFactor(unit);
            break;

          case "total yield":
            this.totalYield = Float.parseFloat(value) * multiplyFactor(unit);
            break;

          case "total reads pf":
            this.totalReadsPF = Float.parseFloat(value) * multiplyFactor(unit);
            break;

          case "% loading concentration":
            this.loadingConcentrationPercent = Float.parseFloat(value) * multiplyFactor(unit);
            break;

          default:
            throw new IllegalStateException("Unknown metric: " + record.get(Headers.Metric));
        }
      }
    }
  }
}
