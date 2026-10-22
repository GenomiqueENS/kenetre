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

package fr.ens.biologie.genomique.kenetre.illumina.interop;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * This class define a metric.
 *
 * @author Laurent Jourdren
 * @since 0.2
 */
public abstract class Metric {

  protected String name;
  protected int version;

  /**
   * Get the name of the metric
   *
   * @return the name of the metric
   */
  public String getName() {
    return this.name;
  }

  /**
   * Get the name of the metric
   *
   * @return the name of the metric
   */
  public int getVersion() {
    return this.version;
  }

  public abstract List<String> fieldNames();

  public abstract List<Number> values();

  public abstract List<Class<?>> fieldTypes();

  public String csvHeader() {

    StringBuilder sb = new StringBuilder();

    sb.append("# ");
    sb.append(getName());
    sb.append(',');
    sb.append(getVersion());
    sb.append("\n# ");

    for (String fieldName : fieldNames()) {
      sb.append(fieldName);
      sb.append(',');
    }

    return sb.toString();
  }

  /**
   * Get the values of the metric as a CVS line.
   *
   * @return a CSV String
   */
  public String toCSV() {
    return toCSV(values());
  }

  /**
   * Convert a list of number to a CSV String.
   *
   * @param values values to conver
   * @return a CSV String
   */
  public static String toCSV(List<Number> values) {

    Objects.requireNonNull(values);

    StringBuilder sb = new StringBuilder();

    boolean first = true;
    for (Number n : values) {

      if (first) {
        first = false;
      } else {
        sb.append(',');
      }

      if (n instanceof Float) {

        float f = n.floatValue();

        if (Float.isNaN(f)) {
          sb.append("nan");
        } else {
          sb.append(String.format(Locale.ROOT, "%.6f", f));
        }
      } else {
        sb.append(n);
      }
    }

    return sb.toString();
  }
}
