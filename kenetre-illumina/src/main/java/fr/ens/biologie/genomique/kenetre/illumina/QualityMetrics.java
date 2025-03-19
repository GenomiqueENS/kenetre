package fr.ens.biologie.genomique.kenetre.illumina;

import static fr.ens.biologie.genomique.kenetre.illumina.CSVUtils.parseDouble;
import static fr.ens.biologie.genomique.kenetre.illumina.CSVUtils.parseInt;
import static fr.ens.biologie.genomique.kenetre.illumina.CSVUtils.parseLong;
import static fr.ens.biologie.genomique.kenetre.illumina.CSVUtils.parseString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class QualityMetrics {

  private List<Entry> entries = new ArrayList<>();

  public static class Entry {

    private final int lane;
    private final String sampleID;
    private final String index;
    private final String index2;
    private final int readNumber;
    private final long yield;
    private final long yieldQ30;
    private final long qualityScoreSum;
    private final double meanQualityScorePF;
    private final double q30Percent;

    /**
     * Get the lane.
     *
     * @return the lane
     */
    public int getLane() {
      return lane;
    }

    /**
     * Get the sample ID.
     *
     * @return the sample ID
     */
    public String getSampleID() {
      return sampleID;
    }

    /**
     * Get the index.
     *
     * @return the index
     */
    public String getIndex() {
      return index;
    }

    /**
     * Get the index2.
     *
     * @return the index2
     */
    public String getIndex2() {
      return index2;
    }

    /**
     * Get the read number
     *
     * @return the read number
     */
    public int getReadNumber() {
      return readNumber;
    }

    /**
     * Get the yield
     *
     * @return the yield
     */
    public long getYield() {
      return yield;
    }

    /**
     * Get the Q30 yield
     *
     * @return the Q30 yield
     */
    public long getYieldQ30() {
      return yieldQ30;
    }

    /**
     * Get the quality score sum.
     *
     * @return the quality score sum
     */
    public long getQualityScoreSum() {
      return qualityScoreSum;
    }

    /**
     * Get the mean quality score passing filter.
     *
     * @return the mean quality score passing filter.
     */
    public double getMeanQualityScorePF() {
      return meanQualityScorePF;
    }

    /**
     * Get the Q30 percent.
     *
     * @return the Q30 percent
     */
    public double getQ30Percent() {
      return q30Percent;
    }

    //
    // Constructor
    //

    private Entry(CSVRecord record) {

      this.lane = parseInt(record, "Lane", -1);
      this.sampleID = parseString(record, "SampleID", "");
      this.index = parseString(record, "index", "");
      this.index2 = parseString(record, "index2", "");
      this.readNumber = parseInt(record, "ReadNumber", -1);
      this.yield = parseLong(record, "Yield", -1);
      this.yieldQ30 = parseLong(record, "YieldQ30", -1);
      this.qualityScoreSum = parseLong(record, "QualityScoreSum", -1);
      this.meanQualityScorePF = parseDouble(record, "Mean Quality Score (PF)", Double.NaN);
      this.q30Percent = parseDouble(record, "% Q30", Double.NaN);
    }
  }

  public List<Entry> entries() {

    return Collections.unmodifiableList(this.entries);
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   *
   * @param file input file
   * @throws IOException if an error occurs while reading the input file
   */
  public QualityMetrics(File file) throws IOException {

    this(new FileInputStream(file));
  }

  /**
   * Public constructor.
   *
   * @param in input stream
   * @throws IOException if an error occurs while reading the input file
   */
  public QualityMetrics(InputStream in) throws IOException {

    Objects.requireNonNull(in);

    try (Reader reader = new InputStreamReader(in)) {

      for (CSVRecord record :
          CSVFormat.RFC4180
              .builder()
              .setIgnoreEmptyLines(true)
              .setHeader()
              .setSkipHeaderRecord(true)
              .setSkipHeaderRecord(true)
              .build()
              .parse(reader)) {

        this.entries.add(new Entry(record));
      }
    }
  }
}
