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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This class define a read filter that filter the special motif
 *
 * @since 2.0
 * @author Runxin DU
 */
public class PatternReadFilter extends AbstractReadFilter {

  private Pattern allowedPattern;
  private Pattern forbiddenPattern;

  @Override
  public String getName() {
    return "readsequenceregex";
  }

  @Override
  public String getDescription() {
    return "Pattern Read Filter";
  }

  @Override
  public boolean accept(ReadSequence read) {
    if (read == null) {
      return false;
    }

    final String seq = read.getSequence();
    if (seq == null) {
      return false;
    }

    if (this.forbiddenPattern != null && this.forbiddenPattern.matcher(seq).find()) {
      return false;
    }

    if (this.allowedPattern != null && !this.allowedPattern.matcher(seq).find()) {
      return false;
    }
    return true;
  }

  @Override
  public void setParameter(final String key, final String value) throws KenetreException {
    if (key == null || value == null) {
      return;
    }

    if ("forbidden.regex".equals(key.trim())) {
      try {
        this.forbiddenPattern = Pattern.compile(value);
      } catch (PatternSyntaxException e) {
        throw new KenetreException("Invalid motif: " + getName() + " read filter: " + value);
      }

    } else if ("allowed.regex".equals(key.trim())) {
      try {
        this.allowedPattern = Pattern.compile(value);
      } catch (PatternSyntaxException e) {
        throw new KenetreException("valid motif: " + getName() + " read filter: " + value);
      }
    } else {
      throw new KenetreException("Unknown parameter for " + getName() + " read filter: " + key);
    }
  }
}
