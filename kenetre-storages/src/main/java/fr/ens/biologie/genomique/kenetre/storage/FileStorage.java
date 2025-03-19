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

import java.util.List;

/**
 * This class define a storage using File API.
 *
 * @since 2.6
 * @author Laurent Jourdren
 */
public class FileStorage extends AbstractFileStorage {

  @Override
  protected DataPath newDataPath(String source) {

    return new FileDataPath(source);
  }

  @Override
  protected DataPath newDataPath(DataPath parent, String filename) {

    return new FileDataPath(parent, filename);
  }

  //
  // Constructor
  //

  /**
   * Constructor.
   *
   * @param rootPath root of the storage
   * @param extensions extension of the files
   */
  public FileStorage(String rootPath, List<String> extensions) {
    super(rootPath, extensions);
  }
}
