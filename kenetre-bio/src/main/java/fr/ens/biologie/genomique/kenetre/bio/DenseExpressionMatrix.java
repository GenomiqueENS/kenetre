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

package fr.ens.biologie.genomique.kenetre.bio;

/**
 * This class define a dense expression matrix.
 *
 * @author Laurent Jourdren
 * @since 2.0
 */
public class DenseExpressionMatrix extends DenseMatrix<Double> implements ExpressionMatrix {

  private static final double DEFAULT_DEFAULT_VALUE = 0.0;

  @Override
  public void setValue(final String rowName, final String columnName, double value) {

    super.setValue(rowName, columnName, (Double) value);
  }

  //
  // Constructors
  //

  /** Public constructor. */
  public DenseExpressionMatrix() {
    this(DEFAULT_DEFAULT_VALUE);
  }

  /**
   * Public constructor.
   *
   * @param defaultValue the default value of the matrix
   */
  public DenseExpressionMatrix(final double defaultValue) {
    super(defaultValue);
  }
}
