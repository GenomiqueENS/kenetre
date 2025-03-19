package fr.ens.biologie.genomique.kenetre.bio.readfilter;

import com.google.common.base.Splitter;
import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.bio.NanoporeReadId;
import fr.ens.biologie.genomique.kenetre.bio.NanoporeReadId.SequenceType;
import fr.ens.biologie.genomique.kenetre.bio.ReadSequence;
import java.util.Iterator;

/**
 * This class define a filter based on the Nanopore sequence type.
 *
 * @since 2.0
 * @author Laurent Jourdren
 */
public class NanoporeSequenceTypeFilter extends AbstractReadFilter {

  public static final String FILTER_NAME = "nanoporesequencetype";

  private final Splitter spliter = Splitter.on(' ').omitEmptyStrings();
  private NanoporeReadId.SequenceType sequenceType = SequenceType.CONSENSUS;

  @Override
  public String getName() {
    return FILTER_NAME;
  }

  @Override
  public String getDescription() {
    return "Filter nanopore reads against its type";
  }

  @Override
  public boolean accept(final ReadSequence read) {

    if (read == null) {
      return false;
    }

    Iterator<String> it = this.spliter.split(read.getName()).iterator();

    if (!it.hasNext()) {
      return false;
    }

    String sequenceName = it.next();

    switch (this.sequenceType) {
      case CONSENSUS:
        return sequenceName.indexOf('_') == -1;

      case TEMPLATE:
        return sequenceName.endsWith("_t");

      case COMPLEMENT:
        return sequenceName.endsWith("_c");

      default:
        return false;
    }
  }

  @Override
  public void setParameter(final String key, final String value) throws KenetreException {

    if (key == null || value == null) {
      return;
    }

    if ("keep".equals(key.trim())) {

      SequenceType type;

      try {
        type = SequenceType.valueOf(value.toUpperCase().trim());
      } catch (IllegalArgumentException e) {
        throw new KenetreException("Invalid sequence type: " + value);
      }

      this.sequenceType = type;

    } else {
      throw new KenetreException("Unknown parameter for " + getName() + " read filter: " + key);
    }
  }
}
