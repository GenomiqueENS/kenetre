package fr.ens.biologie.genomique.kenetre.bin.action;

import static org.apache.commons.cli.Option.builder;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.bio.BadBioEntryException;
import fr.ens.biologie.genomique.kenetre.bio.FastqFormat;
import fr.ens.biologie.genomique.kenetre.bio.ReadSequence;
import fr.ens.biologie.genomique.kenetre.bio.io.FastqReader;
import fr.ens.biologie.genomique.kenetre.bio.io.FastqWriter;
import fr.ens.biologie.genomique.kenetre.bio.readfilter.MultiReadFilterBuilder;
import fr.ens.biologie.genomique.kenetre.bio.readfilter.ReadFilter;
import fr.ens.biologie.genomique.kenetre.io.CompressionType;
import fr.ens.biologie.genomique.kenetre.util.LocalReporter;
import fr.ens.biologie.genomique.kenetre.util.Reporter;
import java.io.File;
import java.io.IOException;
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
 * This program allow to filter FASTQ files.
 *
 * @author Laurent Jourdren
 * @since 0.29
 */
public class FilterReadsAction implements Action {

  private static final String COUNTER_GROUP = "reads_filtering";

  private File inputFile;
  private File inputFile1;
  private File inputFile2;
  private File outputFile;
  private File outputFile1;
  private File outputFile2;
  private FastqFormat format = FastqFormat.FASTQ_SANGER;
  private boolean printStats = true;

  @Override
  public String getName() {

    return "filterreads";
  }

  @Override
  public String getDescription() {

    return "This program filters reads";
  }

  @Override
  public boolean isHidden() {

    return false;
  }

  @Override
  public void action(List<String> arguments) {

    try {

      // Parse command line and create filter
      MultiReadFilterBuilder filterBuilder = new MultiReadFilterBuilder();
      filterBuilder.addParameters(parseOptions(arguments));
      ReadFilter filter = filterBuilder.getReadFilter();

      Reporter reporter = new LocalReporter();

      if (this.inputFile != null) {
        filterFile(this.inputFile, this.outputFile, reporter, filter, this.format);
      } else {
        filterFile(
            this.inputFile1,
            this.inputFile2,
            this.outputFile1,
            this.outputFile2,
            reporter,
            filter,
            this.format);
      }

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
        builder("i1")
            .longOpt("input1")
            .hasArg()
            .argName("file")
            .desc("paired-end input 1")
            .build());

    options.addOption(
        builder("i2")
            .longOpt("input2")
            .hasArg()
            .argName("file")
            .desc("paired-end input 2")
            .build());

    options.addOption(
        builder("o").longOpt("output").hasArg().argName("file").desc("single read output").build());

    options.addOption(
        builder("o1")
            .longOpt("output1")
            .hasArg()
            .argName("file")
            .desc("paired-end output 1")
            .build());

    options.addOption(
        builder("o2")
            .longOpt("output2")
            .hasArg()
            .argName("file")
            .desc("paired-end output 2")
            .build());

    options.addOption(
        builder("f")
            .longOpt("fastq-format")
            .hasArg()
            .argName("format")
            .desc(
                "FASTQ format (sanger,  solexa, fastq-illumina-1.3, fastq-illumina-1.5), default: sanger")
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

      if (line.hasOption("i1")) {
        this.inputFile1 = new File(line.getOptionValue("input1"));
      }

      if (line.hasOption("i2")) {
        this.inputFile1 = new File(line.getOptionValue("input2"));
      }

      if (line.hasOption("o")) {
        this.outputFile = new File(line.getOptionValue("output"));
      }

      if (line.hasOption("o1")) {
        this.inputFile1 = new File(line.getOptionValue("output1"));
      }

      if (line.hasOption("o2")) {
        this.inputFile1 = new File(line.getOptionValue("output2"));
      }

      if (line.hasOption("f")) {
        this.format = FastqFormat.getFormatFromName(line.getOptionValue("fastq-format"));

        if (this.format == null) {
          error("Error: Unknown FASTQ format: " + line.getOptionValue("fastq-format"));
        }
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

    // Check paired-end mode
    if (this.inputFile1 != null
        || this.inputFile2 != null
        || outputFile1 != null
        || outputFile2 != null) {

      if (this.inputFile1 == null) {
        error("Error: input file 1 is not defined");
      }

      if (this.inputFile2 == null) {
        error("Error: input file 2 is not defined");
      }

      if (this.outputFile1 == null) {
        error("Error: output file 1 is not defined");
      }

      if (this.outputFile2 == null) {
        error("Error: output file 2 is not defined");
      }
    } else {

      if (this.inputFile == null) {
        error("Error: input file  is not defined");
      }

      if (this.outputFile == null) {
        error("Error: output file is not defined");
      }
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
   * Filter a file in single end mode.
   *
   * @param inFile input file
   * @param outFile output file
   * @param reporter reporter to use
   * @param filter reads filter to use
   * @param fastqFormat FastqFormat
   * @throws IOException if an error occurs while filtering data
   */
  private static void filterFile(
      final File inFile,
      final File outFile,
      final Reporter reporter,
      final ReadFilter filter,
      final FastqFormat fastqFormat)
      throws IOException {

    try (FastqReader reader = new FastqReader(CompressionType.open(inFile));
        FastqWriter writer = new FastqWriter(CompressionType.create(outFile))) {
      for (final ReadSequence read : reader) {

        // Set Fastq format
        read.setFastqFormat(fastqFormat);

        reporter.incrCounter(COUNTER_GROUP, "input raw reads", 1);

        if (filter.accept(read)) {

          writer.write(read);
          reporter.incrCounter(COUNTER_GROUP, "output accepted reads", 1);
        } else {
          reporter.incrCounter(COUNTER_GROUP, "reads rejected by filters", 1);
        }
      }
      reader.throwException();

    } catch (BadBioEntryException e) {

      throw new IOException(
          "Invalid Fastq format: "
              + e.getMessage()
              + " File: "
              + inFile
              + " Entry: "
              + e.getEntry());
    }
  }

  /**
   * Filter a file in pair-end mode.
   *
   * @param inFile1 first input file
   * @param inFile2 second input file
   * @param outFile1 first output file
   * @param outFile2 second output file
   * @param reporter reporter to use
   * @param filter reads filter to use
   * @param fastqFormat FastqFormat
   * @throws IOException if an error occurs while filtering data
   */
  private static void filterFile(
      final File inFile1,
      final File inFile2,
      final File outFile1,
      final File outFile2,
      final Reporter reporter,
      final ReadFilter filter,
      final FastqFormat fastqFormat)
      throws IOException {

    try (FastqReader reader2 = new FastqReader(CompressionType.open(inFile2));
        FastqWriter writer1 = new FastqWriter(CompressionType.create(outFile1));
        FastqWriter writer2 = new FastqWriter(CompressionType.create(outFile2));
        FastqReader reader1 = new FastqReader(CompressionType.open(inFile1))) {
      for (final ReadSequence read1 : reader1) {

        // Test if the second read exists
        if (!reader2.hasNext()) {
          reader2.throwException();
          throw new IOException(
              "Unexcepted end of the second read file. "
                  + inFile1.getName()
                  + " and "
                  + inFile2.getName()
                  + " must have the same number of entries/lines.");
        }

        // Get the second read
        final ReadSequence read2 = reader2.next();

        // Set fastq format
        read1.setFastqFormat(fastqFormat);
        read2.setFastqFormat(fastqFormat);

        reporter.incrCounter(COUNTER_GROUP, "input raw reads", 1);

        if (filter.accept(read1, read2)) {
          writer1.write(read1);
          writer2.write(read2);
          reporter.incrCounter(COUNTER_GROUP, "output accepted reads", 1);
        } else {
          reporter.incrCounter(COUNTER_GROUP, "reads rejected by filters", 1);
        }
      }
      reader1.throwException();
      reader2.throwException();

      if (reader2.hasNext()) {
        throw new IOException(
            "Unexcepted end of the first read file. "
                + inFile1.getName()
                + " and "
                + inFile2.getName()
                + " must have the same number of entries/lines.");
      }

    } catch (BadBioEntryException e) {

      throw new IOException(
          "Invalid Fastq format: "
              + e.getMessage()
              + " File 1: "
              + inFile1
              + " File2:"
              + inFile2
              + " Entry: "
              + e.getEntry());
    }
  }
}
