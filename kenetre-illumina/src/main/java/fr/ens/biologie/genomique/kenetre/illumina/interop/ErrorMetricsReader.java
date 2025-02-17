/*
 *                 Aozan development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU General Public License version 3 or later 
 * and CeCILL. This should be distributed with the code. If you 
 * do not have a copy, see:
 *
 *      http://www.gnu.org/licenses/gpl-3.0-standalone.html
 *      http://www.cecill.info/licences/Licence_CeCILL_V2-en.html
 *
 * Copyright for this code is held jointly by the Genomic platform
 * of the Institut de Biologie de l'École Normale Supérieure and
 * the individual authors. These should be listed in @author doc
 * comments.
 *
 * For more information on the Aozan project and its aims,
 * or to join the Aozan Google group, visit the home page at:
 *
 *      http://outils.genomique.biologie.ens.fr/aozan
 *
 */

package fr.ens.biologie.genomique.kenetre.illumina.interop;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.ens.biologie.genomique.kenetre.KenetreException;

/**
 * This class define a specified iterator for reading the binary file:
 * ErrorMetricsOut.bin.
 * @author Sandrine Perrin
 * @since 1.1
 */
public class ErrorMetricsReader extends AbstractBinaryFileReader<ErrorMetric> {

  public static final String NAME = "ErrorMetricsOut";

  public static final String ERROR_METRICS_FILE = "ErrorMetricsOut.bin";

  public final List<String> adapterSequences = new ArrayList<>();

  /**
   * Get the file name treated.
   * @return file name
   */
  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected File getMetricsFile() {
    return new File(getDirPathInterOP(), ERROR_METRICS_FILE);
  }

  @Override
  protected int getExpectedRecordSize(int version) {

    switch (version) {
    case 3:
      return 30;

    case 4:
      return 12;

    case 5:
      return 16;

    case 6:
      return 12 + 4 * this.adapterSequences.size();

    default:
      throw new IllegalArgumentException();
    }

  }

  @Override
  protected Set<Integer> getExpectedVersions() {
    return new HashSet<Integer>(Arrays.asList(3, 4, 5, 6));
  }

  @Override
  protected void readOptionalFlag(ByteBuffer bb, int version) {

    if (version == 6) {

      int numAdapter = uShortToInt(bb);
      int adapterBaseCount = uShortToInt(bb);

      for (int i = 0; i < numAdapter; i++) {

        StringBuilder sb = new StringBuilder();

        for (int j = 0; j < adapterBaseCount; j++) {
          sb.append((char) uByteToInt(bb));
        }

        this.adapterSequences.add(sb.toString());
      }
    }
  }

  @Override
  protected void readMetricRecord(final List<ErrorMetric> collection,
      final ByteBuffer bb, final int version) {

    collection.add(new ErrorMetric(version, this.adapterSequences, bb));
  }

  //
  // Constructor
  //

  /**
   * Constructor.
   * @param dirPath path of the directory while the binary is located
   * @throws KenetreException it occurs if size record or version aren't the
   *           same that expected
   * @throws FileNotFoundException if the binary cannot be found
   */

  public ErrorMetricsReader(final File dirPath)
      throws FileNotFoundException, KenetreException {

    super(dirPath);

    if (!new File(getDirPathInterOP(), ERROR_METRICS_FILE).exists()) {
      throw new FileNotFoundException();
    }
  }

}
