package fr.ens.biologie.genomique.kenetre.illumina.interop;

import static fr.ens.biologie.genomique.kenetre.illumina.interop.AbstractBinaryFileReader.uByteToInt;
import static fr.ens.biologie.genomique.kenetre.illumina.interop.AbstractBinaryFileReader.uIntToLong;
import static fr.ens.biologie.genomique.kenetre.illumina.interop.AbstractBinaryFileReader.uShortToInt;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This internal class save a record from TileMetricsOut.bin file. The class logic comes from the
 * Illumina Interop library.
 *
 * @author Laurent Jourdren
 * @since 0.2
 */
@SuppressWarnings("unused")
public class TileMetric extends Metric {

  private static final int CLUSTER_DENSITY = 100;
  private static final int CLUSTER_DENSITY_PF = 101;
  private static final int CLUSTER_COUNT = 102;
  private static final int CLUSTER_COUNT_PF = 103;
  private static final int PHASING = 200;

  private static final int PREFPHASING = 201;
  private static final int PERCENT_ALIGNED = 300;
  private static final int CONTROL_LANE = 400;

  private static final int LANE_BIT_COUNT = 6;
  private static final int TILE_BIT_COUNT = 26;

  private static final int CYCLE_BIT_COUNT = 16;
  private static final int READ_BIT_COUNT = 16;
  private static final int RESERVED_BIT_COUNT = 16;

  private static final int READ_BIT_SHIFT = RESERVED_BIT_COUNT;
  private static final int CYCLE_BIT_SHIFT = RESERVED_BIT_COUNT;
  private static final int TILE_BIT_SHIFT = CYCLE_BIT_SHIFT + CYCLE_BIT_COUNT;
  private static final int LANE_BIT_SHIFT = TILE_BIT_SHIFT + TILE_BIT_COUNT;

  private int laneNumber;
  private long tileNumber;

  private float clusterDensity;
  private float clusterDensityPF;
  private float clusterCount;
  private float clusterCountPF;
  private final List<ReadMetric> readMetrics = new ArrayList<>();

  class ReadMetric {

    private long read;
    private float percentAligned;
    private float percentPhasing;
    private float percentPrephasing;

    //
    // Getters
    //

    /**
     * Get the read number.
     *
     * @return the read number
     */
    long read() {
      return this.read;
    }

    /**
     * Get the percent aligned for read. If percent aligned was never estimated, then it will be NaN
     *
     * @return the percent aligned
     */
    float percentAligned() {
      return this.percentAligned;
    }

    /**
     * Get the percent phasing for read. Supported only in version 2
     *
     * @return percent phasing
     */
    float percentPhasing() {
      // Consistent with legacy
      return (this.percentPhasing < 0) ? 0 : this.percentPhasing;
    }

    /**
     * Get the percent prephasing for read. Supported only in version 2
     *
     * @return the percent prephasing
     */
    float percentPrephasing() {
      // Consistent with legacy
      return (this.percentPrephasing < 0) ? 0 : this.percentPrephasing;
    }

    //
    // Setters
    //

    /**
     * Set percent aligned for read
     *
     * @param val percent aligned
     */
    void percentAligned(float val) {
      this.percentAligned = val;
    }

    /**
     * Set percent phasing for read
     *
     * @note Supported only in version 2
     * @param val percent phasing
     */
    void percentPhasing(float val) {
      this.percentPhasing = val;
    }

    /**
     * Set percent prephasing for read
     *
     * @note Supported only in version 2
     * @param val percent prephasing
     */
    void percentPrephasing(float val) {
      percentPrephasing = val;
    }
  }

  static int id(int lane, int tile) {
    return (lane << LANE_BIT_SHIFT) | (tile << TILE_BIT_SHIFT);
  }

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
   * Density of clusters for each tile (in clusters per mm2)
   *
   * @return cluster density
   */
  public float getClusterDensity() {
    return this.clusterDensity;
  }

  /**
   * Density of clusters passing filter for each tile (in clusters per mm2)
   *
   * @return cluster density passing filter
   */
  public float getClusterDensityPF() {
    return this.clusterDensityPF;
  }

  /**
   * Number of clusters for each tile
   *
   * @return number of clusters
   */
  public float getClusterCount() {
    return this.clusterCount;
  }

  /**
   * Number of clusters passing filter for each tile
   *
   * @return number of clusters passing filter
   */
  public float getClusterCountPF() {
    return this.clusterCountPF;
  }

  /**
   * Percent of clusters passing filter
   *
   * @return percent of clusters passing filter
   */
  public float getPercentPF() {
    return 100 * this.clusterCountPF / this.clusterCount;
  }

  /**
   * Metrics for each read on the tile
   *
   * @return vector of metrics for each read
   */
  public List<ReadMetric> readMetrics() {
    return this.readMetrics;
  }

  /**
   * Percent aligned for read at specified index. Note: if percent aligned was never estimated, then
   * it will be NaN.
   *
   * @param readIndex index of read
   * @return percent aligned (or NaN is out of bounds)
   */
  public float getPercentAligned(int readIndex) {

    if (readIndex >= this.readMetrics.size()) {
      return Float.NaN;
    }

    return this.readMetrics.get(readIndex).percentAligned();
  }

  /**
   * Percent phasing for read at specified index. Note: supported only in version 2.
   *
   * @param readIndex index of read
   * @return percent phasing (or NaN is out of bounds)
   */
  public float getPercentPhasing(int readIndex) {

    if (readIndex >= this.readMetrics.size()) {
      return Float.NaN;
    }

    return this.readMetrics.get(readIndex).percentPhasing();
  }

  /**
   * Percent prephasing for read at specified index. Note: Supported only in version 2.
   *
   * @param readIndex index of read
   * @return percent prephasing (or NaN is out of bounds)
   */
  public float getPercentPrephasing(int readIndex) {

    if (readIndex >= this.readMetrics.size()) {
      return Float.NaN;
    }

    return this.readMetrics.get(readIndex).percentPrephasing();
  }

  /**
   * Percent prephasing for read number. Note: Supported only in version 2.
   *
   * @param number number of read
   * @return percent prephasing (or NaN is out of bounds)
   */
  public float getPercentPrephasingAt(int number) {

    for (ReadMetric rm : readMetrics) {
      if (rm.read() == number) {
        return rm.percentPrephasing();
      }
    }

    return Float.NaN;
  }

  /**
   * Number of reads.
   *
   * @return number of reads
   */
  public int getReadCount() {

    return this.readMetrics.size();
  }

  void parseV2(ByteBuffer bb) {

    int metricCode = uShortToInt(bb);
    float metricValue = bb.getFloat();

    switch (metricCode) {
      case CONTROL_LANE:
        break;

      case CLUSTER_DENSITY:
        this.clusterDensity = metricValue;
        break;
      case CLUSTER_DENSITY_PF:
        this.clusterDensityPF = metricValue;
        break;
      case CLUSTER_COUNT:
        this.clusterCount = metricValue;
        break;
      case CLUSTER_COUNT_PF:
        this.clusterCountPF = metricValue;
        break;

      default:
        if (metricCode % PHASING < 100) {
          // code = Prephasing+read*2;
          int code_offset = metricCode % PHASING;
          if (code_offset % 2 == 0) {
            getRead(this, (code_offset / 2) + 1).percentPhasing(metricValue * 100);
          } else {
            getRead(this, (code_offset + 1) / 2).percentPrephasing(metricValue * 100);
          }
        } else if (metricCode % PERCENT_ALIGNED < 100) {
          int code_offset = metricCode % PERCENT_ALIGNED;
          getRead(this, code_offset + 1).percentAligned(metricValue);
        } else throw new IllegalStateException();
    }
  }

  void parseV3(ByteBuffer bb, float density) {

    int metricCode = uByteToInt(bb);

    switch (metricCode) {
      case 't':
        this.clusterCount = bb.getFloat();
        this.clusterCountPF = bb.getFloat();

        if (density == 0 || Float.isNaN(density)) {
          this.clusterDensity = Float.NaN;
          this.clusterDensityPF = Float.NaN;
        } else {
          this.clusterDensity = this.clusterCount / density;
          this.clusterDensityPF = this.clusterCountPF / density;
        }

        break;

      case 'r':
        long read = uIntToLong(bb);
        float percentAligned = bb.getFloat();

        for (int i = this.readMetrics.size(); i < read; i++) {
          this.readMetrics.add(new ReadMetric());
        }

        ReadMetric rm = this.readMetrics.get((int) read - 1);
        rm.percentAligned = percentAligned;
        rm.percentPhasing = Float.NaN; // do not compute phasing
        rm.percentPrephasing = Float.NaN; // do not compute phasing

        break;

      case 0:
        break;

      default:
        throw new IllegalStateException();
    }
  }

  //
  // Utility methods
  //

  private ReadMetric getRead(TileMetric metric, long read) {

    for (ReadMetric m : metric.readMetrics) {
      if (m.read() == read) {
        return m;
      }
    }

    ReadMetric result = new ReadMetric();
    result.read = read;
    metric.readMetrics.add(result);

    return result;
  }

  //
  // Metric methods
  //

  @Override
  public List<String> fieldNames() {

    return Arrays.asList(
        "Lane",
        "Tile",
        "Read",
        "ClusterCount",
        "ClusterCountPF",
        "Density",
        "DensityPF",
        "Aligned",
        "Prephasing",
        "Phasing");
  }

  @Override
  public List<Number> values() {
    return values(0);
  }

  public List<Number> values(int read) {

    return Arrays.asList(
        getLaneNumber(),
        getTileNumber(),
        read + 1,
        getClusterCount(),
        getClusterCountPF(),
        getClusterDensity(),
        getClusterDensityPF(),
        getPercentAligned(read),
        getPercentPrephasing(read),
        getPercentPhasing(read));
  }

  @Override
  public List<Class<?>> fieldTypes() {

    return Arrays.asList(
        Integer.class,
        Integer.class,
        Integer.class,
        Float.class,
        Float.class,
        Float.class,
        Float.class,
        Float.class,
        Float.class,
        Float.class);
  }

  //
  // Object methods
  //

  @Override
  public String toString() {
    return "NewTileMetric [m_lane="
        + laneNumber
        + ", m_tile="
        + tileNumber
        + ", m_cluster_density="
        + clusterDensity
        + ", m_cluster_density_pf="
        + clusterDensityPF
        + ", m_cluster_count="
        + clusterCount
        + ", m_cluster_count_pf="
        + clusterCountPF
        + "]";
  }

  //
  // Constructor
  //

  TileMetric(int lane, long tile) {

    this.laneNumber = lane;
    this.tileNumber = tile;
  }
}
