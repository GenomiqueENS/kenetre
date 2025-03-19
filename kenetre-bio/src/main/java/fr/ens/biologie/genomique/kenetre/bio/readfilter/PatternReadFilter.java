package fr.ens.biologie.genomique.kenetre.bio.readfilter;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.bio.ReadSequence;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This class define a read filter that filter the special motif
 *
 * @since 2.0
 * @author Runxin DU
 */
public class PatternReadFilter extends AbstractReadFilter {

  private Pattern allowedPattern;
  private Pattern forbiddenPattern;

  @Override
  public String getName() {
    return "readsequenceregex";
  }

  @Override
  public String getDescription() {
    return "Pattern Read Filter";
  }

  @Override
  public boolean accept(ReadSequence read) {
    if (read == null) {
      return false;
    }

    final String seq = read.getSequence();
    if (seq == null) {
      return false;
    }

    if (this.forbiddenPattern != null && this.forbiddenPattern.matcher(seq).find()) {
      return false;
    }

    if (this.allowedPattern != null && !this.allowedPattern.matcher(seq).find()) {
      return false;
    }
    return true;
  }

  @Override
  public void setParameter(final String key, final String value) throws KenetreException {
    if (key == null || value == null) {
      return;
    }

    if ("forbidden.regex".equals(key.trim())) {
      try {
        this.forbiddenPattern = Pattern.compile(value);
      } catch (PatternSyntaxException e) {
        throw new KenetreException("Invalid motif: " + getName() + " read filter: " + value);
      }

    } else if ("allowed.regex".equals(key.trim())) {
      try {
        this.allowedPattern = Pattern.compile(value);
      } catch (PatternSyntaxException e) {
        throw new KenetreException("valid motif: " + getName() + " read filter: " + value);
      }
    } else {
      throw new KenetreException("Unknown parameter for " + getName() + " read filter: " + key);
    }
  }
}
