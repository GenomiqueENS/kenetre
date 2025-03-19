package fr.ens.biologie.genomique.kenetre.illumina.interop;

import com.google.common.base.Splitter;
import fr.ens.biologie.genomique.kenetre.KenetreException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class TileMetricsReaderTest {

  @Test
  public void testHiSeq1500PE100() throws URISyntaxException, KenetreException, IOException {

    testReadFile("interop/HiSeq1500_PE100/InterOp", null);
  }

  @Test
  public void testHiSeq1500SR50() throws URISyntaxException, KenetreException, IOException {

    testReadFile("interop/HiSeq1500_SR50/InterOp", null);
  }

  @Test
  public void testNextSeq500SR75() throws URISyntaxException, KenetreException, IOException {

    testReadFile("interop/NextSeq500_SR75/InterOp", null);
  }

  @Test
  public void testNextSeq50010X() throws URISyntaxException, KenetreException, IOException {

    Map<String, String> map = new HashMap<>();
    map.put("2", "3");

    testReadFile("interop/NextSeq500_10X/InterOp", map);
  }

  @Test
  public void testNextSeq2000SR100() throws URISyntaxException, KenetreException, IOException {

    testReadFile("interop/NextSeq2000_SR100/InterOp", null);
  }

  @Test
  public void testNextSeq2000PE150() throws URISyntaxException, KenetreException, IOException {

    testReadFile("interop/NextSeq2000_PE150/InterOp", null);
  }

  private void testReadFile(String path, Map<String, String> renameReads)
      throws URISyntaxException, KenetreException, IOException {

    if (renameReads == null) {
      renameReads = Collections.emptyMap();
    }

    String binFilename = "TileMetricsOut.bin";
    String csvFilename = "TileMetricsOut.csv";

    // Get the file URL, not working in JAR file.
    URL binResource = getClass().getClassLoader().getResource(path + '/' + binFilename);

    if (binResource == null) {
      throw new IllegalArgumentException("file not found!");
    }

    File binFile = new File(binResource.toURI());
    File binDir = binFile.getParentFile();

    // Read metrics from bin file
    List<TileMetric> result = new TileMetricsReader(binDir).readMetrics();

    // Convert result to CSV and sort it
    boolean first = true;
    List<String> binLines = new ArrayList<>();
    List<Class<?>> types = null;
    for (TileMetric m : result) {

      if (first) {
        types = m.fieldTypes();
        first = false;
      }

      for (int i = 0; i < m.getReadCount(); i++) {
        binLines.add(Metric.toCSV(m.values(i)).replace(".000000,", ","));
      }
    }
    Collections.sort(binLines);

    // Read expected metrics from csv file and sort it
    List<String> csvLines = new ArrayList<>();
    try (BufferedReader in =
        new BufferedReader(
            new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(path + '/' + csvFilename)))) {

      String line = null;
      while ((line = in.readLine()) != null) {

        if (!line.startsWith("#") && !line.startsWith("Lane") && !line.trim().isEmpty()) {

          csvLines.add(line);
        }
      }
    }
    Collections.sort(csvLines);

    // Check if the same number of entries is found
    Assert.assertEquals(csvLines.size(), binLines.size());

    // Compare values
    Splitter splitter = Splitter.on(',').trimResults();
    int count = csvLines.size();
    for (int i = 0; i < count; i++) {

      // System.out.println(i);
      // System.out.println(csvLines.get(i));
      // System.out.println(binLines.get(i));

      List<String> csvFields = splitter.splitToList(csvLines.get(i).replace("nan", "NaN"));
      List<String> binFields = splitter.splitToList(binLines.get(i).replace("nan", "NaN"));

      for (int j = 0; j < types.size(); j++) {

        Class<?> c = types.get(j);

        if (c == Float.class) {

          float csvValue = Float.parseFloat(csvFields.get(j));
          float binValue = Float.parseFloat(binFields.get(j));

          csvValue = Float.parseFloat(String.format(Locale.ROOT, "%1.4e", csvValue));
          binValue = Float.parseFloat(String.format(Locale.ROOT, "%1.4e", binValue));

          if (j == 7 && Float.isNaN(csvValue)) {
            continue;
          }

          if ((j == 8 || j == 9) && Float.isNaN(binValue)) {
            continue;
          }

          float delta = csvValue / 100;

          Assert.assertEquals(csvValue, binValue, delta);
        } else {

          String binValue = binFields.get(j);

          if (j == 2 && renameReads.containsKey(binValue)) {
            binValue = renameReads.get(binValue);
          }

          if (!binFields.get(j).equals("-1")) {

            Assert.assertEquals(csvFields.get(j), binValue);
          }
        }
      }
    }
  }
}
