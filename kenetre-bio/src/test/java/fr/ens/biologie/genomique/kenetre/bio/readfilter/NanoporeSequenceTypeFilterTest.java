package fr.ens.biologie.genomique.kenetre.bio.readfilter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.bio.ReadSequence;
import org.junit.Test;

public class NanoporeSequenceTypeFilterTest {

  @Test
  public void testAcceptReadSequence() throws KenetreException {

    //
    // Template
    //

    ReadFilter filter = new NanoporeSequenceTypeFilter();
    filter.setParameter("keep", "template");
    filter.init();

    assertFalse(filter.accept(null));

    ReadSequence read = new ReadSequence("e122e34a-80bf-4fd6-bdbf-d1d3cb44f3bd_t", "ATG", "ABC");
    assertTrue(filter.accept(read));

    read = new ReadSequence("e122e34a-80bf-4fd6-bdbf-d1d3cb44f3bd_c", "ATG", "ABC");
    assertFalse(filter.accept(read));

    read = new ReadSequence("e122e34a-80bf-4fd6-bdbf-d1d3cb44f3bd", "ATG", "ABC");
    assertFalse(filter.accept(read));

    read = new ReadSequence("foobar", "ATG", "ABC");
    assertFalse(filter.accept(read));

    //
    // Complement
    //

    filter = new NanoporeSequenceTypeFilter();
    filter.setParameter("keep", "complement");
    filter.init();

    assertFalse(filter.accept(null));

    read = new ReadSequence("e122e34a-80bf-4fd6-bdbf-d1d3cb44f3bd_t", "ATG", "ABC");
    assertFalse(filter.accept(read));

    read = new ReadSequence("e122e34a-80bf-4fd6-bdbf-d1d3cb44f3bd_c", "ATG", "ABC");
    assertTrue(filter.accept(read));

    read = new ReadSequence("e122e34a-80bf-4fd6-bdbf-d1d3cb44f3bd", "ATG", "ABC");
    assertFalse(filter.accept(read));

    read = new ReadSequence("foobar", "ATG", "ABC");
    assertFalse(filter.accept(read));

    // Consensus

    filter = new NanoporeSequenceTypeFilter();
    filter.setParameter("keep", "consensus");
    filter.init();

    assertFalse(filter.accept(null));

    read = new ReadSequence("e122e34a-80bf-4fd6-bdbf-d1d3cb44f3bd_t", "ATG", "ABC");
    assertFalse(filter.accept(read));

    read = new ReadSequence("e122e34a-80bf-4fd6-bdbf-d1d3cb44f3bd_c", "ATG", "ABC");
    assertFalse(filter.accept(read));

    read = new ReadSequence("e122e34a-80bf-4fd6-bdbf-d1d3cb44f3bd", "ATG", "ABC");
    assertTrue(filter.accept(read));

    read = new ReadSequence("foobar", "ATG", "ABC");
    assertTrue(filter.accept(read));

    filter = new QualityReadFilter();
    filter.setParameter("threshold", "40");
    filter.init();
    assertFalse(filter.accept(read));
  }
}
