package fr.ens.biologie.genomique.kenetre.db.fg;

import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.db.GenomiqueEnsApiClient;
import fr.ens.biologie.genomique.kenetre.db.model.LibraryInfo;
import fr.ens.biologie.genomique.kenetre.db.model.ProjectInfo;
import fr.ens.biologie.genomique.kenetre.db.model.RunInfo;
import fr.ens.biologie.genomique.kenetre.db.model.RunMetrics;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates the submission data for a project, gathering information from the database (project info,
 * libraries, runs, species) and preparing dictionaries suitable for the France Génomique API.
 *
 * @since 0.41
 * @author Laurent Jourdren
 */
public class FgProjectSubmissionBuilder {

  public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final Map<String, String> applicationFgDict;
  private final Map<String, String> instrumentFgDict;
  private final Map<String, String> flowcellFgDict;
  private final Map<String, String> speciesFgDict;

  /**
   * Create a new builder.
   *
   * @param applicationFgDict application nomenclature map
   * @param instrumentFgDict instrument nomenclature map
   * @param flowcellFgDict flowcell nomenclature map
   * @param speciesFgDict species nomenclature map
   */
  public FgProjectSubmissionBuilder(
      Map<String, String> applicationFgDict,
      Map<String, String> instrumentFgDict,
      Map<String, String> flowcellFgDict,
      Map<String, String> speciesFgDict) {
    this.applicationFgDict = requireNonNull(applicationFgDict);
    this.instrumentFgDict = requireNonNull(instrumentFgDict);
    this.flowcellFgDict = requireNonNull(flowcellFgDict);
    this.speciesFgDict = requireNonNull(speciesFgDict);
  }

  /**
   * Result of building submission data: a project dict and a list of manip dicts.
   *
   * @param projectDict the project data
   * @param manips the list of manip data
   */
  public record SubmissionData(Map<String, Object> projectDict, List<Map<String, Object>> manips) {}

  /**
   * Create submission data for a project.
   *
   * @param projectName the project acronym
   * @return the submission data
   */
  public SubmissionData createSubmissionData(
      GenomiqueEnsApiClient gensApiclient, String projectName) throws IOException {

    // requireNonNull(conn);
    requireNonNull(projectName);

    Map<String, Object> projectDict = new LinkedHashMap<>();

    // Retrieve project and library information
    ProjectInfo project = gensApiclient.fetchProjectInfo(projectName);
    List<LibraryInfo> libraries = new ArrayList<>();
    for (String libraryBatchId : project.libraryBatchIds()) {
      libraries.add(gensApiclient.fetchLibraryBatch(libraryBatchId));
    }

    // Retrieve library information

    // Set results for project
    retrieveProjectInfo(project, libraries, projectDict);

    List<Integer> taxonIds = project.taxons();

    projectDict.put("confidentiel_fg", false);
    projectDict.put("Nouveau_surProjet", false);
    projectDict.put("cle_porteur_fg", "");
    projectDict.put("cle_tutelle_fg", "");
    projectDict.put("cle_unite_fg", "");

    // String maxLibraryReceipeDate = null;
    List<Map<String, Object>> manips = new ArrayList<>();

    for (LibraryInfo library : libraries) {
      Map<String, Object> manipDict = new LinkedHashMap<>();
      manips.add(manipDict);

      retrieveLibraryInfo(library, projectDict, manipDict);
      List<String> runs = library.runs();

      for (String runId : runs) {
        RunInfo run = gensApiclient.fetchRunInfo(runId, projectName);

        boolean successRun = retrieveRunInfo(gensApiclient, run, projectName, manipDict);
        if (!successRun) {
          continue;
        }
        String supplier = (String) manipDict.get("_instrument_supplier");
        retrieveRunMetricsInfo(gensApiclient, supplier, runId, manipDict);

        manipDict.put("cle_espece_fg", taxonIds);
      }
      projectDict.put(
          "date_last_manip",
          minDate(
              (String) projectDict.get("date_last_manip"),
              (String) manipDict.get("date_validation_qc")));

      // Convert reads to millions and bases to gigabases
      Number mread = (Number) manipDict.get("mread_lib");
      Number gb = (Number) manipDict.get("gb_lib");
      if (mread != null) {
        manipDict.put("mread_lib", mread.longValue() / 1_000_000L);
      }
      if (gb != null) {
        manipDict.put("gb_lib", gb.longValue() / 1_000_000_000L);
      }
      manipDict.put("nouveau_sousProjet", false);
    }

    // Check project entries
    for (String k :
        List.of(
            "nom_projet",
            "debut_projet",
            "fin_projet",
            "date_first_manip",
            "date_last_manip",
            "projet_interne",
            "collab_presta",
            "cle_projet_pf",
            "nom_porteur",
            "prenom_porteur",
            "email_porteur",
            "tutelle_porteur",
            "unite_porteur",
            "num_national_unite",
            "ville_porteur",
            "zcode_porteur")) {
      checkEntry(projectDict, k);
    }

    // Check manip entries
    for (Map<String, Object> manipDict : manips) {
      for (String k :
          List.of(
              "cle_manip_pf",
              "cle_projet_pf",
              "cle_espece_fg",
              "cle_type_flowcell_fg",
              "nb_librairies",
              "nb_flowcell",
              "gb_lib",
              "mread_lib",
              "librairies_passees",
              "developpement",
              "type_run",
              "date_validation_qc",
              "descriptif",
              "nb_cycles_total",
              "paired_end")) {
        checkEntry(manipDict, k);
      }

      switch (String.valueOf(manipDict.get("type_run"))) {
        case "standard" -> {
          checkEntry(manipDict, "cle_application_fg");
        }
        case "ready to load" -> {
          manipDict.put("cle_application_fg", false);
        }
        default -> new IllegalStateException("Invalid type_run: " + manipDict.get("type_run"));
      }

      // Remove hidden entries (keys starting with '_')
      manipDict.keySet().removeIf(k -> k.startsWith("_"));
    }

    return new SubmissionData(projectDict, manips);
  }

  //
  // Database retrieval methods
  //

  private void retrieveProjectInfo(
      ProjectInfo project, List<LibraryInfo> libraries, Map<String, Object> projectDict) {

    requireNonNull(project);
    requireNonNull(projectDict);

    var projectName = project.acronym();

    // Check project data
    if (project.rd() && "GenomiqueENS".equals(project.labName())) {
      throw new IllegalStateException(
          "This script does not handle GenomiqueENS R&D project like the "
              + projectName
              + " project!");
    }

    checkNotNull(project.labName(), projectName, "etablissement");
    checkNotNull(project.firstName(), projectName, "prénom chef équipe");
    checkNotNull(project.lastName(), projectName, "nom chef équipe");
    checkNotNull(project.email(), projectName, "courriel chef équipe");
    checkNotNull(project.employer(), projectName, "employeur chef équipe");
    checkNotNull(project.status(), projectName, "statut");
    checkNotNull(project.endYear(), projectName, "année de fin");
    checkNotNull(project.resultSendDate(), projectName, "date envoi resultats");
    checkNotNull(project.city(), projectName, "commune");
    checkNotNull(project.zipCode(), projectName, "code postal");
    checkNotNull(project.labNationalNumber(), projectName, "numero national de structure");

    // Check species
    List<String> especes = project.species();
    if (especes == null || especes.isEmpty()) {
      throw new IllegalStateException(
          "In project \"" + projectName + "\", field \"especes\" is not set.");
    }

    if (!"Terminé".equals(project.status())) {
      throw new IllegalStateException("Project " + projectName + " is not terminated");
    }

    String maxLibraryReceipeDate = maxLibraryReceipeDate(libraries);

    // Set the start of the project to the latest library receipe date
    if (maxLibraryReceipeDate == null) {
      throw new IllegalStateException("Project " + projectName + " has no start date!");
    }

    // Build project dict
    projectDict.put("cle_surProjet_fg", "");
    projectDict.put("nom_projet", projectName);
    projectDict.put("debut_projet", maxLibraryReceipeDate);
    projectDict.put("fin_projet", project.resultSendDate());
    projectDict.put("date_first_manip", null);
    projectDict.put("date_last_manip", null);
    projectDict.put("projet_interne", "1 Plateforme".equals(project.labIndicator()));
    projectDict.put("cle_projet_pf", projectName);
    projectDict.put("nom_porteur", project.lastName());
    projectDict.put("prenom_porteur", project.firstName());
    projectDict.put("email_porteur", project.email());
    projectDict.put("tutelle_porteur", project.employer());
    projectDict.put("unite_porteur", project.labName());
    projectDict.put("num_national_unite", project.labNationalNumber());
    projectDict.put("ville_porteur", project.city());
    projectDict.put("zcode_porteur", project.zipCode());
  }

  private void retrieveLibraryInfo(
      LibraryInfo library, Map<String, Object> projectDict, Map<String, Object> manipDict) {

    if (!projectDict.containsKey("cle_projet_pf")) {
      throw new IllegalStateException("Project name not found in project dict");
    }

    manipDict.put("cle_projet_fg", null);
    manipDict.put("cle_manip_pf", library.libraryBatchId());
    manipDict.put("cle_projet_pf", projectDict.get("cle_projet_pf"));

    String applicationFg = library.fgApplication();
    if (applicationFg != null && this.applicationFgDict.containsKey(applicationFg)) {
      manipDict.put("cle_application_fg", this.applicationFgDict.get(applicationFg));
    }

    String protocole = library.protocol();
    boolean readyToLoad = "Ready-to-load".equals(protocole);

    manipDict.put("cle_espece_fg", null);
    manipDict.put("cle_type_flowcell_fg", null);

    if (readyToLoad) {
      manipDict.put("nb_librairies", library.librarySequencedCount());
    } else {
      manipDict.put("nb_librairies", library.libraryFirstMadeCount());
    }

    manipDict.put("nb_flowcell", null);
    manipDict.put("gb_lib", null);
    manipDict.put("mread_lib", null);
    manipDict.put("nb_cycles_total", null);
    manipDict.put("librairies_passees", true);
    manipDict.put("developpement", false);
    manipDict.put("paired_end", false);
    manipDict.put("date_validation_qc", null);
    manipDict.put("descriptif", protocole);

    if (readyToLoad) {
      manipDict.put("type_run", "ready to load");
    } else {
      manipDict.put("type_run", "standard");
    }

    projectDict.put("collab_presta", manipDict.get("type_run"));

    if (library.receiptDate() != null) {
      projectDict.put(
          "date_first_manip",
          minDate((String) projectDict.get("date_first_manip"), library.receiptDate()));
    }
  }

  private boolean retrieveRunInfo(
      GenomiqueEnsApiClient gensApiclient,
      RunInfo run,
      String projectName,
      Map<String, Object> manipDict)
      throws IOException {

    // Discard failed runs
    if (run.qcDate() == null) {
      return false;
    }

    String flowCellFg = run.flowCellFg();
    if (flowCellFg != null && this.flowcellFgDict.containsKey(flowCellFg)) {
      manipDict.put("cle_type_flowcell_fg", this.flowcellFgDict.get(flowCellFg));
    }
    manipDict.put("_instrument_supplier", run.supplier());
    manipDict.put(
        "date_validation_qc", maxDate((String) manipDict.get("date_validation_qc"), run.qcDate()));

    // Compute percent of flowcell
    double percentFlowcell;

    switch (run.supplier()) {
      case "Illumina" ->
          percentFlowcell =
              gensApiclient.fetchIlluminaFlowcellPercentUsage(run.runId(), projectName);
      case "Oxford Nanopore Technologies" -> percentFlowcell = 1.0;
      default -> throw new IllegalStateException("Unknown instrument supplier: " + run.supplier());
    }

    Object nbFlowcell = manipDict.get("nb_flowcell");
    if (nbFlowcell == null) {
      manipDict.put("nb_flowcell", percentFlowcell);
    } else {
      manipDict.put("nb_flowcell", ((Number) nbFlowcell).doubleValue() + percentFlowcell);
    }

    return true;
  }

  private void retrieveRunMetricsInfo(
      GenomiqueEnsApiClient genomiqueEnsApiClient,
      String supplier,
      String runId,
      Map<String, Object> manipDict)
      throws IOException {

    RunMetrics runMetrics;
    switch (supplier) {
      case "Illumina" -> runMetrics = genomiqueEnsApiClient.fetchIlluminaRunMetrics(runId);
      case "Oxford Nanopore Technologies" ->
          runMetrics = genomiqueEnsApiClient.fetchNanoporeRunMetrics(runId);
      default -> throw new IllegalStateException("Unknown instrument supplier: " + supplier);
    }

    manipDict.put("nb_cycles_total", runMetrics.cycleCount());
    manipDict.put("paired_end", runMetrics.pairedEnd());
    String modelFg = runMetrics.fgModel();
    if (modelFg != null && this.instrumentFgDict.containsKey(modelFg)) {
      manipDict.put("cle_type_instrument_fg", this.instrumentFgDict.get(modelFg));
    }

    manipDict.put("mread_lib", runMetrics.readCount());
    manipDict.put("gb_lib", runMetrics.baseCount());
  }

  private static String maxLibraryReceipeDate(List<LibraryInfo> libraries) {

    String maxLibraryReceipeDate = null;
    for (LibraryInfo library : libraries) {

      // Is the latest library receipe date?
      if (maxLibraryReceipeDate == null) {
        maxLibraryReceipeDate = library.receiptDate();
      } else if (library.receiptDate() != null
          && maxLibraryReceipeDate.compareTo(library.receiptDate()) > 0) {
        maxLibraryReceipeDate = library.receiptDate();
      }
    }

    return maxLibraryReceipeDate;
  }

  //
  // Utility methods
  //

  static String minDate(String previousDateStr, String newDateStr) {
    if (previousDateStr == null) {
      return newDateStr;
    }
    if (newDateStr == null) {
      return previousDateStr;
    }
    LocalDate pd = LocalDate.parse(previousDateStr, DATE_FMT);
    LocalDate nd = LocalDate.parse(newDateStr, DATE_FMT);
    return nd.isBefore(pd) ? newDateStr : previousDateStr;
  }

  static String maxDate(String previousDateStr, String newDateStr) {
    if (previousDateStr == null) {
      return newDateStr;
    }
    if (newDateStr == null) {
      return previousDateStr;
    }
    LocalDate pd = LocalDate.parse(previousDateStr, DATE_FMT);
    LocalDate nd = LocalDate.parse(newDateStr, DATE_FMT);
    return nd.isAfter(pd) ? newDateStr : previousDateStr;
  }

  private static void checkNotNull(Object value, String projectName, String fieldName) {
    if (value == null) {
      throw new IllegalStateException(
          "In project \"" + projectName + "\", field \"" + fieldName + "\" is not set.");
    }
  }

  private static void checkEntry(Map<String, Object> dict, String key) {
    Object value = dict.get(key);
    if (value == null) {
      throw new IllegalStateException("Value for \"" + key + "\" key is missing.");
    }

    if (String.valueOf(value).isBlank()) {
      throw new IllegalStateException("Value for \"" + key + "\" is empty.");
    }
  }
}
