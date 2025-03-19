package fr.ens.biologie.genomique.kenetre.bio.readfilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.bio.ReadSequence;
import org.junit.Test;

public class CropTrimmerReadFilterTest {
  @Test
  public void CropTirmmertest() throws KenetreException {
    ReadFilter filter = new CropTrimmerReadFilter();

    filter.setParameter("arguments", "5");
    filter.init();

    ReadSequence read = new ReadSequence("read1", "AGGGGGCAAA", "xwxwxxabcd");
    assertTrue(filter.accept(read));
    assertEquals("read1", read.getName());
    assertEquals("AGGGG", read.getSequence());
    assertEquals("xwxwx", read.getQuality());
    assertFalse(filter.accept(null));
  }
}
