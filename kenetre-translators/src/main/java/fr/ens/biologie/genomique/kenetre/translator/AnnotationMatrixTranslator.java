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

package fr.ens.biologie.genomique.kenetre.translator;

import fr.ens.biologie.genomique.kenetre.bio.AnnotationMatrix;
import java.util.List;
import java.util.Objects;

/**
 * This class implements a translator using an AnnotationMatrix.
 *
 * @since 2.4
 * @author Laurent Jourdren
 */
public class AnnotationMatrixTranslator extends AbstractTranslator {

  private final AnnotationMatrix matrix;

  @Override
  public List<String> getFields() {

    return this.matrix.getColumnNames();
  }

  @Override
  public String translateField(final String id, final String field) {

    if (id == null || !this.matrix.containsRow(id)) {
      return null;
    }

    return this.matrix.getValue(id, field == null ? getDefaultField() : field);
  }

  @Override
  public List<String> getIds() {

    return this.matrix.getRowNames();
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   *
   * @param matrix matrix to use
   */
  public AnnotationMatrixTranslator(final AnnotationMatrix matrix) {

    Objects.requireNonNull(matrix, "matrix argument cannot be null");

    this.matrix = matrix;
  }
}
