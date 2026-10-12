package fr.ens.biologie.genomique.kenetre.db.commands;

import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.GenomiqueEnsApiClient;
import fr.ens.biologie.genomique.kenetre.db.madbot.MadbotApiClient;
import fr.ens.biologie.genomique.kenetre.db.model.LibraryInfo;
import fr.ens.biologie.genomique.kenetre.db.model.ProjectInfo;
import fr.ens.biologie.genomique.kenetre.db.model.RunInfo;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public class SubmitMadbotCommand extends AbstractCommand {

  private static String MADBOT_WORKSPACE = "TEST-API";
  private static String ENA_PLUGIN_NAME = "ENA";

  private record Sample(
      String sampleName,
      String sampleDescription,
      LibraryInfo library,
      List<List<String>> fastqPaths) {}

  private record Submission(
      String workspace,
      boolean removeExistingWorkspace,
      String acronym,
      String labName,
      List<String> samples) {}

  private Submission submission;

  //
  // AbstractCommand methods
  //

  @Override
  protected String syntax() {
    return "<project_name>...";
  }

  protected Options additionalOptions(Options options) {

    options.addOption("w", "workspace", true, "workspace name (default: " + MADBOT_WORKSPACE + ")");
    options.addOption(
        "r", "remove-existing-workspace", false, "remove existing workspace if it exists");
    options.addOption(
        "a", "submission-acronym", true, "submission acronym (default: project acronym)");
    options.addOption("l", "lab-name", true, "laboratory name (default: project's lab)");
    options.addOption(
        "m",
        "samples",
        true,
        "comma-separated list of sample names to submit (default: all samples of the project(s))");

    return options;
  }

  protected void parseAdditionalOptions(CommandLine line) {

    this.submission = new Submission(MADBOT_WORKSPACE, false, null, null, new ArrayList<>());

    this.submission =
        new Submission(
            line.hasOption("w") ? line.getOptionValue("w") : this.submission.workspace,
            line.hasOption("r"),
            line.hasOption("a") ? line.getOptionValue("a") : this.submission.acronym,
            line.hasOption("l") ? line.getOptionValue("l") : this.submission.labName,
            line.hasOption("m")
                ? Arrays.asList(line.getOptionValue("m").split(","))
                : this.submission.samples);
  }

  @Override
  protected void internalExecute(List<String> arguments) throws KenetreException {

    if (arguments.size() < 1) {
      System.err.println(
          "ERROR: Invalid number of arguments. Expected at least 1, got " + arguments.size());
      System.exit(1);
    }

    System.out.println(this.submission);
    System.out.println(arguments);

    submit(arguments);
  }

  //
  // Submission methods
  //

  private void submit(List<String> projectNames) throws KenetreException {

    if (projectNames.isEmpty()) {
      throw new KenetreException("No project name provided");
    }

    ProjectInfo firstProject = null;
    List<Sample> samples = new ArrayList<>();
    var submissionAcronym = this.submission.acronym();
    var submissionLabName = this.submission.labName();

    try {

      //  Get the ENA plugin slug
      var enaPluginslug = fetchMadbotPluginSlug(this.madbotApiClient, ENA_PLUGIN_NAME);

      // Create workspace if not exists
      var workspaceUuid =
          fetchMadBotWorkspaceUuid(
              super.madbotApiClient,
              this.submission.workspace(),
              this.submission.removeExistingWorkspace());

      // Create ssh connection if not exists
      UUID sshConnectionUuid = fetchMadbotSshConnectionUuid(super.madbotApiClient, workspaceUuid);

      System.out.println(
          "* Workspace \"" + this.submission.workspace() + "\" UUID: " + workspaceUuid);
      System.out.println("* SSH Connection UUID: " + sshConnectionUuid);

      for (var projectName : projectNames) {

        // Retrieve project and library information
        ProjectInfo project = super.gensApiClient.fetchProjectInfo(projectName);
        if (firstProject == null) {
          firstProject = project;
          submissionAcronym =
              submissionAcronym != null ? submissionAcronym : firstProject.acronym();
          submissionLabName =
              submissionLabName != null ? submissionLabName : firstProject.labName();
        }

        List<LibraryInfo> libraries = new ArrayList<>();
        for (String libraryBatchId : project.libraryBatchIds()) {
          libraries.add(super.gensApiClient.fetchLibraryBatch(libraryBatchId));
        }

        // Fetch samples for each library and run
        for (LibraryInfo library : libraries) {
          for (String run : library.runs()) {
            RunInfo runInfo = super.gensApiClient.fetchRunInfo(run, projectName);
            switch (runInfo.supplier()) {
              case "Illumina":
                samples.addAll(
                    filterSamplesByName(
                        fetchIlluminaSamples(
                            super.gensApiClient,
                            this.madbotApiClient,
                            projectName,
                            submissionAcronym,
                            run,
                            library),
                        this.submission.samples()));
                break;
              case "Oxford Nanopore Technologies":
                throw new IllegalArgumentException("Unsupported supplier: " + runInfo.supplier());

              default:
                throw new IllegalArgumentException("Unsupported supplier: " + runInfo.supplier());
            }
          }
        }
      }

      // Submit project to Madbot
      submitProjectToMadbot(
          this.madbotApiClient,
          sshConnectionUuid,
          enaPluginslug,
          workspaceUuid,
          submissionAcronym,
          submissionLabName,
          samples);

    } catch (IOException e) {
      throw new KenetreException(e);
    }
  }

  private static List<Sample> fetchIlluminaSamples(
      GenomiqueEnsApiClient gensApiClient,
      MadbotApiClient madbotApiClient,
      String projectName,
      String submissionAcronym,
      String runId,
      LibraryInfo library)
      throws IOException {

    List<Sample> samples = new ArrayList<>();
    Map<String, Sample> samplesById = new HashMap<>();

    // Define the base path on the SFTP server
    String basePath = madbotApiClient.getSftpBasePath();
    if (basePath == null) {
      basePath = "";
    } else if (!basePath.endsWith("/")) {
      basePath += "/";
    }

    for (GenomiqueEnsApiClient.IlluminaSample s :
        gensApiClient.fetchIlluminaSamples(runId, projectName)) {

      List<String> externalIds = new ArrayList<>();
      for (var p : s.fastqPaths()) {
        externalIds.add(basePath + submissionAcronym + '/' + runId + '/' + new File(p).getName());
      }

      Sample sample;
      if (samplesById.containsKey(s.name())) {
        sample = samplesById.get(s.name());
      } else {
        sample = new Sample(s.name(), s.description(), library, new ArrayList<>());
        samplesById.put(s.name(), sample);
      }
      sample.fastqPaths().add(externalIds);

      if (!samples.contains(sample)) {
        samples.add(sample);
      }
    }

    return samples;
  }

  private static void submitProjectToMadbot(
      MadbotApiClient madbot,
      UUID sshConnectionUuid,
      String enaPluginslug,
      UUID workspaceUuid,
      String acronym,
      String labName,
      List<Sample> samples)
      throws IOException {

    requireNonNull(acronym);
    requireNonNull(labName);

    // Get the ids of required metadata for projects
    var studyMetadataIds = madbot.getMetadataCategory(workspaceUuid, ENA_PLUGIN_NAME, "study");

    // Create project node
    var projectUuid = madbot.createProjectNode(workspaceUuid, acronym);
    System.out.println("* Create project \"" + acronym + "\" UUID: " + projectUuid);

    // Set known metadata values
    System.out.println("* Set project metadata");
    for (var e : Map.of("ena__study__center_name", labName).entrySet()) {
      System.out.println("\t- " + e.getKey() + ": " + e.getValue());
      madbot.setProjectMetadata(
          workspaceUuid, projectUuid, studyMetadataIds.get(e.getKey()).uuid(), e.getValue());
    }

    // Set additional project metadata with complex values
    System.out.println("\t- ena__study__additional_metadata");
    var additionalProjectMetadata =
        List.of(
            Map.of(
                "tag",
                "Gemomics core facility",
                "value",
                "GenomiqueENS, Institut de Biologie de l'ENS (IBENS), Département de biologie, "
                    + "École normale supérieure, CNRS, INSERM, Université PSL, 75005 Paris, France"),
            Map.of(
                "tag",
                "Gemomics core facility acknowledgements",
                "value",
                "The GenomiqueENS core facility was supported by the France Génomique"
                    + " national infrastructure, funded as part of the \"Investissements d'Avenir\" "
                    + "program managed by the Agence Nationale de la Recherche (contract ANR-10-INBS-0009)"));
    madbot.setProjectMetadata(
        workspaceUuid,
        projectUuid,
        studyMetadataIds.get("ena__study__additional_metadata").uuid(),
        additionalProjectMetadata);

    // Create project metadata with empty values
    for (Map.Entry<String, MadbotApiClient.MetadataFieldInfo> entry : studyMetadataIds.entrySet()) {
      System.out.println("\t- " + entry.getKey() + ": [empty]");
      if (entry.getValue().mandatory()) {
        madbot.addEmptyProjectMetadata(workspaceUuid, projectUuid, entry.getValue().uuid());
      }
    }

    // Add empty project description that is non-mandatory
    System.out.println("\t- ena__study__description: [empty]");
    madbot.addEmptyProjectMetadata(
        workspaceUuid, projectUuid, studyMetadataIds.get("ena__study__description").uuid());

    // Create libraries
    for (var s : samples) {
      submitLibrariesToMadbot(
          madbot,
          sshConnectionUuid,
          enaPluginslug,
          workspaceUuid,
          projectUuid,
          s.sampleName(),
          s.sampleDescription(),
          s.library(),
          s.fastqPaths());
    }
  }

  private static void submitLibrariesToMadbot(
      MadbotApiClient madbot,
      UUID sshConnectionUuid,
      String enaPluginslug,
      UUID workspaceUuid,
      UUID projectUuid,
      String sampleName,
      String sampleDescription,
      LibraryInfo library,
      List<List<String>> fastqPaths)
      throws IOException {

    requireNonNull(sampleName, "Sample name cannot be null");
    requireNonNull(sampleDescription, "Sample description cannot be null");
    requireNonNull(library, "library cannot be null");
    requireNonNull(fastqPaths, "FASTQ paths cannot be null");

    // Create sample
    var sampleUuid = madbot.createSample(workspaceUuid, projectUuid, sampleName, sampleDescription);
    System.out.println("* Create sample \"" + sampleName + "\" UUID: " + sampleUuid);

    // Create Datalinks
    createDataLink(madbot, workspaceUuid, projectUuid, sampleUuid, sshConnectionUuid, fastqPaths);

    // sample__erc000011 metadata
    var enaDefaultSampleMetadataIds =
        madbot.getMetadataGroup(workspaceUuid, enaPluginslug, "sample__erc000011");

    // Set the required metadata for sample to empty
    for (var e : enaDefaultSampleMetadataIds.entrySet()) {
      if (e.getValue().mandatory()) {
        System.out.println("\t- " + e.getKey() + ": " + e.getValue());
        madbot.addEmptySampleMetadata(workspaceUuid, projectUuid, sampleUuid, e.getValue().uuid());
      }
    }

    // Raw read metadata
    var raw_read_metadata_ids =
        madbot.getMetadataCategory(workspaceUuid, enaPluginslug, "raw_read");

    // Define metadata complex values
    var libraryLayoutValue = Map.of("library_layout", "single", "library_selection", "cDNA");
    var instrumentValue = Map.of("platform", "ILLUMINA", "model", "NextSeq 2000");

    for (var e :
        Map.of(
                "ena__raw_read__alias",
                sampleName,
                "ena__raw_read__title",
                sampleDescription,
                "ena__raw_read__library_source",
                library.enaLibrarySource(),
                "ena__raw_read__library_strategy",
                library.enaLibraryStrategy(),
                "ena__raw_read__library_layout",
                libraryLayoutValue,
                "ena__raw_read__instrument",
                instrumentValue)
            .entrySet()) {

      if (!raw_read_metadata_ids.containsKey(e.getKey())) {
        throw new IOException("Missing metadata key: " + e.getKey());
      }
      System.out.println("\t- " + e.getKey() + ": " + e.getValue());
      madbot.setSampleMetadata(
          workspaceUuid,
          projectUuid,
          sampleUuid,
          raw_read_metadata_ids.get(e.getKey()).uuid(),
          e.getValue());
    }

    // Add unknown required metadata with empty values
    for (Map.Entry<String, MadbotApiClient.MetadataFieldInfo> e :
        raw_read_metadata_ids.entrySet()) {

      if (e.getValue().mandatory()) {
        System.out.println("\t- " + e.getKey() + ": " + e.getValue());
        madbot.addEmptySampleMetadata(workspaceUuid, projectUuid, sampleUuid, e.getValue().uuid());
      }
    }
  }

  //
  // Utility methods
  //

  private static String fetchMadbotPluginSlug(MadbotApiClient madbotApiClient, String pluginName)
      throws IOException, KenetreException {

    var result = madbotApiClient.getPluginSlug(pluginName);
    if (result == null) {
      throw new KenetreException("Plugin " + pluginName + " not found");
    }

    return result;
  }

  private static UUID fetchMadBotWorkspaceUuid(
      MadbotApiClient madbotApiClient, String workspaceName, boolean removeExistingWorkspace)
      throws IOException, KenetreException {

    // Remove workspace if exists
    if (removeExistingWorkspace && madbotApiClient.isWorkspace(workspaceName)) {
      madbotApiClient.removeWorkspace(workspaceName);
    }

    var result =
        madbotApiClient.isWorkspace(workspaceName)
            ? madbotApiClient.getWorkspaceUuid(workspaceName)
            : madbotApiClient.createWorkspace(workspaceName);
    if (result == null) {
      throw new KenetreException("Workspace " + workspaceName + " not found");
    }
    return result;
  }

  private static UUID fetchMadbotSshConnectionUuid(
      MadbotApiClient madbotApiClient, UUID workspaceUuid) throws IOException, KenetreException {

    UUID result = madbotApiClient.getExistingSshfsConnections(workspaceUuid);
    if (result == null) {
      result = madbotApiClient.createSshfsConnection(workspaceUuid);
    }
    if (result == null) {
      throw new KenetreException("Cannot create/obtain SSH connection");
    }

    return result;
  }

  private static void createDataLink(
      MadbotApiClient madbot,
      UUID workspaceUuid,
      UUID projectUuid,
      UUID sampleUuid,
      UUID sshConnectionUuid,
      List<List<String>> fastqPaths)
      throws IOException {

    System.out.println("Creating data links for sample UUID: " + sampleUuid + " " + fastqPaths);
    for (var laneFastqPaths : fastqPaths) {

      List<UUID> dataUuids = new ArrayList<>();

      for (var fastqPath : laneFastqPaths) {

        // Create data
        var dataUuid = madbot.createData(workspaceUuid, sshConnectionUuid, fastqPath);
        System.out.println("\t- Create data (" + fastqPath + ") UUID: " + dataUuid);
        dataUuids.add(dataUuid);

        // Create datalink
        var datalinkUuid = madbot.createDataLink(workspaceUuid, projectUuid, sampleUuid, dataUuid);
        System.out.println("\t- Create datalink UUID: " + datalinkUuid);

        // Create bound sample
        madbot.createBoundSample(workspaceUuid, sampleUuid, dataUuid, datalinkUuid);
        System.out.println("\t- Create bound sample for data UUID: " + dataUuid);
      }

      // Associate paired-end files
      if (dataUuids.size() > 1) {

        var associationUuid = madbot.createSampleAssociation(workspaceUuid, dataUuids);
        System.out.println("\t- Create sample association UUID: " + associationUuid);
      }
    }
  }

  private static List<Sample> filterSamplesByName(
      List<Sample> samples, Collection<String> sampleNamesToKeep) {

    if (sampleNamesToKeep == null || sampleNamesToKeep.isEmpty()) {
      return samples;
    }

    return samples.stream()
        .filter(sample -> sampleNamesToKeep.contains(sample.sampleName()))
        .collect(Collectors.toList());
  }

  //
  // Constructor
  //

  /**
   * Constructor.
   *
   * @param actionName The name of the action to execute (e.g., "list-remote-projects").
   * @param conf The configuration map containing necessary parameters for the command.
   * @param arguments The list of command line arguments passed to the command.
   * @throws KenetreException If an error occurs while initializing the command (e.g., invalid
   *     configuration).
   */
  public SubmitMadbotCommand(String actionName, Map<String, String> conf, List<String> arguments)
      throws KenetreException {

    super(actionName, conf, arguments, false);
  }
}
