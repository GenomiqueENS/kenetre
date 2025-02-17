/*
 *                  Eoulsan development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public License version 2.1 or
 * later and CeCILL-C. This should be distributed with the code.
 * If you do not have a copy, see:
 *
 *      http://www.gnu.org/licenses/lgpl-2.1.txt
 *      http://www.cecill.info/licences/Licence_CeCILL-C_V1-en.txt
 *
 * Copyright for this code is held jointly by the Genomic platform
 * of the Institut de Biologie de l'École normale supérieure and
 * the individual authors. These should be listed in @author doc
 * comments.
 *
 * For more information on the Eoulsan project and its aims,
 * or to join the Eoulsan Google group, visit the home page
 * at:
 *
 *      http://outils.genomique.biologie.ens.fr/eoulsan
 *
 */
package fr.ens.biologie.genomique.kenetre.it.comparator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collection;

import com.google.common.collect.Sets;

import fr.ens.biologie.genomique.eoulsan.util.EnhancedBloomFilter;

/**
 * This class allow compare two text files with use BloomFilter.
 * @since 2.0
 * @author Sandrine Perrin
 */
public class TextComparator extends AbstractComparatorWithBloomFilter {

  private static final String NAME_COMPARATOR = "TextComparator";
  private static final Collection<String> EXTENSIONS =
      Sets.newHashSet(".txt", ".tsv", ".csv", ".xml");

  private int numberElementsCompared;

  @Override
  public boolean compareFiles(final EnhancedBloomFilter filter,
      final InputStream is) throws IOException {

    final BufferedReader reader =
        new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()));
    String line = null;
    this.numberElementsCompared = 0;

    while ((line = reader.readLine()) != null) {
      this.numberElementsCompared++;

      if (!filter.mightContain(line)) {
        // Save line occurs fail comparison
        setCauseFailComparison(line);

        reader.close();
        return false;
      }
    }
    reader.close();

    // Check count element is the same between two files
    if (this.numberElementsCompared != filter.getAddedNumberOfElements()) {
      setCauseFailComparison("Different count elements "
          + this.numberElementsCompared + " was "
          + filter.getAddedNumberOfElements() + " expected.");
      return false;
    }
    return true;
  }

  //
  // Getter
  //
  @Override
  public Collection<String> getExtensions() {
    return EXTENSIONS;
  }

  @Override
  public String getName() {

    return NAME_COMPARATOR;
  }

  @Override
  public int getNumberElementsCompared() {
    return this.numberElementsCompared;
  }

  //
  // Constructor
  //

  /**
   * Public constructor
   * @param useSerializeFile true if it needed to save BloomFilter in file with
   *          extension '.ser'
   */
  public TextComparator(final boolean useSerializeFile) {
    super(useSerializeFile);
  }
}
