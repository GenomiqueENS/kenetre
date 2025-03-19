package fr.ens.biologie.genomique.kenetre.illumina;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 * This class allow to parse the DemultiplexingStats csv file.
 *
 * @author Laurent Jourdren
 * @since 0.3
 */
public class DemultiplexStats {

  private List<Entry> entries = new ArrayList<>();

  public static class Entry {

    private final int lane;
    private final String sampleID;
    private final String index;
    private final int readCount;
    private final int perfectIndexReadCount;
    private final int oneMismatchIndexReadCount;
    private final long q30BaseCount;
    private final double meanPFQualityScore;

    private final int twoMismatchIndexReadCount;
    private final double readsPercent;
    private final double perfectIndexreadsPercent;
    private final double oneMismatchIndexReadPercent;
    private final double twoMismatchIndexReadPercent;

    //
    // Constructor
    //

    /**
     * Get the lane.
     *
     * @return the lane
     */
    public int getLane() {
      return lane;
    }

    /**
     * Get the sample id.
     *
     * @return the sampleID
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
     * Get the read count.
     *
     * @return the read count
     */
    public int getReadCount() {
      return readCount;
    }

    /**
     * Get the perfect index read count.
     *
     * @return the perfect index read count
     */
    public int getPerfectIndexReadCount() {
      return perfectIndexReadCount;
    }

    /**
     * Get the one mismatch index read count.
     *
     * @return the one mismatch index read count
     */
    public int getOneMismatchIndexReadCount() {
      return oneMismatchIndexReadCount;
    }

    /**
     * Get the Q30 base count.
     *
     * @return the Q30 base count
     */
    public long getQ30BaseCount() {
      return q30BaseCount;
    }

    /**
     * Get the mean passing filter quality score.
     *
     * @return the mean passing filter quality score
     */
    public double getMeanPFQualityScore() {
      return meanPFQualityScore;
    }

    /**
     * Get the two mismatch index read count.
     *
     * @return the two mismatch index read count
     */
    public int getTwoMismatchIndexReadCount() {
      return twoMismatchIndexReadCount;
    }

    /**
     * Get the read percent.
     *
     * @return the read percent
     */
    public double getReadsPercent() {
      return readsPercent;
    }

    /**
     * Get the perfect index reads percent.
     *
     * @return the perfect index reads percent
     */
    public double getPerfectIndexreadsPercent() {
      return perfectIndexreadsPercent;
    }

    /**
     * Get the one mismatch index read percent.
     *
     * @return the one mismatch index read percent
     */
    public double getOneMismatchIndexReadPercent() {
      return oneMismatchIndexReadPercent;
    }

    /**
     * Get the two mismatch index read percent.
     *
     * @return the two mismatch index read percent
     */
    public double getTwoMismatchIndexReadPercent() {
      return twoMismatchIndexReadPercent;
    }

    @Override
    public String toString() {
      return "Entry [lane="
          + lane
          + ", sampleID="
          + sampleID
          + ", index="
          + index
          + ", readCount="
          + readCount
          + ", perfectIndexReadCount="
          + perfectIndexReadCount
          + ", oneMismatchIndexReadCount="
          + oneMismatchIndexReadCount
          + ", q30BaseCount="
          + q30BaseCount
          + ", meanPFQualityScore="
          + meanPFQualityScore
          + ", twoMismatchIndexReadCount="
          + twoMismatchIndexReadCount
          + ", readsPercent="
          + readsPercent
          + ", perfectIndexreadsPercent="
          + perfectIndexreadsPercent
          + ", oneMismatchIndexReadPercent="
          + oneMismatchIndexReadPercent
          + ", twoMismatchIndexReadPercent="
          + twoMismatchIndexReadPercent
          + "]";
    }

    //
    // Constructor
    //

    private Entry(CSVRecord record) {

      this.lane = CSVUtils.parseInt(record, "Lane", -1);
      this.sampleID = CSVUtils.parseString(record, "SampleID", "");
      this.index = CSVUtils.parseString(record, "Index", "");
      this.readCount = CSVUtils.parseInt(record, "# Reads", -1);
      this.perfectIndexReadCount = CSVUtils.parseInt(record, "# Perfect Index Reads", -1);
      this.oneMismatchIndexReadCount = CSVUtils.parseInt(record, "# One Mismatch Index Reads", -1);
      this.q30BaseCount = CSVUtils.parseLong(record, "# of >= Q30 Bases (PF)", -1);
      this.meanPFQualityScore = CSVUtils.parseDouble(record, "Mean Quality Score (PF)", Double.NaN);

      this.twoMismatchIndexReadCount = CSVUtils.parseInt(record, "# Two Mismatch Index Reads", -1);
      this.readsPercent = CSVUtils.parseDouble(record, "% Reads", Double.NaN);
      this.perfectIndexreadsPercent =
          CSVUtils.parseDouble(record, "% Perfect Index Reads", Double.NaN);
      this.oneMismatchIndexReadPercent =
          CSVUtils.parseDouble(record, "% One Mismatch Index Reads", Double.NaN);
      this.twoMismatchIndexReadPercent =
          CSVUtils.parseDouble(record, "% Two Mismatch Index Reads", Double.NaN);
    }
  }

  /**
   * Get the entries of the file.
   *
   * @return a list with the entries of the file
   */
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
  public DemultiplexStats(Path file) throws IOException {

    this(new FileInputStream(file.toFile()));
  }

  /**
   * Public constructor.
   *
   * @param file input file
   * @throws IOException if an error occurs while reading the input file
   */
  public DemultiplexStats(File file) throws IOException {

    this(new FileInputStream(file));
  }

  /**
   * Public constructor.
   *
   * @param in input stream
   * @throws IOException if an error occurs while reading the input file
   */
  public DemultiplexStats(InputStream in) throws IOException {

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
