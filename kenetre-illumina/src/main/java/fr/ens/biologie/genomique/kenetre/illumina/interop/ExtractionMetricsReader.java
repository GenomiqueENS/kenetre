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

package fr.ens.biologie.genomique.kenetre.illumina.interop;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class define a specified iterator for reading the binary file version 2:
 * ExtractionMetricsOut.bin.
 *
 * @author Sandrine Perrin
 * @since 1.1
 */
public class ExtractionMetricsReader extends AbstractBinaryFileReader<ExtractionMetric> {

  public static final String NAME = "ExtractionMetricsOut";

  public static final String EXTRACTION_METRICS_FILE = "ExtractionMetricsOut.bin";

  private int channelCount = 4;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected File getMetricsFile() {
    return new File(getDirPathInterOP(), EXTRACTION_METRICS_FILE);
  }

  @Override
  protected int getExpectedRecordSize(int version) {

    switch (version) {
      case 2:
        return 38;

      case 3:
        return 8 + 6 * this.channelCount;

      default:
        throw new IllegalArgumentException();
    }
  }

  @Override
  protected Set<Integer> getExpectedVersions() {
    return new HashSet<Integer>(Arrays.asList(2, 3));
  }

  @Override
  protected void readOptionalFlag(ByteBuffer bb, int version) {

    switch (version) {
      case 2:
        return;

      case 3:
        this.channelCount = uByteToInt(bb);
        return;

      default:
        throw new IllegalArgumentException();
    }
  }

  @Override
  protected void readMetricRecord(
      final List<ExtractionMetric> collection, final ByteBuffer bb, final int version) {

    collection.add(new ExtractionMetric(version, this.channelCount, bb));
  }

  //
  // Constructor
  //

  /**
   * Constructor.
   *
   * @param dirPath the directory containing the interop files
   * @throws KenetreException if the directory is invalid
   */
  public ExtractionMetricsReader(final File dirPath) throws KenetreException {
    super(dirPath);
  }
}
