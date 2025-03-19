/*
 *                  Aozan development code
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

import static fr.ens.biologie.genomique.kenetre.illumina.interop.AbstractBinaryFileReader.uIntToLong;
import static fr.ens.biologie.genomique.kenetre.illumina.interop.AbstractBinaryFileReader.uShortToInt;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This internal class save a record from ExtractionMetricsOut.bin file, corresponding of the
 * description of the EXPECTED_VERSION. An record contains data per tile per cycle per lane. Each
 * record create an object_____________ The_file_format_for_q-metrics_is_as_follows:
 * __________________________________
 * ______________________________________________________________________________
 * Header________________________________________________________________________
 * ______________________________________________________________________________ ____byte_0:
 * _version_number____________________________________________________ ____byte_1:
 * _record_size_______________________________________________________
 * ______________________________________________________________________________
 * Extended_Header_______________________________________________________________
 * ______________________________________________________________________________
 * ____byte_2:_________________________flag_indicating_whether_is_has_bins_( bool)
 * ______________________________________________________________________________
 * If_byte_2_is_true,_then_the_following_information_is_also_in_the_header: ______
 * ______________________________________________________________________________
 * _byte_3:_________________________number_of_bins_(uint8) _______________________
 * _byte_4-binCount:________________array_of_low_ends_for_each_bin_(uint8) _______
 * _byte_4+binCount-4+binCount*2:___array_of_high_ends_for_each_bin_(uint8) ______
 * _byte_4+2*binCount-4+binCount*3:_array_of_values_for_each_bin_(uint8) _________
 * ______________________________________________________________________________ n-
 * Records_____________________________________________________________________
 * ______________________________________________________________________________
 * ______________________________________________________________________________
 * ____2_bytes:_lane_number_(uint16) _____________________________________________
 * ____2_bytes:_tile_number_(uint16) _____________________________________________
 * ____2_bytes:_cycle_number_(uint16) ____________________________________________
 *
 * @author Cyril Firmo
 * @since 2.0
 */
public class QMetric extends Metric {

  private final int laneNumber;
  private final long tileNumber;
  private final int cycleNumber;
  private final int binCount;
  private final long[] clustersScore = new long[50];
  private final int[] remappedScoreQuality;

  /**
   * Get the lane number.
   *
   * @return the lane number
   */
  public int getLaneNumber() {
    return this.laneNumber;
  }

  /**
   * Get the tile number.
   *
   * @return the tile number
   */
  public long getTileNumber() {
    return this.tileNumber;
  }

  /**
   * Get the cycle number of this record.
   *
   * @return cycle number
   */
  public int getCycleNumber() {
    return this.cycleNumber;
  }

  /**
   * Get the number of cluster having each quality score.
   *
   * @return an array of longs with number of cluster for each quality score
   */
  public long[] getClustersScore() {
    return Arrays.copyOf(this.clustersScore, this.clustersScore.length);
  }

  //
  // Metric methods
  //

  @Override
  public List<String> fieldNames() {

    List<String> result = new ArrayList<>(3 + this.binCount);

    result.add("Lane");
    result.add("Tile");
    result.add("Cycle");

    int binCount =
        this.remappedScoreQuality != null ? this.remappedScoreQuality.length : this.binCount;

    for (int i = 1; i <= binCount; i++) {
      result.add("Bin_" + i);
    }

    return result;
  }

  @Override
  public List<Number> values() {

    List<Number> result = new ArrayList<>(3 + this.binCount);

    result.add(getLaneNumber());
    result.add(getTileNumber());
    result.add(getCycleNumber());

    if (this.remappedScoreQuality != null) {
      for (int i = 0; i < this.remappedScoreQuality.length; i++) {
        result.add(this.clustersScore[remappedScoreQuality[i] - 1]);
      }
    } else {

      for (int i = 0; i < this.binCount; i++) {
        result.add(clustersScore[i]);
      }
    }

    return result;
  }

  @Override
  public List<Class<?>> fieldTypes() {
    List<Class<?>> result = new ArrayList<>(3 + this.binCount);

    int binCount =
        this.remappedScoreQuality != null ? this.remappedScoreQuality.length : this.binCount;

    for (int i = 0; i < binCount + 3; i++) {
      result.add(Integer.class);
    }

    return result;
  }

  //
  // Constructor
  //

  /**
   * Constructor. One record countReads on the ByteBuffer.
   *
   * @param bb ByteBuffer who read one record
   */
  QMetric(
      final int version,
      final ByteBuffer bb,
      final int binCount,
      final int[] remappedScoreQuality) {

    this.laneNumber = uShortToInt(bb);
    this.tileNumber = version == 7 ? uIntToLong(bb) : uShortToInt(bb);
    this.cycleNumber = uShortToInt(bb);
    this.binCount = binCount > 0 ? binCount : 50;
    this.remappedScoreQuality = remappedScoreQuality;

    if (binCount > 0) {

      // Read cluster count in each bin if version 6 but do nothing with this
      // information
      for (int i = 0; i < binCount; i++) {
        this.clustersScore[remappedScoreQuality[i] - 1] = uIntToLong(bb);
      }
    } else {

      // Read cluster count for each Phred score
      for (int i = 0; i < 50; i++) {
        this.clustersScore[i] = uIntToLong(bb);
      }
    }
  }
}
