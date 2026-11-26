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

package fr.ens.biologie.genomique.eoulsan.util;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * This class define Bloom filters utils. <b>Warning</b>: this class only exists in this package for
 * compatibility issues with deserialisation.
 *
 * @author Sandrine Perrin
 * @since 2.0
 */
public class EnhancedBloomFilter implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final double FALSE_POSITIVE_PROBABILITY_DEFAULT = 0.03;

  private final BloomFilter<String> bf;
  private int addedNumberOfElements;
  private final int expectedNumberOfElements;
  private final double falsePositiveProbability;

  /**
   * Add an element to the bloom filter.
   *
   * @param element the element to add
   */
  public void put(final String element) {
    this.bf.put(element);
    this.addedNumberOfElements++;
  }

  /**
   * Test if an element might be in the bloom filter.
   *
   * @param element the element to test
   * @return true if the element might be in the filter, false if it is definitely not
   */
  public boolean mightContain(final String element) {
    return this.bf.mightContain(element);
  }

  /**
   * Build or retrieve a bloomFilterUtils from a file used .
   *
   * @param fileSer filename to serialization
   * @return BloomFilter completed
   * @throws IOException if an error occurs during deserialization
   */
  public static EnhancedBloomFilter deserializationBloomFilter(final File fileSer)
      throws IOException {

    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileSer))) {

      return (EnhancedBloomFilter) ois.readObject();
    } catch (Exception e) {
      e.printStackTrace();
      throw new IOException("Cannot deserialize file: " + fileSer, e);
    }
  }

  /**
   * Build a serialization file with the instance of bloomFilterUtils
   *
   * @param fileSer filename to serialization
   * @param bloomFilter bloomFilter to serialization
   * @throws IOException if an error occurs during serialization
   */
  public static void serializationBloomFilter(
      final File fileSer, final EnhancedBloomFilter bloomFilter) throws IOException {

    if (bloomFilter == null) {
      throw new IOException("Bloom filter not exists");
    }

    // Serialization BloomFilter
    ObjectOutputStream oos;
    try {
      oos = new ObjectOutputStream(new FileOutputStream(fileSer));
      oos.writeObject(bloomFilter);
      oos.flush();
      oos.close();

    } catch (FileNotFoundException e) {
      throw new IOException("Cannot serialize file: " + fileSer, e);
    }
  }

  /**
   * Compare parameters used to create bloom filter
   *
   * @param that bloom filter to compare
   * @return same parameters true else false
   */
  public boolean sameConfigurationFilter(final EnhancedBloomFilter that) {
    return getExpectedNumberOfElements() == that.getExpectedNumberOfElements()
        && getFalsePositiveProbability() == that.getFalsePositiveProbability();
  }

  //
  // Getter and Setter
  //
  /**
   * Get the bloom filter.
   *
   * @return the bloom filter
   */
  public BloomFilter<String> getBf() {
    return this.bf;
  }

  /**
   * Get number of elements added in bloom filter.
   *
   * @return the number of elements added in bloom filter
   */
  public int getAddedNumberOfElements() {
    return this.addedNumberOfElements;
  }

  /**
   * Get the expected number of elements.
   *
   * @return the expected number of elements
   */
  public int getExpectedNumberOfElements() {
    return this.expectedNumberOfElements;
  }

  /**
   * Get the false positive rate.
   *
   * @return the false positive rate
   */
  public double getFalsePositiveProbability() {
    return this.falsePositiveProbability;
  }

  @Override
  public String toString() {

    return "Bloom filter features"
        + "\n\tfalse positive probability "
        + getFalsePositiveProbability()
        + "%"
        + "\n\tnumber elements expected "
        + getExpectedNumberOfElements()
        + "\n\tnumber elements added "
        + getAddedNumberOfElements();
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   *
   * @param expectedNumberOfElements parameter to create bloom filter
   */
  public EnhancedBloomFilter(final int expectedNumberOfElements) {
    this(expectedNumberOfElements, FALSE_POSITIVE_PROBABILITY_DEFAULT);
  }

  /**
   * Public constructor.
   *
   * @param expectedNumberOfElements parameter to create bloom filter, must be positive
   * @param falsePositiveProbability parameter to create bloom filter, must be between 0 and 100%
   */
  public EnhancedBloomFilter(
      final int expectedNumberOfElements, final double falsePositiveProbability) {

    // Check parameter
    if (expectedNumberOfElements <= 0) {
      throw new IllegalArgumentException(
          "Parameter 'expectedNumberOfElements' to create bloom filter invalid "
              + expectedNumberOfElements);
    }

    if (falsePositiveProbability <= 0 || falsePositiveProbability >= 1.0) {
      throw new IllegalArgumentException(
          "Parameter 'falsePositiveProbability' to create bloom filter invalid "
              + falsePositiveProbability);
    }

    this.addedNumberOfElements = 0;
    this.expectedNumberOfElements = expectedNumberOfElements;
    this.falsePositiveProbability = falsePositiveProbability;

    this.bf =
        BloomFilter.create(
            new Funnel<String>() {

              private static final long serialVersionUID = 1L;

              @Override
              public void funnel(final String from, final PrimitiveSink into) {
                into.putString(from, StandardCharsets.UTF_8);
              }
            },
            expectedNumberOfElements,
            falsePositiveProbability);
  }

  /**
   * Public constructor.
   *
   * @param bf the Bloom filters
   * @param addedNumberOfElements added number of elements
   * @param expectedNumberOfElements parameter to create bloom filter, must be positive
   * @param falsePositiveProbability parameter to create bloom filter, must be between 0 and 100%
   */
  public EnhancedBloomFilter(
      final BloomFilter<String> bf,
      final int addedNumberOfElements,
      final int expectedNumberOfElements,
      final double falsePositiveProbability) {

    if (bf == null) {
      throw new NullPointerException("bf argument cannot be null");
    }

    this.bf = bf.copy();
    this.addedNumberOfElements = addedNumberOfElements;
    this.expectedNumberOfElements = expectedNumberOfElements;
    this.falsePositiveProbability = falsePositiveProbability;
  }
}
