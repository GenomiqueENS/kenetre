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

import com.google.common.base.Splitter;
import fr.ens.biologie.genomique.kenetre.bio.ReadSequence;

/**
 * This class define a read filter that reverse reads marked as "polyT"
 *
 * @since 2.4
 * @author Laurent Jourdren
 */
public class ReversePolyTReadFilter extends AbstractReadFilter {

  public static final String FILTER_NAME = "reversepolyt";
  private Splitter splitter = Splitter.on(' ').omitEmptyStrings().trimResults();

  @Override
  public String getName() {

    return FILTER_NAME;
  }

  @Override
  public String getDescription() {

    return "Reverse complements polyT reads";
  }

  @Override
  public boolean accept(ReadSequence read) {

    String readName = read.getName();

    for (String s : this.splitter.splitToList(readName)) {

      s = s.toLowerCase().replace("\"", "").replace(" ", "");

      if ("tail_type=polyt".equals(s)) {
        read.reverseComplement();
      }
    }

    return true;
  }
}
