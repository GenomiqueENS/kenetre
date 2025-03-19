package fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

public class SampleSheetXLSXReaderTest {

  private static final String SAMPLE_SHEET_BARCODES_CSV_FILENAME = "sample_sheet_barcodes.csv";
  private static final String SAMPLE_SHEET_BARCODES_XLSX_FILENAME = "sample_sheet_barcodes.xlsx";

  @Test
  public void testReadWrite() throws IOException {

    SampleSheet csvSamplesheet;
    SampleSheet xlsxSamplesheet;

    try (SampleSheetReader reader =
        new SampleSheetCSVReader(loadRessource(SAMPLE_SHEET_BARCODES_CSV_FILENAME))) {

      csvSamplesheet = reader.read();
      assertNotNull(csvSamplesheet);
    }

    try (SampleSheetReader reader =
        new SampleSheetXLSXReader(loadRessource(SAMPLE_SHEET_BARCODES_XLSX_FILENAME))) {

      xlsxSamplesheet = reader.read();
      assertNotNull(xlsxSamplesheet);
    }
    assertEquals(csvSamplesheet, xlsxSamplesheet);
  }

  //
  // Common methods
  //

  private InputStream loadRessource(String filename) {

    return this.getClass().getResourceAsStream("/samplesheets/" + filename);
  }
}
