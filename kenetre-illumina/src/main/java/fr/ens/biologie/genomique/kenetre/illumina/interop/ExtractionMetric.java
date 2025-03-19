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
 * record create an object_____________ byte 0: file version number
 * (2)____________________________________________ byte 1: length of each
 * record______________________________________________ bytes (N * 38 + 2) - (N *38 + 39):
 * record:_________________________________ __2 bytes: lane number
 * (uint16)____________________________________________ __2 bytes: tile number
 * (uint16)____________________________________________ __2 bytes: cycle number
 * (uint16)___________________________________________ __4 x 4 bytes: fwhm scores (float) for
 * channel [A, C, G, T] respectively___ __2 x 4 bytes: intensities (uint16) for channel [A, C, G, T]
 * respectively__ __8 bytes: date/time of CIF creation_______________________________________ Where
 * N is the record index________________________________________________
 *
 * @author Sandrine Perrin
 * @since Aozan 1.1
 */
public class ExtractionMetric extends Metric {

  private final float[] fwhm = new float[4]; // A C G T
  private final int[] intensities = new int[4]; // A C G T

  private final int laneNumber;
  private final long tileNumber;
  private final int cycleNumber;
  private final int channelCount;
  private final long timestamp;

  /**
   * Get the number lane.
   *
   * @return the lane number
   */
  public int getLaneNumber() {
    return this.laneNumber;
  }

  /**
   * Get the number tile.
   *
   * @return the tile number
   */
  public long getTileNumber() {
    return this.tileNumber;
  }

  /**
   * Get the time stamp.
   *
   * @return the time stamp
   */
  public long getTimestamp() {
    return this.timestamp;
  }

  /**
   * Get the number cycle of this record.
   *
   * @return number cycle
   */
  public int getCycleNumber() {
    return this.cycleNumber;
  }

  /**
   * Get a float array with the fwhm (full width at half maximum) scores of each base (A, C, G, T).
   *
   * @return float array with the fwhm scores of each channel
   */
  public float[] getFwhm() {
    return Arrays.copyOf(this.fwhm, this.fwhm.length);
  }

  /**
   * Get a integer array with the raw intensities of each base (A, C, G, T).
   *
   * @return float array with the raw intensities of each channel
   */
  public int[] getIntensities() {
    return Arrays.copyOf(this.intensities, this.intensities.length);
  }

  /**
   * Get the average of the four intensities (one per channel) for this record.
   *
   * @return average of the four intensities (one per channel)
   */
  public int getAverageIntensities() {
    int sum = 0;
    for (final int intensity : this.intensities) {
      sum += intensity;
    }
    return sum / this.intensities.length;
  }

  //
  // Metric methods
  //

  @Override
  public List<String> fieldNames() {

    ArrayList<String> result = new ArrayList<>();
    result.addAll(Arrays.asList("Lane", "Tile", "Cycle", "TimeStamp"));

    if (this.channelCount == 4) {
      result.addAll(
          Arrays.asList(
              "MaxIntensity_A",
              "MaxIntensity_C",
              "MaxIntensity_G",
              "MaxIntensity_T",
              "Focus_A",
              "Focus_C",
              "Focus_G",
              "Focus_T"));
      return result;
    }

    result.addAll(
        Arrays.asList("MaxIntensity_Red", "MaxIntensity_Green", "Focus_Red", "Focus_Green"));

    return result;
  }

  @Override
  public List<Number> values() {

    ArrayList<Number> result = new ArrayList<>();
    result.addAll(
        Arrays.asList(getLaneNumber(), getTileNumber(), getCycleNumber(), getTimestamp()));

    int channelCount = this.channelCount;

    if (channelCount == 4) {
      channelCount =
          (this.intensities[2] == 0
                  && this.intensities[3] == 0
                  && this.fwhm[2] == 0
                  && this.fwhm[3] == 0)
              ? 2
              : 4;
    }

    for (int i = 0; i < channelCount; i++) {
      result.add(this.intensities[i]);
    }

    for (int i = 0; i < channelCount; i++) {
      result.add(this.fwhm[i]);
    }

    return result;
  }

  @Override
  public List<Class<?>> fieldTypes() {

    ArrayList<Class<?>> result = new ArrayList<>();
    result.addAll(Arrays.asList(Integer.class, Integer.class, Integer.class, long.class));

    int channelCount = this.channelCount;

    if (channelCount == 4) {
      channelCount =
          (this.intensities[2] == 0
                  && this.intensities[3] == 0
                  && this.fwhm[2] == 0
                  && this.fwhm[3] == 0)
              ? 2
              : 4;
    }

    for (int i = 0; i < channelCount; i++) {
      result.add(Integer.class);
    }

    for (int i = 0; i < channelCount; i++) {
      result.add(Float.class);
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
  ExtractionMetric(final int version, final int channelCount, final ByteBuffer bb) {

    this.laneNumber = uShortToInt(bb);
    this.tileNumber = version == 3 ? uIntToLong(bb) : uShortToInt(bb);
    this.cycleNumber = uShortToInt(bb);
    this.channelCount = channelCount;

    if (version == 2) {

      for (int i = 0; i < 4; i++) {
        this.fwhm[i] = bb.getFloat();
      }

      for (int i = 0; i < 4; i++) {
        this.intensities[i] = uShortToInt(bb);
      }

      // Do not handle timestamp
      bb.getLong();
      this.timestamp = -1;

    } else if (version == 3) {

      for (int i = 0; i < channelCount; i++) {
        this.fwhm[i] = bb.getFloat();
      }

      for (int i = 0; i < channelCount; i++) {
        this.intensities[i] = uShortToInt(bb);
      }

      this.timestamp = -1;
    } else {
      throw new IllegalStateException("Unknow version for  Extraction metric:" + version);
    }
  }

  public static String byteArrayToHex(byte[] a) {
    StringBuilder sb = new StringBuilder(a.length * 2);
    for (byte b : a) sb.append(String.format("%02x", b));
    return sb.toString();
  }

  public static String byteArrayToBin(byte[] a) {
    StringBuilder sb = new StringBuilder(a.length * 8);
    for (byte b : a) {
      sb.append(Integer.toBinaryString(b));
      sb.append("|");
    }

    return sb.toString();
  }
}
