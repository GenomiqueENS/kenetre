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

package fr.ens.biologie.genomique.kenetre.storage;

import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.log.GenericLogger;
import java.io.IOException;

/**
 * This class define a basic GenomeIndexStorage based on an index file.
 *
 * @since 1.1
 * @author Laurent Jourdren
 */
public class FileGenomeIndexStorage extends AbstractFileGenomeIndexStorage {

  @Override
  protected DataPath newDataPath(String source) {

    return new FileDataPath(source);
  }

  @Override
  protected DataPath newDataPath(DataPath parent, String filename) {

    return new FileDataPath(parent, filename);
  }

  //
  // Static methods
  //

  /**
   * Create a GenomeIndexStorage
   *
   * @param dir the path of the genome descriptions storage
   * @param logger the logger
   * @return a GenomeIndexStorage object if the path contains an index storage or null if no index
   *     storage is found
   */
  public static GenomeIndexStorage getInstance(final String dir, GenericLogger logger) {

    requireNonNull(dir);

    try {
      return new FileGenomeIndexStorage(new FileDataPath(dir), logger);
    } catch (IOException | NullPointerException e) {
      return null;
    }
  }

  //
  // Constructor
  //

  /**
   * Private constructor.
   *
   * @param dir the path of the genome descriptions storage
   * @param logger logger to use
   * @throws IOException
   */
  private FileGenomeIndexStorage(DataPath dir, GenericLogger logger) throws IOException {
    super(dir, logger);
    // TODO Auto-generated constructor stub
  }
}
