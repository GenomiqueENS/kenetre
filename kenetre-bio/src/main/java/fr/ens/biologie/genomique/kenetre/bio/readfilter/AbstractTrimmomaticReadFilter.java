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
import org.usadellab.trimmomatic.fastq.FastqRecord;
import org.usadellab.trimmomatic.trim.AbstractSingleRecordTrimmer;

/**
 * This classe define an abstract trimmomatic ReadFilter that allow paired-end and single-end.
 *
 * @since 1.0
 * @author du
 */
public abstract class AbstractTrimmomaticReadFilter extends AbstractReadFilter {

  private AbstractSingleRecordTrimmer trimmer;

  @Override
  public String getDescription() {
    return getName() + " Trimmomatic filter";
  }

  @Override
  public void setParameter(final String key, final String value) throws KenetreException {

    if ("arguments".equals(key)) {

      try {
        this.trimmer = createTrimmer(value == null ? "" : value);
      } catch (Exception e) {
        throw new KenetreException("Invalid parameter: " + value, e);
      }

    } else {
      throw new KenetreException("Unknown parameter for " + getName() + " read filter: " + key);
    }
  }

  @Override
  public void init() {

    if (this.trimmer == null) {
      this.trimmer = createTrimmer("");
    }
  }

  /**
   * Create a Trimmer object
   *
   * @param trimmerArgs trimmer arguments
   * @return new sequence after trimmomatic
   */
  protected abstract AbstractSingleRecordTrimmer createTrimmer(String trimmerArgs);

  @Override
  public boolean accept(final ReadSequence read) {

    // The sequence can not be empty
    if (read == null) {
      return false;
    }

    // Fastq record entry
    FastqRecord in =
        new FastqRecord(
            read.getName(),
            read.getSequence(),
            "",
            read.getQuality(),
            read.getFastqFormat().getAsciiOffset());

    // Fastq record exit
    FastqRecord out = this.trimmer.processRecord(in);

    // The fastq record exit can not be empty
    if (out == null) {
      return false;
    }

    // update sequence and their quality after trimmomatic
    read.setSequence(out.getSequence());
    read.setQuality(out.getQuality());

    return true;
  }
}
