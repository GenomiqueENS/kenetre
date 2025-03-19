package fr.ens.biologie.genomique.kenetre.illumina;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.Test;

public class DemultiplexStatsTest {

  @Test
  public void testV12() throws IOException {

    InputStream in =
        RunInfoTest.class.getClassLoader().getResourceAsStream("Demultiplex_Stats-1.2.0.csv");

    DemultiplexStats ds = new DemultiplexStats(in);

    List<DemultiplexStats.Entry> entries = ds.entries();

    assertEquals(13, entries.size());

    DemultiplexStats.Entry e = entries.get(0);

    assertEquals(1, e.getLane());
    assertEquals("2021-033", e.getSampleID());
    assertEquals("ATCACG", e.getIndex());
    assertEquals(39043991, e.getReadCount());
    assertEquals(39043991, e.getPerfectIndexReadCount());
    assertEquals(0, e.getOneMismatchIndexReadCount());
    assertEquals(4768008939L, e.getQ30BaseCount());
    assertEquals(33.14, e.getMeanPFQualityScore(), 0.01);

    assertEquals(-1, e.getTwoMismatchIndexReadCount());
    assertEquals(Double.NaN, e.getReadsPercent(), 0.1);
    assertEquals(Double.NaN, e.getPerfectIndexreadsPercent(), 0.1);
    assertEquals(Double.NaN, e.getOneMismatchIndexReadPercent(), 0.1);
    assertEquals(Double.NaN, e.getTwoMismatchIndexReadPercent(), 0.1);

    e = entries.get(1);

    assertEquals(1, e.getLane());
    assertEquals("2021-034", e.getSampleID());
    assertEquals("CGATGT", e.getIndex());
    assertEquals(46395019, e.getReadCount());
    assertEquals(46395019, e.getPerfectIndexReadCount());
    assertEquals(0, e.getOneMismatchIndexReadCount());
    assertEquals(5677673164L, e.getQ30BaseCount());
    assertEquals(33.18, e.getMeanPFQualityScore(), 0.01);

    assertEquals(-1, e.getTwoMismatchIndexReadCount());
    assertEquals(Double.NaN, e.getReadsPercent(), 0.1);
    assertEquals(Double.NaN, e.getPerfectIndexreadsPercent(), 0.1);
    assertEquals(Double.NaN, e.getOneMismatchIndexReadPercent(), 0.1);
    assertEquals(Double.NaN, e.getTwoMismatchIndexReadPercent(), 0.1);
  }

  @Test
  public void testV14() throws IOException {

    InputStream in =
        RunInfoTest.class.getClassLoader().getResourceAsStream("Demultiplex_Stats-1.4.1.csv");

    DemultiplexStats ds = new DemultiplexStats(in);

    List<DemultiplexStats.Entry> entries = ds.entries();

    assertEquals(13, entries.size());

    DemultiplexStats.Entry e = entries.get(0);

    assertEquals(1, e.getLane());
    assertEquals("2021-437", e.getSampleID());
    assertEquals("CTCGCTTCGG-TTGACTAGTA", e.getIndex());
    assertEquals(37921840, e.getReadCount());
    assertEquals(37921840, e.getPerfectIndexReadCount());
    assertEquals(0, e.getOneMismatchIndexReadCount());
    assertEquals(-1, e.getQ30BaseCount());
    assertEquals(Double.NaN, e.getMeanPFQualityScore(), 0.01);

    assertEquals(0, e.getTwoMismatchIndexReadCount());
    assertEquals(0.0707, e.getReadsPercent(), 0.1);
    assertEquals(1.0, e.getPerfectIndexreadsPercent(), 0.1);
    assertEquals(0.0, e.getOneMismatchIndexReadPercent(), 0.1);
    assertEquals(0.0, e.getTwoMismatchIndexReadPercent(), 0.1);

    e = entries.get(1);

    assertEquals(1, e.getLane());
    assertEquals("2021-438", e.getSampleID());
    assertEquals("CTGTTGGTCC-AACGGTCTAT", e.getIndex());
    assertEquals(40231102, e.getReadCount());
    assertEquals(40231102, e.getPerfectIndexReadCount());
    assertEquals(0, e.getOneMismatchIndexReadCount());
    assertEquals(-1, e.getQ30BaseCount());
    assertEquals(Double.NaN, e.getMeanPFQualityScore(), 0.01);

    assertEquals(0, e.getTwoMismatchIndexReadCount());
    assertEquals(0.0750, e.getReadsPercent(), 0.1);
    assertEquals(1.0, e.getPerfectIndexreadsPercent(), 0.1);
    assertEquals(0.0, e.getOneMismatchIndexReadPercent(), 0.1);
    assertEquals(0.0, e.getTwoMismatchIndexReadPercent(), 0.1);
  }
}
