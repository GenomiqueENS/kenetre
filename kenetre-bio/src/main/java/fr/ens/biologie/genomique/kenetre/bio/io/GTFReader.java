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

package fr.ens.biologie.genomique.kenetre.bio.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * This class defines a GTF reader.
 *
 * @since 2.0
 * @author Laurent Jourdren
 */
public class GTFReader extends GFFReader {

  //
  // Constructors
  //

  /**
   * Public constructor
   *
   * @param is InputStream to use
   */
  public GTFReader(final InputStream is) {

    super(is);
    setGFF3Format(false);
  }

  /**
   * Public constructor
   *
   * @param file File to use
   * @throws FileNotFoundException if the file does not exists
   */
  public GTFReader(final File file) throws FileNotFoundException {

    super(file);
    setGFF3Format(false);
  }

  /**
   * Public constructor.
   *
   * @param filename File to use
   * @throws FileNotFoundException if the file does not exists
   */
  public GTFReader(final String filename) throws FileNotFoundException {

    super(filename);
    setGFF3Format(false);
  }
}
