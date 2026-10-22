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

package fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.io;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This abstract class contains common methods for SampleSheetReader classes.
 *
 * @since 0.32
 * @author Laurent Jourdren
 */
public abstract class AbstractSampleSheetReader implements SampleSheetReader {

  private boolean allowAnyfields = false;
  private Set<String> allowedFields = new HashSet<>();

  @Override
  public void allowAnyField(boolean allow) {
    this.allowAnyfields = allow;
  }

  @Override
  public void addAllowedField(String fieldName) {

    requireNonNull(fieldName);
    this.allowedFields.add(fieldName.trim().toLowerCase());
  }

  @Override
  public void addAllowedFields(Collection<String> fieldNames) {

    requireNonNull(fieldNames);
    for (String fieldName : fieldNames) {
      addAllowedField(fieldName);
    }
  }

  @Override
  public void addAllowedFields(String... fieldNames) {

    addAllowedFields(Arrays.asList(fieldNames));
  }

  /**
   * Create a new parser.
   *
   * @return a configurated SampleSheetParser
   */
  protected SampleSheetParser newSampleSheetParser() {

    SampleSheetParser parser = new SampleSheetParser();
    parser.allowAnyField(this.allowAnyfields);
    parser.addAllowedFields(this.allowedFields);

    return parser;
  }
}
