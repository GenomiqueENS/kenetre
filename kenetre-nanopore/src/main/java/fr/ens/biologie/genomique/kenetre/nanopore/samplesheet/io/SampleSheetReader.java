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

import fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet;
import java.io.IOException;
import java.util.Collection;

/**
 * This interface define a reader for Nanopore software sample sheets.
 *
 * @since 0.20
 * @author Laurent Jourdren
 */
public interface SampleSheetReader extends AutoCloseable {

  /**
   * Allow to accept any field.
   *
   * @param allow the value to set
   */
  void allowAnyField(boolean allow);

  /**
   * Allow additional field name.
   *
   * @param fieldName field name to allow
   */
  void addAllowedField(String fieldName);

  /**
   * Allow additional field names.
   *
   * @param fieldNames field names to allow
   */
  void addAllowedFields(Collection<String> fieldNames);

  /**
   * Allow additional field names.
   *
   * @param fieldNames field names to allow
   */
  void addAllowedFields(String... fieldNames);

  /**
   * Read a sample sheet.
   *
   * @return a SampleSheet object
   * @throws IOException if an error occurs while reading the sample sheet
   */
  SampleSheet read() throws IOException;

  @Override
  void close() throws IOException;
}
