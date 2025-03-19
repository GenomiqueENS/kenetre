package fr.ens.biologie.genomique.kenetre.bio.readfilter;

import org.usadellab.trimmomatic.trim.AbstractSingleRecordTrimmer;
import org.usadellab.trimmomatic.trim.LeadingTrimmer;

/**
 * This class define a Leading trimmomatic readfilter allow paired-end and single-end
 *
 * @since 1.0
 * @author du
 */
public class LeadingTrimmerReadFilter extends AbstractTrimmomaticReadFilter {

  @Override
  public String getName() {
    return "leading";
  }

  @Override
  protected AbstractSingleRecordTrimmer createTrimmer(String trimmerArgs) {
    return new LeadingTrimmer(trimmerArgs);
  }
}
