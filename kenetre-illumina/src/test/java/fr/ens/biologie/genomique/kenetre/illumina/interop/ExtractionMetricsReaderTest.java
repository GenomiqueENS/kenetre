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
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ExtractionMetricsReaderTest {

  @Test
  public void testHiSeq1500PE100() throws URISyntaxException, KenetreException, IOException {

    testReadFile("interop/HiSeq1500_PE100/InterOp");
  }

  @Test
  public void testHiSeq1500SR50() throws URISyntaxException, KenetreException, IOException {

    testReadFile("interop/HiSeq1500_SR50/InterOp");
  }

  @Test
  public void testNextSeq500SR75() throws URISyntaxException, KenetreException, IOException {

    testReadFile("interop/NextSeq500_SR75/InterOp");
  }

  @Test
  public void testNextSeq50010X() throws URISyntaxException, KenetreException, IOException {

    testReadFile("interop/NextSeq500_10X/InterOp");
  }

  @Test
  public void testNextSeq2000SR100() throws URISyntaxException, KenetreException, IOException {

    testReadFile("interop/NextSeq2000_SR100/InterOp");
  }

  @Test
  public void testNextSeq2000PE150() throws URISyntaxException, KenetreException, IOException {

    testReadFile("interop/NextSeq2000_PE150/InterOp");
  }

  private void testReadFile(String path) throws URISyntaxException, KenetreException, IOException {

    String binFilename = "ExtractionMetricsOut.bin";
    String csvFilename = "ExtractionMetricsOut.csv";

    // Get the file URL, not working in JAR file.
    URL binResource = getClass().getClassLoader().getResource(path + '/' + binFilename);

    if (binResource == null) {
      throw new IllegalArgumentException("file not found!");
    }

    File binFile = new File(binResource.toURI());
    File binDir = binFile.getParentFile();

    // Read metrics from bin file
    List<ExtractionMetric> result = new ExtractionMetricsReader(binDir).readMetrics();

    // Convert result to CSV and sort it
    boolean first = true;
    List<String> binLines = new ArrayList<>();
    List<Class<?>> types = null;
    for (ExtractionMetric m : result) {

      if (first) {
        types = m.fieldTypes();
        first = false;
      }

      binLines.add(m.toCSV());
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

        if (!line.startsWith("#") && !line.startsWith("Lane")) {

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

          Assert.assertEquals(
              Float.parseFloat(csvFields.get(j)), Float.parseFloat(binFields.get(j)), 0.0001);
        } else {

          if (!binFields.get(j).equals("-1")) {

            Assert.assertEquals(csvFields.get(j), binFields.get(j));
          }
        }
      }
    }
  }
}
