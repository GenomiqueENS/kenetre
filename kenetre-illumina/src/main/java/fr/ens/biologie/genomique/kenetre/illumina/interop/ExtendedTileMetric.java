package fr.ens.biologie.genomique.kenetre.illumina.interop;

import static fr.ens.biologie.genomique.kenetre.illumina.interop.AbstractBinaryFileReader.uIntToLong;
import static fr.ens.biologie.genomique.kenetre.illumina.interop.AbstractBinaryFileReader.uShortToInt;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * This internal class save a record from ExtendedTileMetricsOut.bin file.
 *
 * @author Laurent Jourdren
 * @since 0.3
 */
public class ExtendedTileMetric extends Metric {

  private int laneNumber;
  private long tileNumber;
  private float clusterCountcOccupied;
  private float upperLeftX;
  private float upperLeftY;

  //
  // Getters
  //

  /**
   * @return the laneNumber
   */
  public int getLaneNumber() {
    return laneNumber;
  }

  /**
   * @return the tileNumber
   */
  public long getTileNumber() {
    return tileNumber;
  }

  /**
   * @return the clusterCount
   */
  public float getClusterCountOccupied() {
    return clusterCountcOccupied;
  }

  /**
   * @return the upperLeftX
   */
  public float getUpperLeftX() {
    return upperLeftX;
  }

  /**
   * @return the upperLeftY
   */
  public float getUpperLeftY() {
    return upperLeftY;
  }

  //
  // Metrics methods
  //

  @Override
  public List<String> fieldNames() {

    if (super.version < 3) {
      return Arrays.asList("Lane", "Tile", "OccupiedCount");
    }

    return Arrays.asList("Lane", "Tile", "OccupiedCount", "Upper Left X", "Upper Left Y");
  }

  @Override
  public List<Number> values() {
    if (super.version < 3) {
      return Arrays.asList(this.laneNumber, this.tileNumber, this.clusterCountcOccupied);
    }

    return Arrays.asList(
        this.laneNumber,
        this.tileNumber,
        this.clusterCountcOccupied,
        this.upperLeftX,
        this.upperLeftY);
  }

  @Override
  public List<Class<?>> fieldTypes() {
    if (super.version < 3) {
      return Arrays.asList(Integer.class, Integer.class, Float.class);
    }

    return Arrays.asList(Integer.class, Integer.class, Float.class, Float.class, Float.class);
  }

  //
  // Parsing methods
  //

  private static float parseClusterCountV1(final ByteBuffer bb) {

    int code = uShortToInt(bb);
    float value = bb.getFloat();

    switch (code) {
      case 0:
        return value;

      default:
        throw new IllegalStateException();
    }
  }

  //
  // Constructor
  //

  ExtendedTileMetric(final int version, final ByteBuffer bb) {

    super.version = version;

    this.laneNumber = uShortToInt(bb);
    this.tileNumber = version > 2 ? uIntToLong(bb) : uShortToInt(bb);

    switch (version) {
      case 1:
        this.clusterCountcOccupied = parseClusterCountV1(bb);
        break;

      case 2:
        this.clusterCountcOccupied = bb.getFloat();
        this.upperLeftX = Float.NaN;
        this.upperLeftY = Float.NaN;
        break;

      case 3:
        this.clusterCountcOccupied = bb.getFloat();
        this.upperLeftX = bb.getFloat();
        this.upperLeftY = bb.getFloat();
        break;

      default:
        throw new IllegalArgumentException();
    }
  }
}
