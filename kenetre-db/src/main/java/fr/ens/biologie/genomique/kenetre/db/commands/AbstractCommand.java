package fr.ens.biologie.genomique.kenetre.db.commands;

import static java.util.Objects.requireNonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.FgApiClient;
import fr.ens.biologie.genomique.kenetre.db.GenomiqueEnsApiClient;
import fr.ens.biologie.genomique.kenetre.db.ProjectSubmissionBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.*;

/**
 * Abstract base class for command line actions. Provides common options parsing and utilities for
 * child classes.
 *
 * @author Laurent Jourdren
 * @since 0.41
 */
public abstract class AbstractCommand {

  private static final int DEFAULT_SINCE_YEAR = 2025;

  private final String actionName;
  protected final GenomiqueEnsApiClient gensApiClient;
  protected final FgApiClient fgApiClient;
  protected final ProjectSubmissionBuilder submissionBuilder;
  protected final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  protected boolean quietMode = false;
  protected boolean force = false;
  protected boolean debug = false;
  protected int sinceYear = DEFAULT_SINCE_YEAR;

  // Nomemclatures
  protected final Map<String, String> applicationFgMap;
  protected final Map<String, String> instrumentFgMap;
  protected final Map<String, String> flowcellFgMap;
  protected final Map<String, String> speciesFgDict;

  //
  // Parsing methods
  //

  /**
   * Create default options for command line
   *
   * @return an Options object
   */
  private Options defaultOptions() {

    // Create Options object
    var options = new Options();

    options.addOption("h", "help", false, "this help");
    options.addOption("q", "quiet", false, "quiet mode");
    options.addOption("f", "force", false, "force execution");
    options.addOption("d", "debug", false, "debug mode");
    options.addOption("s", "since-year", true, "since year");

    return options;
  }

  protected abstract String syntax();

  protected Options additionalOptions(Options options) {
    return options;
  }
  ;

  protected void parseAdditionalOptions(CommandLine line) {}
  ;

  protected abstract void internalExecute(List<String> arguments) throws KenetreException;

  public void execute(List<String> arguments) throws KenetreException {

    var options = additionalOptions(defaultOptions());
    var parser = new DefaultParser();

    try {

      // parse the command line arguments
      final CommandLine line = parser.parse(options, arguments.toArray(new String[0]), true);

      // Help option
      if (line.hasOption("help")) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(this.actionName + " " + syntax(), options);
        System.exit(0);
      }

      // Quiet option
      if (line.hasOption("quiet")) {
        this.quietMode = true;
      }

      // Force option
      if (line.hasOption("force")) {
        this.force = true;
      }

      // Debug option
      if (line.hasOption("debug")) {
        this.debug = true;
      }

      // Year option
      if (line.hasOption("since-year")) {
        try {
          this.sinceYear = Integer.parseInt(line.getOptionValue("since-year"));
        } catch (NumberFormatException e) {
          throw new KenetreException(
              "Invalid value for --since-year option: " + line.getOptionValue("since-year"), e);
        }
      }

      // Parse additional options of child classes
      parseAdditionalOptions(line);

      // Execute
      internalExecute(line.getArgList());

    } catch (ParseException e) {
      throw new KenetreException(
          "Error while parsing command line arguments: " + e.getMessage(), e);
    }
  }

  //
  // Constructor
  //

  protected AbstractCommand(
      String actionName, Map<String, String> conf, boolean databaseConnection, boolean fgConnection)
      throws KenetreException {

    requireNonNull(actionName);
    requireNonNull(conf);

    // Set action name
    this.actionName = actionName;

    // Initialize GenomiqueENS connection
    this.gensApiClient = new GenomiqueEnsApiClient(conf, debug);

    // Create FG API client and retrieve nomemclatures
    if (fgConnection) {

      this.fgApiClient = new FgApiClient(conf, true, debug);

      try {
        this.applicationFgMap =
            this.fgApiClient.getNomenclature(
                "applications", "nom_application", "cle_application_fg");
        this.instrumentFgMap =
            this.fgApiClient.getNomenclature(
                "typeinstruments", "nom_instrument", "cle_type_instrument_fg");
        this.flowcellFgMap =
            this.fgApiClient.getNomenclature(
                "flowcells",
                "descriptif",
                "cle_type_flowcell_fg",
                "nom_instrument",
                List.of("NextSeq 2000", "MinION", "PromethION P2 solo"));
        this.speciesFgDict =
            this.fgApiClient.getNomenclature("especes", "nom_espece", "cle_espece_fg");

        this.submissionBuilder =
            new ProjectSubmissionBuilder(
                applicationFgMap, instrumentFgMap, flowcellFgMap, speciesFgDict);

      } catch (IOException e) {
        throw new KenetreException("Unable to retrieve nomenclature", e);
      }

      ProjectSubmissionBuilder builder =
          new ProjectSubmissionBuilder(
              applicationFgMap, instrumentFgMap, flowcellFgMap, speciesFgDict);
    } else {
      this.fgApiClient = null;
      this.applicationFgMap = null;
      this.instrumentFgMap = null;
      this.flowcellFgMap = null;
      this.speciesFgDict = null;
      this.submissionBuilder = null;
    }
  }
}
