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

package fr.ens.biologie.genomique.kenetre.bio.readfilter;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.bio.ReadSequence;

/**
 * Define a filter that check the length of the reads.
 *
 * @since 2.0
 * @author Runxin DU
 */
public class MaxLengthReadFilter extends AbstractReadFilter {
  public static final String FILTER_NAME = "maxlength";

  private int maximumLengthThreshold;

  @Override
  public boolean accept(final ReadSequence read) {

    if (read == null) {
      return false;
    }

    return read.length() < this.maximumLengthThreshold;
  }

  @Override
  public String getName() {

    return FILTER_NAME;
  }

  @Override
  public String getDescription() {

    return "Max Length ReadFilter";
  }

  @Override
  public void setParameter(final String key, final String value) throws KenetreException {

    if (key == null || value == null) {
      return;
    }

    if ("maximum.length.threshold".equals(key.trim())) {

      try {
        this.maximumLengthThreshold = Integer.parseInt(value.trim());
      } catch (NumberFormatException e) {
        return;
      }

      if (this.maximumLengthThreshold < 1) {
        throw new KenetreException("Invalid length threshold: " + this.maximumLengthThreshold);
      }
    } else {
      throw new KenetreException("Unknown parameter for " + getName() + " read filter: " + key);
    }
  }

  @Override
  public void init() {

    if (this.maximumLengthThreshold < 1) {
      throw new IllegalArgumentException(
          "Maximum length threshold is not set for " + getName() + " read filter.");
    }
  }
}
