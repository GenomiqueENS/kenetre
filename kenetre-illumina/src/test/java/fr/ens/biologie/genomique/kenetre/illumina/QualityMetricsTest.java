package fr.ens.biologie.genomique.kenetre.illumina;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.Test;

public class QualityMetricsTest {

  @Test
  public void test() throws IOException {

    InputStream in = RunInfoTest.class.getClassLoader().getResourceAsStream("Quality_Metrics.csv");

    QualityMetrics ds = new QualityMetrics(in);

    List<QualityMetrics.Entry> entries = ds.entries();

    assertEquals(13, entries.size());

    QualityMetrics.Entry e = entries.get(0);

    assertEquals(1, e.getLane());
    assertEquals("2021-437", e.getSampleID());
    assertEquals("CTCGCTTCGG", e.getIndex());
    assertEquals("TTGACTAGTA", e.getIndex2());
    assertEquals(1, e.getReadNumber());
    assertEquals(4474777120L, e.getYield());
    assertEquals(4187939366L, e.getYieldQ30());
    assertEquals(147869541810L, e.getQualityScoreSum());
    assertEquals(33.05, e.getMeanQualityScorePF(), 0.01);
    assertEquals(0.94, e.getQ30Percent(), 0.1);

    e = entries.get(1);

    assertEquals(1, e.getLane());
    assertEquals("2021-438", e.getSampleID());
    assertEquals("CTGTTGGTCC", e.getIndex());
    assertEquals("AACGGTCTAT", e.getIndex2());
    assertEquals(1, e.getReadNumber());
    assertEquals(4747270036L, e.getYield());
    assertEquals(4444783806L, e.getYieldQ30());
    assertEquals(156942971400L, e.getQualityScoreSum());
    assertEquals(33.06, e.getMeanQualityScorePF(), 0.01);
    assertEquals(0.94, e.getQ30Percent(), 0.1);
  }
}
