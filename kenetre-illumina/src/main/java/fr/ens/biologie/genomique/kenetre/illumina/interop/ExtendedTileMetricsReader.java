package fr.ens.biologie.genomique.kenetre.illumina.interop;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class define a specified iterator for reading the binary file: ExtendedTileMetricsOut.bin.
 *
 * @author Laurent Jourdren
 * @since 0.3
 */
public class ExtendedTileMetricsReader extends AbstractBinaryFileReader<ExtendedTileMetric> {

  public static final String NAME = "ExtendedTileMetricsOut";

  public static final String METRICS_FILE = "ExtendedTileMetricsOut.bin";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected File getMetricsFile() {

    return new File(getDirPathInterOP(), METRICS_FILE);
  }

  @Override
  protected int getExpectedRecordSize(int version) {

    switch (version) {
      case 1:
      case 2:
        return 10;

      case 3:
        return 18;

      default:
        throw new IllegalArgumentException();
    }
  }

  @Override
  protected Set<Integer> getExpectedVersions() {

    return new HashSet<Integer>(Arrays.asList(1, 2, 3));
  }

  @Override
  protected void readMetricRecord(List<ExtendedTileMetric> collection, ByteBuffer bb, int version) {

    collection.add(new ExtendedTileMetric(version, bb));
  }

  //
  // Constructor
  //

  public ExtendedTileMetricsReader(final File dirPath) throws KenetreException {
    super(dirPath);
  }
}
