package fr.ens.biologie.genomique.kenetre.bio.readfilter;

import static fr.ens.biologie.genomique.kenetre.bio.Sequence.reverseComplement;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.bio.Alphabets;
import fr.ens.biologie.genomique.kenetre.bio.ReadSequence;
import java.util.ArrayList;
import java.util.List;

/**
 * This filter allow to detect primer at the ends of reads.
 *
 * @author Laurent Jourdren
 */
public class POPSReadFilter extends AbstractReadFilter {

  private static final String PRIMER1_DEFAULT = "GTTATACGCTGATGATTTCCCCTGC";
  private static final String PRIMER2_DEFAULT = "AAGCAGTGGTATCAACGCAGAGTAC";

  private enum TailType {
    UNDEFINED("undefined"),
    POLYA("polyA"),
    POLYT("polyT"),
    AMBIGUOUS("ambiguous");

    String name;

    @Override
    public String toString() {
      return this.name;
    }

    TailType(String name) {
      this.name = name;
    }
  };

  private String primer1 = PRIMER1_DEFAULT;
  private String rcPrimer1 = reverseComplement(this.primer1, Alphabets.READ_DNA_ALPHABET);

  private String primer2 = PRIMER2_DEFAULT;
  private String rcPrimer2 = reverseComplement(primer2, Alphabets.READ_DNA_ALPHABET);

  private final int lengthSearch = 200;
  private int maxMismatches = 0;

  @Override
  public String getName() {

    return "pops";
  }

  @Override
  public String getDescription() {
    return "POPS read filter";
  }

  @Override
  public void setParameter(final String key, final String value) throws KenetreException {

    if (key == null || value == null) {
      return;
    }

    switch (key.trim()) {
      case "primer1":
        this.primer1 = value.trim();
        this.rcPrimer1 = reverseComplement(this.primer1, Alphabets.READ_DNA_ALPHABET);
        break;

      case "primer2":
        this.primer2 = value.trim();
        this.rcPrimer2 = reverseComplement(this.primer2, Alphabets.READ_DNA_ALPHABET);
        break;

      case "max.mismatches":
        try {
          this.maxMismatches = Integer.parseInt(value.trim());

          if (this.maxMismatches < 0) {
            throw new KenetreException(
                "Invalid value parameter for " + getName() + " read filter: " + key);
          }

        } catch (NumberFormatException e) {
          throw new KenetreException(
              "Invalid value parameter for " + getName() + " read filter: " + key);
        }
        break;

      default:
        throw new KenetreException("Unknown parameter for " + getName() + " read filter: " + key);
    }
  }

  @Override
  public boolean accept(ReadSequence read) {

    String seq = read.getSequence();
    String headSeq = seq.substring(0, Math.min(this.lengthSearch, seq.length()));
    String tailSeq =
        seq.substring(seq.length() < this.lengthSearch ? 0 : seq.length() - this.lengthSearch);

    TailType result = TailType.UNDEFINED;
    int countPolyA = 0;
    int countPolyT = 0;

    if (match(this.primer1, tailSeq, this.maxMismatches)) {
      result = setResult(result, TailType.POLYA);
      countPolyA++;
    }

    if (match(this.rcPrimer1, headSeq, this.maxMismatches)) {
      result = setResult(result, TailType.POLYT);
      countPolyT++;
    }

    if (match(this.primer2, headSeq, this.maxMismatches)) {
      result = setResult(result, TailType.POLYA);
      countPolyA++;
    }

    if (match(this.rcPrimer2, tailSeq, this.maxMismatches)) {
      result = setResult(result, TailType.POLYT);
      countPolyT++;
    }

    read.setName(
        read.getName()
            + " tail_type=\""
            + result
            + '"'
            + " polyA_primer_count=\""
            + countPolyA
            + "\" polyT_primer_count=\""
            + countPolyT
            + "\"");

    return true;
  }

  private static TailType setResult(TailType previousResult, TailType newResult) {

    if (previousResult == TailType.UNDEFINED) {
      return newResult;
    }

    return previousResult == newResult ? newResult : TailType.AMBIGUOUS;
  }

  private static boolean match(String primer, String seq, int maxMismatches) {

    return maxMismatches == 0 ? seq.contains(primer) : !find(seq, primer, maxMismatches).isEmpty();
  }

  /**
   * Bitap algorithm implementation from @see https://github.com/ordinaryman09/Bitap-in-Java Return
   * the list of indexes where the pattern was found. The indexes are not exacts because of the
   * addition and deletion : Example : the text "aa bb cc" with the pattern "bb" and k=1 will match
   * " b","b","bb","b ". and only the index of the first result " b" is added to the list even if
   * "bb" have q lower error rate.
   *
   * @param doc
   * @param pattern
   * @param k
   * @return
   */
  private static List<Integer> find(String doc, String pattern, int k) {

    // Range of the alphabet
    // 128 is enough if we stay in the ASCII range (0-127)
    int alphabetRange = 128;
    int firstMatchedText = -1;
    // Indexes where the pattern was found
    ArrayList<Integer> indexes = new ArrayList<Integer>();
    long[] r = new long[k + 1];

    long[] patternMask = new long[alphabetRange];
    for (int i = 0; i <= k; i++) {
      r[i] = 1;
    }

    // Example : The mask for the letter 'e' and the pattern "hello" is
    // 11101 (0 means this letter is at this place in the pattern)
    for (int i = 0; i < pattern.length(); ++i) {
      patternMask[(int) pattern.charAt(i)] |= 1 << i;
    }
    int i = 0;

    while (i < doc.length()) {

      long old = 0;
      long nextOld = 0;

      for (int d = 0; d <= k; ++d) {
        // Three operations of the Levenshtein distance
        long sub = (old | (r[d] & patternMask[doc.charAt(i)])) << 1;
        long ins = old | ((r[d] & patternMask[doc.charAt(i)]) << 1);
        long del = (nextOld | (r[d] & patternMask[doc.charAt(i)])) << 1;
        old = r[d];
        r[d] = sub | ins | del | 1;
        nextOld = r[d];
      }
      // When r[k] is full of zeros, it means we matched the pattern
      // (modulo k errors)
      if (0 < (r[k] & (1 << pattern.length()))) {
        // The pattern "aaa" for the document "bbaaavv" with k=2
        // will slide from "bba","baa","aaa","aav","avv"
        // Because we allow two errors !
        // This test keep only the first one and skip all the others.
        // (We can't skip by increasing i otherwise the r[d] will be
        // wrong)
        if ((firstMatchedText == -1) || (i - firstMatchedText > pattern.length())) {
          firstMatchedText = i;
          indexes.add(firstMatchedText - pattern.length() + 1);
        }
      }
      i++;
    }

    return indexes;
  }
}
