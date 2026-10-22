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
import fr.ens.biologie.genomique.kenetre.bio.NanoporeReadId;
import fr.ens.biologie.genomique.kenetre.bio.NanoporeReadId.SequenceType;
import fr.ens.biologie.genomique.kenetre.bio.ReadSequence;
import java.util.Iterator;

/**
 * This class define a filter based on the Nanopore sequence type.
 *
 * @since 2.0
 * @author Laurent Jourdren
 */
public class NanoporeSequenceTypeFilter extends AbstractReadFilter {

  public static final String FILTER_NAME = "nanoporesequencetype";

  private final Splitter spliter = Splitter.on(' ').omitEmptyStrings();
  private NanoporeReadId.SequenceType sequenceType = SequenceType.CONSENSUS;

  @Override
  public String getName() {
    return FILTER_NAME;
  }

  @Override
  public String getDescription() {
    return "Filter nanopore reads against its type";
  }

  @Override
  public boolean accept(final ReadSequence read) {

    if (read == null) {
      return false;
    }

    Iterator<String> it = this.spliter.split(read.getName()).iterator();

    if (!it.hasNext()) {
      return false;
    }

    String sequenceName = it.next();

    switch (this.sequenceType) {
      case CONSENSUS:
        return sequenceName.indexOf('_') == -1;

      case TEMPLATE:
        return sequenceName.endsWith("_t");

      case COMPLEMENT:
        return sequenceName.endsWith("_c");

      default:
        return false;
    }
  }

  @Override
  public void setParameter(final String key, final String value) throws KenetreException {

    if (key == null || value == null) {
      return;
    }

    if ("keep".equals(key.trim())) {

      SequenceType type;

      try {
        type = SequenceType.valueOf(value.toUpperCase().trim());
      } catch (IllegalArgumentException e) {
        throw new KenetreException("Invalid sequence type: " + value);
      }

      this.sequenceType = type;

    } else {
      throw new KenetreException("Unknown parameter for " + getName() + " read filter: " + key);
    }
  }
}
