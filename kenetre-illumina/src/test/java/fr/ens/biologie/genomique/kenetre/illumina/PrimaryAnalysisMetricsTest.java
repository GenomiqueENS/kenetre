package fr.ens.biologie.genomique.kenetre.illumina;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

public class PrimaryAnalysisMetricsTest {

  private static final String FILE_1_4_1 = "PrimaryAnalysisMetrics-1.4.1.csv";
  private static final String FILE_1_7_0 = "PrimaryAnalysisMetrics-1.7.0.csv";

  private static final PrimaryAnalysisMetrics load(String filename) throws IOException {

    InputStream in = RunInfoTest.class.getClassLoader().getResourceAsStream(filename);

    return new PrimaryAnalysisMetrics(in);
  }

  @Test
  public void testGetAverageQ30() throws IOException {
    assertEquals(0.931, load(FILE_1_4_1).getAverageQ30(), 0.001);
    assertEquals(0.9155, load(FILE_1_7_0).getAverageQ30(), 0.001);
  }

  @Test
  public void testGetTotalYield() throws IOException {
    assertEquals(72.45e9, load(FILE_1_4_1).getTotalYield(), 0.01e9);
    assertEquals(73.43e9, load(FILE_1_7_0).getTotalYield(), 0.01e9);
  }

  @Test
  public void testGetTotalReads() throws IOException {
    assertEquals(536.70e6, load(FILE_1_4_1).getTotalReadsPF(), 0.01e6);
    assertEquals(547.98e6, load(FILE_1_7_0).getTotalReadsPF(), 0.01e6);
  }

  @Test
  public void testGetLoadingConcentrationPercent() throws IOException {
    assertEquals(0.998, load(FILE_1_4_1).getLoadingConcentrationPercent(), 0.001);
    assertEquals(0.9949, load(FILE_1_7_0).getLoadingConcentrationPercent(), 0.001);
  }
}
