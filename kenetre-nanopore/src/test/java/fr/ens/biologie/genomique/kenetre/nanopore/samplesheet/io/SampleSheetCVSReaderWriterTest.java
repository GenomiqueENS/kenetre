package fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet;
import fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet.Barcode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class SampleSheetCVSReaderWriterTest {

  private static final String SAMPLE_SHEET_NO_BARCODE_FILENAME = "sample_sheet_no_barcode.csv";

  private static final String SAMPLE_SHEET_BARCODES_FILENAME = "sample_sheet_barcodes.csv";

  @Test
  public void testNoBarcode() throws IOException {

    try (SampleSheetReader reader =
        new SampleSheetCSVReader(loadRessource(SAMPLE_SHEET_NO_BARCODE_FILENAME))) {

      SampleSheet s = reader.read();

      Assert.assertFalse(s.isBarcode());
      Assert.assertEquals("7cf95983-53f5-463d-9502-7847a7c42474", s.getProtocolRunId());
      Assert.assertEquals("MN17734", s.getPositionId());
      Assert.assertEquals("FAT94559", s.getFlowCellId());
      Assert.assertEquals("no_sample", s.getSampleId());
      Assert.assertEquals("LIGcDNAoriente_A2022", s.getExperimentId());
      Assert.assertEquals("FLO-MIN106", s.getFlowCellProductCode());
      Assert.assertEquals("SQK-LSK110", s.getKit());
      Assert.assertEquals(Collections.emptyList(), s.getBarcodes());
      assertFalse(s.isDualBarcoding());
    }
  }

  @Test
  public void testBarcode() throws IOException {

    try (SampleSheetReader reader =
        new SampleSheetCSVReader(loadRessource(SAMPLE_SHEET_BARCODES_FILENAME))) {

      SampleSheet s = reader.read();

      assertTrue(s.isBarcode());
      assertNull(s.getProtocolRunId());
      assertEquals("X1", s.getPositionId());
      assertNull(s.getFlowCellId());
      assertEquals("Test", s.getSampleId());
      assertEquals("Sample_sheet", s.getExperimentId());
      assertEquals("FLO-MIN106", s.getFlowCellProductCode());
      assertEquals("SQK-LSK109 EXP-NBD196", s.getKit());
      assertEquals(12, s.getBarcodes().size());
      assertFalse(s.isDualBarcoding());

      assertFalse(s.containsBarcode("barcode00"));
      for (int i = 1; i <= 12; i++) {

        String k = String.format("barcode%02d", i);
        assertTrue(s.containsBarcode(k));
        Barcode b = s.getBarcode(k);
        assertFalse(b.isDualBarcoding());
        assertNull(b.getType());
        assertEquals("Test" + i, b.getAlias());
        assertNull(b.getInternalBarcode());
        assertNull(b.getExternalBarcode());
      }
    }
  }

  @Test
  public void testReadWrite() throws IOException {

    File tmpFile = File.createTempFile("samplesheet-", ".csv");

    SampleSheet inputSamplesheet;
    SampleSheet outputSamplesheet;

    try (SampleSheetReader reader =
            new SampleSheetCSVReader(loadRessource(SAMPLE_SHEET_BARCODES_FILENAME));
        SampleSheetWriter writer = new SampleSheetCSVWriter(tmpFile)) {

      inputSamplesheet = reader.read();
      assertNotNull(inputSamplesheet);
      writer.writer(inputSamplesheet);
    }

    try (SampleSheetReader reader = new SampleSheetCSVReader(tmpFile)) {
      outputSamplesheet = reader.read();
    }

    tmpFile.delete();
    assertEquals(inputSamplesheet, outputSamplesheet);
  }

  //
  // Common methods
  //

  private InputStream loadRessource(String filename) {

    return this.getClass().getResourceAsStream("/samplesheets/" + filename);
  }
}
