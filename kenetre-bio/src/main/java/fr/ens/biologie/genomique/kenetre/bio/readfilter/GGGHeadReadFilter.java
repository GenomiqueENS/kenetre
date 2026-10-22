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

import fr.ens.biologie.genomique.kenetre.bio.ReadSequence;

/**
 * This filter will search for GGG head and CCC tail and add additional fields in read header
 * fields.
 *
 * @since 2.4
 * @author Laurent Jourdren
 */
public class GGGHeadReadFilter extends AbstractReadFilter {

  public static final String FILTER_NAME = "ggghead";

  private static final int ADDITIONAL_BASE_COUNT = 3;

  @Override
  public String getName() {

    return FILTER_NAME;
  }

  @Override
  public String getDescription() {

    return "GGG head identifier";
  }

  @Override
  public boolean accept(ReadSequence read) {

    String sequence = read.getSequence();
    int length = read.length();

    String start = sequence.substring(0, Math.min(ADDITIONAL_BASE_COUNT, length));
    String end = sequence.substring(Math.max(length - ADDITIONAL_BASE_COUNT, 0));

    read.setName(
        read.getName()
            + " start_sequence="
            + start
            + " start_G_count="
            + count(start, 'G')
            + " end_sequence="
            + end
            + " end_C_count="
            + count(end, 'C'));

    return true;
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
