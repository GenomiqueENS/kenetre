package fr.ens.biologie.genomique.kenetre.bio.io;

import static fr.ens.biologie.genomique.kenetre.bio.io.BEDWriter.DEFAULT_FORMAT;
import static fr.ens.biologie.genomique.kenetre.bio.io.BioCharsets.BED_CHARSET;
import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.bio.BEDEntry;
import fr.ens.biologie.genomique.kenetre.bio.BEDEntry.DefaultBEDEntryComparator;
import fr.ens.biologie.genomique.kenetre.io.FileUtils;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This class define a Sorted BED writer.
 *
 * @since 0.33
 * @author Laurent Jourdren
 */
public class SortedBEDWriter implements Closeable {

  private static final int CHUNK_SIZE = 1_000_000;
  private List<BEDEntry> chunk = new ArrayList<>(CHUNK_SIZE);
  private List<File> tempFiles = new ArrayList<>();

  private final int format;
  private final Writer writer;
  private File temporaryDirectory;

  //
  // Internal classes
  //

  private static class BEDLine {
    private final BEDEntry line;
    private final BEDReader reader;

    public BEDLine(BEDEntry line, BEDReader reader) {
      this.line = line;
      this.reader = reader;
    }
  }

  private static class BEDLineComparator implements Comparator<BEDLine> {

    private static final Comparator<BEDEntry> comparator = new DefaultBEDEntryComparator();

    @Override
    public int compare(BEDLine o1, BEDLine o2) {
      return comparator.compare(o1.line, o2.line);
    }
  }

  //
  // Public methods
  //

  /**
   * Set the temporary directory
   *
   * @param temporaryDirectory
   * @throws IOException if the temporary directory does not exists
   */
  public void setTemporaryDirectory(File temporaryDirectory) throws IOException {

    requireNonNull(temporaryDirectory);

    if (!Files.isDirectory(temporaryDirectory.toPath())) {
      throw new IOException("Temporary directory does not exists: " + temporaryDirectory);
    }

    this.temporaryDirectory = temporaryDirectory;
  }

  /**
   * Write the current entry.
   *
   * @param entry the entry to write
   * @throws IOException if an error occurs while writing data
   */
  public void write(final BEDEntry entry) throws IOException {

    if (entry == null) {
      return;
    }

    this.chunk.add(entry);
    if (this.chunk.size() == CHUNK_SIZE) {
      createSortedTempFile();
    }
  }

  @Override
  public void close() throws IOException {

    // Save and sort the remaining data
    createSortedTempFile();

    // Merge sorted files
    mergeSortedFiles();
  }

  //
  // Internal methods
  //

  /**
   * Save a sorted chunk of the data to write in a temporary directory.
   *
   * @param chunk chunk to sort and write
   * @param format format of the BED file
   * @return the path of temporary created file
   * @throws IOException if an error occurs while writing the temporary file
   */
  private void createSortedTempFile() throws IOException {

    // In memory sort
    Collections.sort(this.chunk);

    // Save sorted in memory
    File tempFile = File.createTempFile("sorted_", ".bed.tmp", temporaryDirectory);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile, BED_CHARSET))) {
      for (BEDEntry entry : chunk) {
        writer.write(entry.toBED(this.format) + '\n');
      }
    }

    this.chunk.clear();
    this.tempFiles.add(tempFile);
  }

  /**
   * Merge temporary files
   *
   * @param tempFiles temporary file to merge
   * @param writer writer for the merger
   * @param format format of the BED file
   * @throws IOException if an error occurs while writing the output file
   */
  private void mergeSortedFiles() throws IOException {

    PriorityQueue<BEDLine> pq = new PriorityQueue<>(new BEDLineComparator());

    List<BEDReader> readers = new ArrayList<>();

    for (File tempFile : this.tempFiles) {

      BEDReader reader = new BEDReader(tempFile);

      readers.add(reader);
      if (reader.hasNext()) {
        pq.add(new BEDLine(reader.next(), reader));
      }
    }

    try (Writer w = this.writer) {
      while (!pq.isEmpty()) {
        BEDLine smallest = pq.poll();
        w.write(smallest.line.toBED(format) + '\n');

        BEDReader reader = smallest.reader;
        if (reader.hasNext()) {
          pq.add(new BEDLine(reader.next(), smallest.reader));
        } else {
          smallest.reader.close();
        }
      }
    }

    // Close readers
    for (BEDReader reader : readers) {
      reader.close();
    }

    // Remove temporary files
    for (File tempFile : this.tempFiles) {
      tempFile.delete();
    }
  }

  //
  // Constructors
  //

  /**
   * Public constructor.
   *
   * @param writer Writer to use
   * @param format bed format as a number
   */
  public SortedBEDWriter(final Writer writer, final int format) {

    if (writer == null) {
      throw new NullPointerException("The writer is null.");
    }

    // Check the number of BED fields
    BEDEntry.checkBEDFieldCount(format);

    this.writer = writer;
    this.format = format;
  }

  /**
   * Public constructor.
   *
   * @param os OutputStream to use
   * @param format bed format as a number
   */
  public SortedBEDWriter(final OutputStream os, final int format) {

    // Check the number of BED fields
    BEDEntry.checkBEDFieldCount(format);

    this.writer = FileUtils.createFastBufferedWriter(os, BED_CHARSET);
    this.format = format;
  }

  /**
   * Public constructor.
   *
   * @param outputFile file to use
   * @param format bed format as a number
   * @throws IOException if an error occurs while creating the file
   */
  public SortedBEDWriter(final File outputFile, final int format) throws IOException {

    // Check the number of BED fields
    BEDEntry.checkBEDFieldCount(format);

    this.writer = FileUtils.createFastBufferedWriter(outputFile, BED_CHARSET);
    this.format = format;
  }

  /**
   * Public constructor.
   *
   * @param outputFilename name of the file to use
   * @param format bed format as a number
   * @throws IOException if an error occurs while creating the file
   */
  public SortedBEDWriter(final String outputFilename, final int format) throws IOException {

    // Check the number of BED fields
    BEDEntry.checkBEDFieldCount(format);

    this.writer = FileUtils.createFastBufferedWriter(outputFilename, BED_CHARSET);
    this.format = format;
  }

  /**
   * Public constructor.
   *
   * @param writer Writer to use
   */
  public SortedBEDWriter(final Writer writer) {

    this(writer, DEFAULT_FORMAT);
  }

  /**
   * Public constructor.
   *
   * @param os OutputStream to use
   */
  public SortedBEDWriter(final OutputStream os) {

    this(os, DEFAULT_FORMAT);
  }

  /**
   * Public constructor.
   *
   * @param outputFile file to use
   * @throws IOException if an error occurs while creating the file
   */
  public SortedBEDWriter(final File outputFile) throws IOException {

    this(outputFile, DEFAULT_FORMAT);
  }

  /**
   * Public constructor.
   *
   * @param outputFilename name of the file to use
   * @throws IOException if an error occurs while creating the file
   */
  public SortedBEDWriter(final String outputFilename) throws IOException {

    this(outputFilename, DEFAULT_FORMAT);
  }
}
