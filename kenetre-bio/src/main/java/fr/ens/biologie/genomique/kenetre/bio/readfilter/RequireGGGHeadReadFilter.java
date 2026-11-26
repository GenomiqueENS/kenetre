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
 * This filter will remove all the reads without a GGG head sequence.
 *
 * @since 2.4
 * @author Laurent Jourdren
 */
public class RequireGGGHeadReadFilter extends AbstractReadFilter {

  public static final String FILTER_NAME = "requireggghead";

  private static final int ADDITIONAL_BASE_COUNT = 3;

  private boolean mismatch = true;

  @Override
  public String getName() {

    return FILTER_NAME;
  }

  @Override
  public String getDescription() {

    return "additional GGG identifier";
  }

  @Override
  public void setParameter(final String key, final String value) throws KenetreException {

    switch (key) {
      case "allow.mismatch":
        this.mismatch = Boolean.parseBoolean(value);
        break;

      default:
        throw new KenetreException("Unknown parameter for " + getName() + " read filter: " + key);
    }
  }

  @Override
  public boolean accept(ReadSequence read) {

    String sequence = read.getSequence();
    int length = read.length();

    if (length < 3) {
      return false;
    }

    String start = sequence.substring(0, Math.min(ADDITIONAL_BASE_COUNT, length));

    return count(start, 'G') >= (this.mismatch ? 2 : 3);
  }

  private static int count(String s, char c) {

    int result = 0;

    for (int i = 0; i < s.length(); i++) {

      if (s.charAt(i) == c) {
        result++;
      }
    }
    return result;
  }
}
