package fr.ens.biologie.genomique.kenetre.nanopore.samplesheet;

import static org.junit.Assert.assertTrue;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import org.junit.Test;

public class SampleSheetCheckerTest {

  @Test
  public void checkTest() {

    SampleSheet s = new SampleSheet();
    SampleSheetChecker checker = new SampleSheetChecker();

    s.setPositionId("MN17734");
    s.setFlowCellId("FAT94559");
    s.setSampleId("no_sample");
    s.setExperimentId("LIGcDNAoriente_A2022");
    s.setFlowCellProductCode("FLO-MIN106");
    s.setKit("SQK-LSK110");

    assertPass(checker, s);

    s.setFlowCellProductCode("FLO-MIN10X");
    assertFail(checker, s);

    s.setFlowCellProductCode("FLO-MIN106");
    s.setKit("SQK-LSK11X");
    assertFail(checker, s);

    s.setKit("SQK-LSK110");
    assertPass(checker, s);

    s.setKit("SQK-LSK110   EXP-NBD19X");
    assertFail(checker, s);

    s.setKit("SQK-LSK110   EXP-NBD196");
    assertPass(checker, s);
  }

  private void assertFail(SampleSheetChecker checker, SampleSheet sampleSheet) {

    try {
      checker.check(sampleSheet);
      assertTrue(false);
    } catch (KenetreException e) {
      assertTrue(true);
    }
  }

  private void assertPass(SampleSheetChecker checker, SampleSheet sampleSheet) {

    try {
      checker.check(sampleSheet);
      assertTrue(true);
    } catch (KenetreException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }
}
