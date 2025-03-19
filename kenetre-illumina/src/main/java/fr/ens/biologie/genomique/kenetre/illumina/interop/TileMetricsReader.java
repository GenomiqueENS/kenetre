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

import fr.ens.biologie.genomique.kenetre.KenetreException;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class define a specified iterator for reading the binary file: TileMetricsOut.bin.
 *
 * @author Sandrine Perrin
 * @since 1.1
 */
public class TileMetricsReader extends AbstractBinaryFileReader<TileMetric> {

  private float density;
  private Map<Integer, TileMetric> mapTile = new HashMap<>();

  @Override
  public String getName() {

    return "TileMetricsOut";
  }

  @Override
  protected File getMetricsFile() {

    return new File(getDirPathInterOP(), getName() + ".bin");
  }

  @Override
  protected int getExpectedRecordSize(int version) {

    switch (version) {
      case 2:
        return 10;

      case 3:
        return 15;

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

    if (version == 3) {
      this.density = bb.getFloat();
    }
  }

  @Override
  protected void readMetricRecord(List<TileMetric> collection, ByteBuffer bb, int version) {

    int laneNumber = -1;
    long tileNumber = -1;

    switch (version) {
      case 2:
        laneNumber = Short.toUnsignedInt(bb.getShort()); // uShortToInt(bb);
        tileNumber = Short.toUnsignedInt(bb.getShort());
        break;

      case 3:
        laneNumber = uShortToInt(bb);
        tileNumber = uIntToLong(bb);
        break;

      default:
        throw new IllegalArgumentException();
    }

    TileMetric tile;

    int id = TileMetric.id(laneNumber, (int) tileNumber);

    if (this.mapTile.containsKey(id)) {
      tile = this.mapTile.get(id);
    } else {
      tile = new TileMetric(laneNumber, tileNumber);
      this.mapTile.put(id, tile);
      collection.add(tile);
    }

    switch (version) {
      case 2:
        tile.parseV2(bb);
        break;

      case 3:
        tile.parseV3(bb, this.density);
        break;

      default:
        throw new IllegalArgumentException();
    }
  }

  //
  // Constructor
  //

  public TileMetricsReader(final File dirPath) throws KenetreException {
    super(dirPath);
  }
}
