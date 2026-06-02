package fr.ens.biologie.genomique.kenetre.db.commands;

import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.GenomiqueEnsApiClient;
import fr.ens.biologie.genomique.kenetre.db.madbot.MadbotApiClient;
import fr.ens.biologie.genomique.kenetre.db.model.LibraryInfo;
import fr.ens.biologie.genomique.kenetre.db.model.ProjectInfo;
import fr.ens.biologie.genomique.kenetre.db.model.RunInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SubmitMadbotCommand extends AbstractCommand {

  private static String MADBOT_WORKSPACE = "TEST-API";
  private static String ENA_PLUGIN_NAME = "ENA";

  private record Sample(String sampleName, String sampleDescription, LibraryInfo library) {}

  @Override
  protected String syntax() {
    return "<project_name>";
  }

  @Override
  protected void internalExecute(List<String> arguments) throws KenetreException {

    if (arguments.size() != 1) {
      System.err.println("ERROR: Invalid number of arguments. Expected 1, got " + arguments.size());
      System.exit(1);
    }

    // Get the project name
    var projectName = arguments.get(0);
    var workspace = MADBOT_WORKSPACE;

    try {

      // Retrieve project and library information
      ProjectInfo project = super.gensApiClient.fetchProjectInfo(projectName);
      List<LibraryInfo> libraries = new ArrayList<>();
      for (String libraryBatchId : project.libraryBatchIds()) {
        libraries.add(super.gensApiClient.fetchLibraryBatch(libraryBatchId));
      }

      // Remove workspace if exists
      if (super.madbotApiClient.isWorkspace(workspace)) {
        super.madbotApiClient.removeWorkspace(workspace);
      }

      // Create workspace if not exists
      var workspaceUuid = madbotApiClient.createWorkspace(workspace);

      //  Get the ENA plugin slug
      var enaPluginslug = this.madbotApiClient.getPluginSlug(ENA_PLUGIN_NAME);
      if (enaPluginslug == null) {
        throw new KenetreException("Plugin " + ENA_PLUGIN_NAME + " not found");
      }
      System.out.println("ENA plugin slug: " + enaPluginslug);

      List<Sample> samples = null;
      for (LibraryInfo library : libraries) {
        for (String run : library.runs()) {
          RunInfo runInfo = super.gensApiClient.fetchRunInfo(run, projectName);
          switch (runInfo.supplier()) {
            case "Illumina":
              samples = fetchIlluminaSamples(projectName, run, library);
              break;
            case "Oxford Nanopore Technologies":
              throw new IllegalArgumentException("Unsupported supplier: " + runInfo.supplier());

            default:
              throw new IllegalArgumentException("Unsupported supplier: " + runInfo.supplier());
          }
        }
      }

      // Submit project to Madbot
      submitMadbot(this.madbotApiClient, enaPluginslug, workspaceUuid, project, samples);

    } catch (IOException e) {
      throw new KenetreException(e);
    }
  }

  private List<Sample> fetchIlluminaSamples(String projectName, String runId, LibraryInfo library)
      throws IOException {

    List<Sample> samples = new ArrayList<>();

    for (GenomiqueEnsApiClient.IlluminaSample s :
        this.gensApiClient.fetchIlluminaSamples(runId, projectName)) {

      Sample sample = new Sample(s.name(), s.description(), library);
      if (!samples.contains(sample)) {
        samples.add(sample);
      }
    }

    return samples;
  }

  private static void submitMadbot(
      MadbotApiClient madbot,
      String enaPluginslug,
      UUID workspaceUuid,
      ProjectInfo project,
      List<Sample> samples)
      throws IOException {

    System.out.println("Workspace UUID: " + workspaceUuid);

    // Get the ids of required metadata for projects
    var studyMetadataIds = madbot.getMetadataCategory(workspaceUuid, ENA_PLUGIN_NAME, "study");

    // Create project node
    var projectUuid = madbot.createProjectNode(workspaceUuid, project.acronym());

    System.out.println(project);

    // Set known metadata values
    for (var e : Map.of("ena__study__center_name", project.labName()).entrySet()) {
      madbot.setProjectMetadata(
          workspaceUuid, projectUuid, studyMetadataIds.get(e.getKey()).uuid(), e.getValue());
    }

    // Set additional project metadata with complex values
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

    // System.out.println(madbot.getPluginSchema(enaPluginslug, "ena__study__additional_metadata"));

    // Create project metadata with empty values
    for (Map.Entry<String, MadbotApiClient.MetadataFieldInfo> entry : studyMetadataIds.entrySet()) {
      System.out.println(entry.getKey() + " -> " + entry.getValue());
      if (entry.getValue().mandatory()) {
        madbot.addEmptyProjectMetadata(workspaceUuid, projectUuid, entry.getValue().uuid());
      }
    }

    // Add empty project description that is non-mandatory
    madbot.addEmptyProjectMetadata(
        workspaceUuid, projectUuid, studyMetadataIds.get("ena__study__description").uuid());

    // Create libraries
    for (var s : samples) {
      submitLibrariesToMadbot(
          madbot,
          enaPluginslug,
          workspaceUuid,
          projectUuid,
          s.sampleName(),
          s.sampleDescription(),
          s.library());
    }
  }

  private static void submitLibrariesToMadbot(
      MadbotApiClient madbot,
      String enaPluginslug,
      UUID workspaceUuid,
      UUID projectUuid,
      String sampleName,
      String sampleDescription,
      LibraryInfo library)
      throws IOException {

    requireNonNull(sampleName, "Sample name cannot be null");
    requireNonNull(sampleDescription, "Sample description cannot be null");
    requireNonNull(library, "library cannot be null");

    // Create sample
    var sample_uuid =
        madbot.createSample(workspaceUuid, projectUuid, sampleName, sampleDescription);
    System.out.println("Sample UUID: " + sample_uuid);

    // sample__erc000011 metadata
    System.out.println("* sample__erc000011");
    var enaDefaultSampleMetadataIds =
        madbot.getMetadataGroup(workspaceUuid, enaPluginslug, "sample__erc000011");
    System.out.println("enaDefaultSampleMetadataIds=" + enaDefaultSampleMetadataIds);

    // Set the required metadata for sample to empty
    for (var e : enaDefaultSampleMetadataIds.entrySet()) {
      if (e.getValue().mandatory()) {
        System.out.println(e.getKey() + " -> " + e.getValue());
        madbot.addEmptySampleMetadata(workspaceUuid, projectUuid, sample_uuid, e.getValue().uuid());
      }
    }

    // Raw read metadata
    System.out.println("* raw_read");
    var raw_read_metadata_ids =
        madbot.getMetadataCategory(workspaceUuid, enaPluginslug, "raw_read");

    // ena__raw_read__library_layout : "Single" or "Paired"
    // ena__raw_read__instrument

    // Print schema of a metadata
    // System.out.println(madbot.getPluginSchema(enaPluginslug, "ena__raw_read__library_layout"));
    // System.out.println(madbot.getPluginSchema(enaPluginslug, "ena__raw_read__instrument"));

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
      System.out.println(e.getKey() + " -> " + e.getValue());
      madbot.setSampleMetadata(
          workspaceUuid,
          projectUuid,
          sample_uuid,
          raw_read_metadata_ids.get(e.getKey()).uuid(),
          e.getValue());
    }

    // Add unknown required metadata with empty values
    for (Map.Entry<String, MadbotApiClient.MetadataFieldInfo> e :
        raw_read_metadata_ids.entrySet()) {

      if (e.getValue().mandatory()) {
        System.out.println(e.getKey() + " -> " + e.getValue());
        madbot.addEmptySampleMetadata(workspaceUuid, projectUuid, sample_uuid, e.getValue().uuid());
      }
    }
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
