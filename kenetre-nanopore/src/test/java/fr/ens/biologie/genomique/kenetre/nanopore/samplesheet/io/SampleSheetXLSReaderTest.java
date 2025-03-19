package fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

public class SampleSheetXLSReaderTest {

  private static final String SAMPLE_SHEET_BARCODES_CSV_FILENAME = "sample_sheet_barcodes.csv";
  private static final String SAMPLE_SHEET_BARCODES_XLS_FILENAME = "sample_sheet_barcodes.xls";

  @Test
  public void testReadWrite() throws IOException {

    SampleSheet csvSamplesheet;
    SampleSheet xlsSamplesheet;

    try (SampleSheetReader reader =
        new SampleSheetCSVReader(loadRessource(SAMPLE_SHEET_BARCODES_CSV_FILENAME))) {

      csvSamplesheet = reader.read();
      assertNotNull(csvSamplesheet);
    }

    try (SampleSheetReader reader =
        new SampleSheetXLSReader(loadRessource(SAMPLE_SHEET_BARCODES_XLS_FILENAME))) {

      xlsSamplesheet = reader.read();
      assertNotNull(xlsSamplesheet);
    }
    assertEquals(csvSamplesheet, xlsSamplesheet);
  }

  //
  // Common methods
  //

  private InputStream loadRessource(String filename) {

    return this.getClass().getResourceAsStream("/samplesheets/" + filename);
  }
}
