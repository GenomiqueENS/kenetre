package fr.ens.biologie.genomique.kenetre.illumina;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

import fr.ens.biologie.genomique.kenetre.illumina.RunInfo.Read;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class RunInfoTest {

  private static RunInfoTest obj;

  private RunInfo hiSeq1500PE100;
  private RunInfo hiSeq1500SR50;
  private RunInfo nextSeq2000PE150;
  private RunInfo nextSeq2000SR100;
  private RunInfo nextSeq50010X;
  private RunInfo nextSeq500SR75;

  @BeforeClass
  public static void load() throws ParserConfigurationException, SAXException, IOException {

    obj = new RunInfoTest();

    obj.hiSeq1500PE100 = load("HiSeq1500_PE100");
    obj.hiSeq1500SR50 = load("HiSeq1500_SR50");
    obj.nextSeq2000PE150 = load("NextSeq2000_PE150");
    obj.nextSeq2000SR100 = load("NextSeq2000_SR100");
    obj.nextSeq50010X = load("NextSeq500_10X");
    obj.nextSeq500SR75 = load("NextSeq500_SR75");
  }

  private static RunInfo load(String interOpDir)
      throws ParserConfigurationException, SAXException, IOException {

    InputStream in =
        RunInfoTest.class
            .getClassLoader()
            .getResourceAsStream("interop/" + interOpDir + "/RunInfo.xml");

    return RunInfo.parse(in);
  }

  @Test
  public void testGetTilesCount() {

    assertEquals(64, obj.hiSeq1500PE100.getTilesCount());
    assertEquals(64, obj.hiSeq1500SR50.getTilesCount());
    assertEquals(168, obj.nextSeq2000PE150.getTilesCount());
    assertEquals(132, obj.nextSeq2000SR100.getTilesCount());
    assertEquals(216, obj.nextSeq50010X.getTilesCount());
    assertEquals(216, obj.nextSeq500SR75.getTilesCount());
  }

  @Test
  public void testGetId() {

    assertEquals("130522_SNL110_0069_AH0HU9ADXX", obj.hiSeq1500PE100.getId());
    assertEquals("130926_SNL110_0085_AH0EYHADXX", obj.hiSeq1500SR50.getId());
    assertEquals("210628_VH00567_1_AAAGLG7HV", obj.nextSeq2000PE150.getId());
    assertEquals("211012_VH00567_4_AAAMT2KM5", obj.nextSeq2000SR100.getId());
    assertEquals("180723_NB500892_0277_AHJYM2BGX7", obj.nextSeq50010X.getId());
    assertEquals("181022_NB500892_0297_AHV2YMBGX7", obj.nextSeq500SR75.getId());
  }

  @Test
  public void testGetNumber() {

    assertEquals(69, obj.hiSeq1500PE100.getNumber());
    assertEquals(85, obj.hiSeq1500SR50.getNumber());
    assertEquals(1, obj.nextSeq2000PE150.getNumber());
    assertEquals(1, obj.nextSeq2000SR100.getNumber());
    assertEquals(277, obj.nextSeq50010X.getNumber());
    assertEquals(297, obj.nextSeq500SR75.getNumber());
  }

  @Test
  public void testGetFlowCell() {
    assertEquals("H0HU9ADXX", obj.hiSeq1500PE100.getFlowCell());
    assertEquals("H0EYHADXX", obj.hiSeq1500SR50.getFlowCell());
    assertEquals("AAAGLG7HV", obj.nextSeq2000PE150.getFlowCell());
    assertEquals("AAAMT2KM5", obj.nextSeq2000SR100.getFlowCell());
    assertEquals("HJYM2BGX7", obj.nextSeq50010X.getFlowCell());
    assertEquals("HV2YMBGX7", obj.nextSeq500SR75.getFlowCell());
  }

  @Test
  public void testGetInstrument() {
    assertEquals("SNL110", obj.hiSeq1500PE100.getInstrument());
    assertEquals("SNL110", obj.hiSeq1500SR50.getInstrument());
    assertEquals("VH00567", obj.nextSeq2000PE150.getInstrument());
    assertEquals("VH00567", obj.nextSeq2000SR100.getInstrument());
    assertEquals("NB500892", obj.nextSeq50010X.getInstrument());
    assertEquals("NB500892", obj.nextSeq500SR75.getInstrument());
  }

  @Test
  public void testGetDate() {
    assertEquals("130522", obj.hiSeq1500PE100.getDate());
    assertEquals("130926", obj.hiSeq1500SR50.getDate());
    assertEquals("2021-06-28T08:42:43Z", obj.nextSeq2000PE150.getDate());
    assertEquals("2021-10-12T09:51:18Z", obj.nextSeq2000SR100.getDate());
    assertEquals("180723", obj.nextSeq50010X.getDate());
    assertEquals("181022", obj.nextSeq500SR75.getDate());
  }

  @Test
  public void testGetReads() {

    Map<Integer, Read> map = new HashMap<>();
    for (Read r : obj.hiSeq1500PE100.getReads()) {
      map.put(r.getNumber(), r);
    }

    assertEquals(3, map.size());
    assertEquals(101, map.get(1).getNumberCycles());
    assertEquals(false, map.get(1).isIndexedRead());
    assertEquals(7, map.get(2).getNumberCycles());
    assertEquals(true, map.get(2).isIndexedRead());
    assertEquals(101, map.get(3).getNumberCycles());
    assertEquals(false, map.get(3).isIndexedRead());

    map.clear();
    for (Read r : obj.hiSeq1500SR50.getReads()) {
      map.put(r.getNumber(), r);
    }

    assertEquals(2, map.size());
    assertEquals(51, map.get(1).getNumberCycles());
    assertEquals(false, map.get(1).isIndexedRead());
    assertEquals(7, map.get(2).getNumberCycles());
    assertEquals(true, map.get(2).isIndexedRead());

    map.clear();
    for (Read r : obj.nextSeq2000PE150.getReads()) {
      map.put(r.getNumber(), r);
    }

    assertEquals(2, map.size());
    assertEquals(151, map.get(1).getNumberCycles());
    assertEquals(false, map.get(1).isIndexedRead());
    assertEquals(151, map.get(2).getNumberCycles());
    assertEquals(false, map.get(2).isIndexedRead());

    map.clear();
    for (Read r : obj.nextSeq2000SR100.getReads()) {
      map.put(r.getNumber(), r);
    }

    assertEquals(3, map.size());
    assertEquals(117, map.get(1).getNumberCycles());
    assertEquals(false, map.get(1).isIndexedRead());
    assertEquals(10, map.get(2).getNumberCycles());
    assertEquals(true, map.get(2).isIndexedRead());
    assertEquals(10, map.get(3).getNumberCycles());
    assertEquals(true, map.get(2).isIndexedRead());

    map.clear();
    for (Read r : obj.nextSeq50010X.getReads()) {
      map.put(r.getNumber(), r);
    }

    assertEquals(3, map.size());
    assertEquals(26, map.get(1).getNumberCycles());
    assertEquals(false, map.get(1).isIndexedRead());
    assertEquals(8, map.get(2).getNumberCycles());
    assertEquals(true, map.get(2).isIndexedRead());
    assertEquals(98, map.get(3).getNumberCycles());
    assertEquals(false, map.get(3).isIndexedRead());

    map.clear();
    for (Read r : obj.nextSeq500SR75.getReads()) {
      map.put(r.getNumber(), r);
    }

    assertEquals(2, map.size());
    assertEquals(75, map.get(1).getNumberCycles());
    assertEquals(false, map.get(1).isIndexedRead());
    assertEquals(6, map.get(2).getNumberCycles());
    assertEquals(true, map.get(2).isIndexedRead());
  }

  @Test
  public void testGetFlowCellLaneCount() {
    assertEquals(2, obj.hiSeq1500PE100.getFlowCellLaneCount());
    assertEquals(2, obj.hiSeq1500SR50.getFlowCellLaneCount());
    assertEquals(2, obj.nextSeq2000PE150.getFlowCellLaneCount());
    assertEquals(1, obj.nextSeq2000SR100.getFlowCellLaneCount());
    assertEquals(4, obj.nextSeq50010X.getFlowCellLaneCount());
    assertEquals(4, obj.nextSeq500SR75.getFlowCellLaneCount());
  }

  @Test
  public void testGetFlowCellSurfaceCount() {
    assertEquals(2, obj.hiSeq1500PE100.getFlowCellSurfaceCount());
    assertEquals(2, obj.hiSeq1500SR50.getFlowCellSurfaceCount());
    assertEquals(2, obj.nextSeq2000PE150.getFlowCellSurfaceCount());
    assertEquals(2, obj.nextSeq2000SR100.getFlowCellSurfaceCount());
    assertEquals(2, obj.nextSeq50010X.getFlowCellSurfaceCount());
    assertEquals(2, obj.nextSeq500SR75.getFlowCellSurfaceCount());
  }

  @Test
  public void testGetFlowCellSwathCount() {
    assertEquals(2, obj.hiSeq1500PE100.getFlowCellSwathCount());
    assertEquals(2, obj.hiSeq1500SR50.getFlowCellSwathCount());
    assertEquals(6, obj.nextSeq2000PE150.getFlowCellSwathCount());
    assertEquals(6, obj.nextSeq2000SR100.getFlowCellSwathCount());
    assertEquals(3, obj.nextSeq50010X.getFlowCellSwathCount());
    assertEquals(3, obj.nextSeq500SR75.getFlowCellSwathCount());
  }

  @Test
  public void testGetFlowCellTileCount() {
    assertEquals(16, obj.hiSeq1500PE100.getFlowCellTileCount());
    assertEquals(16, obj.hiSeq1500SR50.getFlowCellTileCount());
    assertEquals(14, obj.nextSeq2000PE150.getFlowCellTileCount());
    assertEquals(11, obj.nextSeq2000SR100.getFlowCellTileCount());
    assertEquals(12, obj.nextSeq50010X.getFlowCellTileCount());
    assertEquals(12, obj.nextSeq500SR75.getFlowCellTileCount());
  }

  @Test
  public void testGetFlowCellSectionPerLane() {
    assertEquals(-1, obj.hiSeq1500PE100.getFlowCellSectionPerLane());
    assertEquals(-1, obj.hiSeq1500SR50.getFlowCellSectionPerLane());
    assertEquals(-1, obj.nextSeq2000PE150.getFlowCellSectionPerLane());
    assertEquals(-1, obj.hiSeq1500SR50.getFlowCellSectionPerLane());
    assertEquals(-1, obj.hiSeq1500SR50.getFlowCellSectionPerLane());
    assertEquals(-1, obj.hiSeq1500SR50.getFlowCellSectionPerLane());
  }

  @Test
  public void testGetFlowCellLanePerSection() {
    assertEquals(-1, obj.hiSeq1500PE100.getFlowCellLanePerSection());
    assertEquals(-1, obj.hiSeq1500SR50.getFlowCellLanePerSection());
    assertEquals(-1, obj.nextSeq2000PE150.getFlowCellLanePerSection());
    assertEquals(-1, obj.nextSeq2000SR100.getFlowCellLanePerSection());
    assertEquals(2, obj.nextSeq50010X.getFlowCellLanePerSection());
    assertEquals(2, obj.nextSeq500SR75.getFlowCellLanePerSection());
  }

  @Test
  public void testGetAlignToPhix() {
    assertEquals(asList(1, 2), obj.hiSeq1500PE100.getAlignToPhix());
    assertEquals(asList(1, 2), obj.hiSeq1500SR50.getAlignToPhix());
    assertEquals(emptyList(), obj.nextSeq2000PE150.getAlignToPhix());
    assertEquals(emptyList(), obj.nextSeq2000SR100.getAlignToPhix());
    assertEquals(emptyList(), obj.nextSeq50010X.getAlignToPhix());
    assertEquals(emptyList(), obj.nextSeq500SR75.getAlignToPhix());
  }

  @Test
  public void testGetImageChannels() {
    assertEquals(emptyList(), obj.hiSeq1500PE100.getImageChannels());
    assertEquals(emptyList(), obj.hiSeq1500SR50.getImageChannels());
    assertEquals(emptyList(), obj.hiSeq1500SR50.getImageChannels());
    assertEquals(asList("green", "blue"), obj.nextSeq2000PE150.getImageChannels());
    assertEquals(asList("green", "blue"), obj.nextSeq2000SR100.getImageChannels());
    assertEquals(asList("Red", "Green"), obj.nextSeq50010X.getImageChannels());
    assertEquals(asList("Red", "Green"), obj.nextSeq500SR75.getImageChannels());
  }
}
