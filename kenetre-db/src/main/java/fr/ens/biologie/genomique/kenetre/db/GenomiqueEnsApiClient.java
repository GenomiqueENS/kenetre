package fr.ens.biologie.genomique.kenetre.db;

import static java.util.Objects.requireNonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.ens.biologie.genomique.kenetre.db.model.LibraryInfo;
import fr.ens.biologie.genomique.kenetre.db.model.ProjectInfo;
import fr.ens.biologie.genomique.kenetre.db.model.RunInfo;
import fr.ens.biologie.genomique.kenetre.db.model.RunMetrics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GenomiqueEnsApiClient is a client for the Genomique ENS REST API. It provides methods to fetch
 * project, library batch, and run information from the API and convert it into Java objects.
 *
 * @since 0.41
 * @author Laurent Jourdren
 */
public class GenomiqueEnsApiClient extends ApiClient {

  //
  // API methods
  //

  /**
   * Fetch a JSON object from the REST API for a given route and key.
   *
   * @param route the API route (e.g., "projets", "banques", "runs")
   * @param key the key to identify the specific object (e.g., project acronym, library name, run
   *     name)
   * @return
   * @throws IOException
   */
  public JsonObject fetchJsonObject(String route, String key) throws IOException {

    requireNonNull(route);
    requireNonNull(key);

    String json = get(route, key);

    // Create JSON object
    JsonArray array = this.gson.fromJson(json, JsonArray.class);

    // Get the first element of the array
    return array.get(0).getAsJsonObject();
  }

  /**
   * Fetch project information for all projects from the REST API.
   *
   * @return a list of {@link ProjectInfo} objects populated from the API response
   * @throws IOException
   */
  public List<ProjectInfo> fetchProjectInfos() throws IOException {

    return fetchProjectInfos(null);
  }

  /**
   * Fetch project information for all projects from the REST API.
   *
   * @return a list of {@link ProjectInfo} objects populated from the API response
   * @throws IOException
   */
  public List<ProjectInfo> fetchProjectInfos(Map<String, String> filters) throws IOException {

    String json = get("projets", filters);

    // Create JSON object
    JsonArray array = this.gson.fromJson(json, JsonArray.class);

    List<ProjectInfo> result = new ArrayList<>();
    for (JsonElement e : array) {
      result.add(fetchProjectInfo(e.getAsJsonObject()));
    }

    return result;
  }

  /**
   * Fetch project information from the REST API.
   *
   * @param projectName the project acronym
   * @return a {@link ProjectInfo} object populated from the API response
   * @throws IOException if an I/O or HTTP error occurs
   * @throws IllegalStateException if the project is not found
   */
  public ProjectInfo fetchProjectInfo(String projectName) throws IOException {

    // Fetch the JSON object
    return fetchProjectInfo(fetchJsonObject("projets", projectName));
  }

  private ProjectInfo fetchProjectInfo(JsonObject obj) {

    return new ProjectInfo(
        jsonArrayToStringList(obj.getAsJsonArray("banques")),
        jsonArrayToStringList(obj.getAsJsonArray("runs")),
        jsonArrayToStringList(obj.getAsJsonArray("especes")),
        jsonArrayToIntegerList(obj.getAsJsonArray("taxons")),
        jsonToString(obj, "acronyme"),
        jsonToBoolean(obj, "rd"),
        jsonToString(obj, "date_envoi_resultats"),
        jsonToString(obj, "prenom"),
        jsonToString(obj, "nom"),
        jsonToString(obj, "courriel"),
        jsonToString(obj, "employeur"),
        jsonToString(obj, "statut"),
        jsonToInteger(obj, "annee_de_fin"),
        jsonToString(obj, "indicateur_laboratoires"),
        jsonToString(obj, "commune"),
        jsonToString(obj, "code_postal"),
        jsonToString(obj, "numero_national_de_structure"),
        jsonToString(obj, "etablissement"),
        jsonToBoolean(obj, "prestation_fabrication_de_banque"),
        jsonToBoolean(obj, "prestation_sequencage"),
        jsonToString(obj, "prestation_analyse"));
  }

  /**
   * Fetch library batch information from the REST API.
   *
   * @param libraryName the library batch identifier
   * @return a {@link LibraryInfo} object populated from the API response
   * @throws IOException if an I/O or HTTP error occurs
   * @throws IllegalStateException if the library is not found
   */
  public LibraryInfo fetchLibraryBatch(String libraryName) throws IOException {

    // Fetch the JSON object
    JsonObject obj = fetchJsonObject("banques", libraryName);

    return new LibraryInfo(
        jsonToString(obj, "banque_id"),
        jsonToObject(obj, "nb_banques_primo_fabriquees"),
        jsonToObject(obj, "nb_banques_sequencees"),
        jsonToString(obj, "protocole"),
        jsonToString(obj, "application_fg"),
        jsonToString(obj, "date_reception"),
        jsonToString(obj, "date_qc_banques"),
        jsonArrayToStringList(obj.getAsJsonArray("runs")));
  }

  /**
   * Fetch run information from the REST API.
   *
   * @param runName the run identifier
   * @param projectName the project acronym
   * @return a {@link RunInfo} object populated from the API response
   * @throws IOException if an I/O or HTTP error occurs
   * @throws IllegalStateException if the run is not found
   */
  public RunInfo fetchRunInfo(String runName, String projectName) throws IOException {

    // Fetch the JSON object
    JsonObject obj = fetchJsonObject("runs", runName);

    // Check if acronyme is present for the run
    List<String> acronymes = jsonArrayToStringList(obj.getAsJsonArray("acronymes"));
    if (acronymes.isEmpty()) {
      throw new IOException("No acronymes found for run " + runName);
    } else if (!acronymes.contains(projectName)) {
      throw new IOException(
          "Project " + projectName + " not found in the list of acronymes for run " + runName);
    }

    return new RunInfo(
        jsonToString(obj, "run_id"),
        jsonToString(obj, "date_run"),
        jsonToString(obj, "date_qc"),
        jsonToString(obj, "probleme"),
        jsonToString(obj, "flowcell"),
        jsonToString(obj, "flow_cell_fg"),
        jsonToString(obj, "fournisseur"),
        jsonToString(obj, "instrument_fg"));
  }

  public double fetchIlluminaFlowcellPercentUsage(String runName, String projectName)
      throws IOException {

    JsonObject obj = fetchJsonObject("illumina-runs", runName);

    long cluster_count_total = 0;
    long cluster_count_project = 0;

    for (JsonElement e1 : obj.getAsJsonArray("samples")) {

      JsonObject sampleObj = e1.getAsJsonObject();
      if (sampleObj.get("undetermined").getAsBoolean()) {
        continue;
      }

      // TODO Handle project alias
      boolean selectedProject = projectName.equals(sampleObj.get("sample_project").getAsString());
      for (JsonElement e2 : sampleObj.getAsJsonArray("metrics")) {

        JsonObject metricObj = e2.getAsJsonObject();
        if (!metricObj.get("pf").getAsBoolean()) {
          continue;
        }
        long cluster_count = metricObj.get("result_cluster_count").getAsLong();
        cluster_count_total += cluster_count;
        if (selectedProject) {
          cluster_count_project += cluster_count;
        }
      }
    }

    return (double) cluster_count_project / cluster_count_total;
  }

  public RunMetrics fetchIlluminaRunMetrics(String runName) throws IOException {

    JsonObject obj = fetchJsonObject("illumina-runs", runName);

    int cycleCount = obj.get("cycle_count").getAsInt();
    boolean pairedEnd = obj.get("read_count").getAsInt() == 2;
    boolean success = obj.get("success").getAsBoolean();

    JsonObject instrumentObj = obj.getAsJsonObject("instrument");

    String model = instrumentObj.get("model").getAsString();
    String fgModel = instrumentObj.get("model_fg").getAsString();

    long readCount = 0L;
    long baseCount = 0L;

    for (JsonElement e1 : obj.getAsJsonArray("samples")) {

      JsonObject sampleObj = e1.getAsJsonObject();
      if (sampleObj.get("undetermined").getAsBoolean()) {
        continue;
      }

      for (JsonElement e2 : sampleObj.getAsJsonArray("metrics")) {

        JsonObject metricObj = e2.getAsJsonObject();
        if (!metricObj.get("pf").getAsBoolean()) {
          continue;
        }
        readCount += metricObj.get("result_cluster_count").getAsLong();
        baseCount += metricObj.get("result_yield").getAsLong();
      }
    }

    return new RunMetrics(cycleCount, pairedEnd, model, fgModel, readCount, baseCount, success);
  }

  public RunMetrics fetchNanoporeRunMetrics(String runName) throws IOException {

    JsonObject obj = fetchJsonObject("nanopre-runs", runName);

    int cycleCount = 0; // Not applicable for Nanopore runs
    boolean pairedEnd = false; // No paired-end mode for Nanopore runs
    boolean success = true; // TODO Handle run fails

    JsonObject instrumentObj = obj.getAsJsonObject("instrument");

    String model = instrumentObj.get("model").getAsString();
    String fgModel = instrumentObj.get("model_fg").getAsString();

    JsonObject metricsObj = obj.getAsJsonObject("instrument");

    long readCount = metricsObj.get("total_read_count").getAsLong();
    long baseCount = metricsObj.get("total_base_count").getAsLong();

    return new RunMetrics(cycleCount, pairedEnd, model, fgModel, readCount, baseCount, success);
  }

  //
  // Utility methods
  //

  private static <E> List<E> removeNullElements(List<E> list) {

    if (list == null) {
      return null;
    }

    if (list.isEmpty()) {
      return null;
    }

    List<E> result = new ArrayList<>();
    for (E e : list) {
      if (e != null) {
        result.add(e);
      }
    }

    return result;
  }

  private static List<String> jsonArrayToStringList(JsonArray arr) {
    List<String> result = new ArrayList<>();
    if (arr != null) {
      for (JsonElement el : arr) {
        if (!el.isJsonNull()) {
          result.add(el.getAsString());
        }
      }
    }
    return removeNullElements(result);
  }

  private static List<Integer> jsonArrayToIntegerList(JsonArray arr) {
    List<Integer> result = new ArrayList<>();
    if (arr != null) {
      for (JsonElement el : arr) {
        if (!el.isJsonNull()) {
          result.add(el.getAsInt());
        }
      }
    }
    return removeNullElements(result);
  }

  private static boolean jsonToBoolean(JsonObject obj, String key) {
    return obj.has(key) && !obj.get(key).isJsonNull() && obj.get(key).getAsBoolean();
  }

  private static Integer jsonToInteger(JsonObject obj, String key) {
    return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : null;
  }

  private static String jsonToString(JsonObject obj, String key) {
    return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
  }

  private static Object jsonToObject(JsonObject obj, String key) {
    return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsNumber() : null;
  }

  //
  // Credential
  //

  /**
   * Create a new FgApiClient.
   *
   * @param credentials map containing url, domain_kc, realm, username, password
   * @param debugApiRequests whether to log debug information for API requests
   */
  public GenomiqueEnsApiClient(Map<String, String> credentials, boolean debugApiRequests) {
    super(credentials, "genomiqueens", false, false);
  }
}
