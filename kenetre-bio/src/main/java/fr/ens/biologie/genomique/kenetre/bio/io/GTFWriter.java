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
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * This class define a GTF writer.
 *
 * @since 2.0
 * @author Laurent Jourdren
 */
public class GTFWriter extends GFFWriter {

  //
  // Constructors
  //

  /**
   * Public constructor.
   *
   * @param writer Writer to use
   */
  public GTFWriter(final Writer writer) {

    super(writer);
    setGFF3Format(false);
  }

  /**
   * Public constructor.
   *
   * @param os OutputStream to use
   */
  public GTFWriter(final OutputStream os) {

    super(os);
    setGFF3Format(false);
  }

  /**
   * Public constructor.
   *
   * @param outputFile file to use
   * @throws IOException if an error occurs while creating the file
   */
  public GTFWriter(final File outputFile) throws IOException {

    super(outputFile);
    setGFF3Format(false);
  }

  /**
   * Public constructor.
   *
   * @param outputFilename name of the file to use
   * @throws IOException if an error occurs while creating the file
   */
  public GTFWriter(final String outputFilename) throws IOException {

    super(outputFilename);
    setGFF3Format(false);
  }
}
