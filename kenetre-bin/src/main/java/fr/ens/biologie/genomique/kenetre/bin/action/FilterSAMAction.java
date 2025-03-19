package fr.ens.biologie.genomique.kenetre.bin.action;

import static org.apache.commons.cli.Option.builder;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.bio.SAMComparator;
import fr.ens.biologie.genomique.kenetre.bio.alignmentfilter.MultiReadAlignmentFilterBuilder;
import fr.ens.biologie.genomique.kenetre.bio.alignmentfilter.ReadAlignmentFilter;
import fr.ens.biologie.genomique.kenetre.bio.alignmentfilter.ReadAlignmentFilterBuffer;
import fr.ens.biologie.genomique.kenetre.util.LocalReporter;
import fr.ens.biologie.genomique.kenetre.util.Reporter;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMFormatException;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamInputResource;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This program allow to filter SAM files.
 *
 * @author Laurent Jourdren
 * @since 0.29
 */
public class FilterSAMAction implements Action {

  private static final String COUNTER_GROUP = "sam_filtering";

  private File inputFile;
  private File outputFile;
  private File tmpDir = new File(System.getProperty("java.io.tmpdir"));
  private boolean printStats = true;

  @Override
  public String getName() {

    return "filtersam";
  }

  @Override
  public String getDescription() {

    return "Filter SAM file";
  }

  @Override
  public boolean isHidden() {

    return false;
  }

  @Override
  public void action(List<String> arguments) {

    try {

      // Parse command line and create filter
      MultiReadAlignmentFilterBuilder filterBuilder = new MultiReadAlignmentFilterBuilder();
      filterBuilder.addParameters(parseOptions(arguments));
      ReadAlignmentFilter filter = filterBuilder.getAlignmentFilter();

      Reporter reporter = new LocalReporter();

      filterFile(this.inputFile, this.outputFile, reporter, filter, this.tmpDir);

      if (this.printStats) {
        System.err.println(reporter);
      }

    } catch (KenetreException | IOException e) {
      error("Error: " + e.getMessage());
    }
  }

  //
  // Command line parsing
  //

  /**
   * Create options for command line
   *
   * @return an Options object
   */
  @SuppressWarnings("static-access")
  private static Options makeOptions() {

    // create Options object
    final Options options = new Options();

    options.addOption(
        builder("i").longOpt("input").hasArg().argName("file").desc("single read input").build());

    options.addOption(
        builder("o").longOpt("output").hasArg().argName("file").desc("single read output").build());

    options.addOption(
        builder("T")
            .longOpt("tmpdir")
            .hasArg()
            .argName("dire")
            .desc("temporary directory")
            .build());

    options.addOption("s", "stdin", false, "stdin input");
    options.addOption("t", "stdout", false, "stdout output");
    options.addOption("n", "no-stats", false, "stdout output");

    // Help option
    options.addOption("h", "help", false, "display this help");

    return options;
  }

  private Map<String, String> parseOptions(List<String> arguments) {

    final Options options = makeOptions();
    final CommandLineParser parser = new DefaultParser();

    Map<String, String> result = new LinkedHashMap<>();

    try {

      // parse the command line arguments
      final CommandLine line = parser.parse(options, arguments.toArray(new String[0]), true);

      // Help option
      if (line.hasOption("help")) {
        help(options);
      }

      if (line.hasOption("s")) {
        this.inputFile = new File("/dev/stdin");
      }

      if (line.hasOption("t")) {
        this.outputFile = new File("/dev/stdout");
      }

      if (line.hasOption("n")) {
        this.printStats = false;
      }

      if (line.hasOption("i")) {
        this.inputFile = new File(line.getOptionValue("input"));
      }

      if (line.hasOption("o")) {
        this.outputFile = new File(line.getOptionValue("output"));
      }

      if (line.hasOption("T")) {
        this.tmpDir = new File(line.getOptionValue("tmpdir"));
      }

      for (String arg : line.getArgList()) {

        int pos = arg.indexOf('=');
        if (pos == -1) {
          continue;
        }

        result.put(arg.substring(0, pos), arg.substring(pos + 1));
      }

    } catch (ParseException e) {
      error("Error while parsing command line arguments: " + e.getMessage());
    }

    if (this.inputFile == null) {
      error("Error: input file  is not defined");
    }

    if (this.outputFile == null) {
      error("Error: output file is not defined");
    }

    return result;
  }

  /**
   * Show command line help.
   *
   * @param options Options of the software
   */
  private static void help(final Options options) {

    // Show help message
    final HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("filterreads [options] parameters", options);

    System.exit(0);
  }

  private static void error(String msg) {

    System.err.println(msg);
    System.exit(1);
  }

  //
  // Filtering code
  //

  /**
   * Filter a file in single-end mode or paired-end mode.
   *
   * @param inFile input file
   * @param outFile output file
   * @param reporter reporter to use
   * @param filter alignments filter to use
   * @param tmpDir temporary directory
   * @throws IOException if an error occurs while filtering data
   */
  private static void filterFile(
      final File inFile,
      final File outFile,
      final Reporter reporter,
      final ReadAlignmentFilter filter,
      final File tmpDir)
      throws IOException {

    final List<SAMRecord> records = new ArrayList<>();
    int counterInput = 0;
    int counterOutput = 0;
    int counterInvalid = 0;
    boolean pairedEnd = false;

    // Creation of a buffer object to store alignments with the same read name
    final ReadAlignmentFilterBuffer rafb = new ReadAlignmentFilterBuffer(filter);

    // Get reader
    final SamReader inputSam = SamReaderFactory.makeDefault().open(SamInputResource.of(inFile));

    // Get Writer
    final SAMFileWriter outputSam =
        new SAMFileWriterFactory()
            .setTempDirectory(tmpDir)
            .makeSAMWriter(inputSam.getFileHeader(), false, outFile);

    final SAMRecordIterator it = inputSam.iterator();

    while (it.hasNext()) {

      final SAMRecord samRecord;

      // Check if SAM entry is correct
      try {
        samRecord = it.next();

      } catch (SAMFormatException e) {
        counterInvalid++;
        continue;
      }

      // single-end or paired-end mode ?
      if (counterInput == 0) {
        if (samRecord.getReadPairedFlag()) {
          pairedEnd = true;
        }
      }

      counterInput++;

      // storage and filtering of all the alignments of a read in the list
      // "records"
      if (!rafb.addAlignment(samRecord)) {

        records.clear();
        records.addAll(rafb.getFilteredAlignments());

        // sort alignments of the current read
        records.sort(new SAMComparator());

        // writing records
        for (SAMRecord r : records) {
          outputSam.addAlignment(r);
          counterOutput++;
        }

        rafb.addAlignment(samRecord);
      }
    }

    // treatment of the last record
    records.clear();
    records.addAll(rafb.getFilteredAlignments());

    // sort alignments of the last read
    records.sort(new SAMComparator());

    // writing records
    for (SAMRecord r : records) {
      outputSam.addAlignment(r);
      counterOutput++;
    }

    // paired-end mode
    if (pairedEnd) {
      int nbInput = counterInput / 2;
      int nbOutput = counterOutput / 2;
      reporter.incrCounter(COUNTER_GROUP, "input alignments", nbInput);
      reporter.incrCounter(COUNTER_GROUP, "output filtered alignments", nbOutput);
      reporter.incrCounter(COUNTER_GROUP, "alignments in invalid sam format", counterInvalid / 2);
      reporter.incrCounter(COUNTER_GROUP, "alignments rejected by filters", nbInput - nbOutput);
    }

    // single-end mode
    else {
      reporter.incrCounter(COUNTER_GROUP, "input alignments", counterInput);
      reporter.incrCounter(COUNTER_GROUP, "output filtered alignments", counterOutput);
      reporter.incrCounter(COUNTER_GROUP, "alignments in invalid sam format", counterInvalid);
      reporter.incrCounter(
          COUNTER_GROUP, "alignments rejected by filters", counterInput - counterOutput);
    }

    // Close files
    inputSam.close();
    outputSam.close();
  }
}
