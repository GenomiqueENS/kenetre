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
import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.bio.ReadSequence;
import fr.ens.biologie.genomique.kenetre.bio.readfilter.PolyATailReadFilter.TailType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class define a read filter that remove reads without a valid polyA tail.
 *
 * @since 2.4
 * @author Laurent Jourdren
 */
public class RemoveInvalidPolyAReadFilter extends AbstractReadFilter {

  public static final String FILTER_NAME = "removeinvalidpolya";
  private Splitter splitter = Splitter.on(' ').omitEmptyStrings().trimResults();
  private Set<String> allowed = new HashSet<>(Arrays.asList("polya", "polyt"));

  @Override
  public String getName() {

    return FILTER_NAME;
  }

  @Override
  public String getDescription() {

    return "PolyA invalid type filter";
  }

  @Override
  public void setParameter(final String key, final String value) throws KenetreException {

    switch (key) {
      case "allowed.tail.type":
        this.allowed.clear();

        for (String type :
            Splitter.on(',').omitEmptyStrings().trimResults().split(value.toLowerCase())) {

          TailType t = TailType.parse(type);

          if (t == null) {
            throw new KenetreException(
                "Unknown value for " + getName() + "." + key + " parameter: " + type);
          }

          this.allowed.add(t.toString().toLowerCase());
        }

        break;

      default:
        throw new KenetreException("Unknown parameter for " + getName() + " read filter: " + key);
    }
  }

  @Override
  public boolean accept(ReadSequence read) {

    String readName = read.getName();

    for (String s : this.splitter.splitToList(readName)) {

      s = s.toLowerCase().replace("\"", "").replace(" ", "");

      if (!s.startsWith("tail_type=")) {
        continue;
      }

      s = s.substring(s.indexOf('=') + 1);

      if (allowed.contains(s)) {
        return true;
      }
    }

    return false;
  }
}
