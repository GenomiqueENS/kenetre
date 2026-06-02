package fr.ens.biologie.genomique.kenetre.db.fg;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.ens.biologie.genomique.kenetre.db.ApiClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for the FG REST API, providing high-level methods for projects and manips.
 *
 * @since 0.41
 * @author Laurent Jourdren
 */
public class FgApiClient extends ApiClient {

  //
  // Nomenclature
  //

  /**
   * Load a nomenclature from the FG API.
   *
   * @param nomenclature the nomenclature name
   * @param keyField the JSON field to use as map key
   * @param valueField the JSON field to use as map value
   * @param filterField optional field to filter on (may be null)
   * @param acceptedValues accepted values for the filter field (may be null)
   * @return a map of key to value
   * @throws IOException if an I/O error occurs
   */
  public Map<String, String> getNomenclature(
      String nomenclature,
      String keyField,
      String valueField,
      String filterField,
      List<String> acceptedValues)
      throws IOException {

    String responseBody = get("nomenclatures", Map.of("n", nomenclature));
    JsonArray data = super.gson.fromJson(responseBody, JsonArray.class);

    Map<String, String> result = new LinkedHashMap<>();
    for (JsonElement element : data) {
      JsonObject obj = element.getAsJsonObject();
      JsonElement keyElem = obj.get(keyField);
      if (keyElem == null || keyElem.isJsonNull()) {
        continue;
      }
      String key = keyElem.getAsString();
      if (key.isBlank()) {
        continue;
      }

      if (filterField != null) {
        JsonElement filterElem = obj.get(filterField);
        if (filterElem == null
            || filterElem.isJsonNull()
            || acceptedValues == null
            || !acceptedValues.contains(filterElem.getAsString())) {
          continue;
        }
      }

      JsonElement valueElem = obj.get(valueField);
      result.put(
          key, valueElem != null && !valueElem.isJsonNull() ? valueElem.getAsString() : null);
    }
    return result;
  }

  /**
   * Load a nomenclature without filtering.
   *
   * @param nomenclature the nomenclature name
   * @param keyField the JSON field to use as map key
   * @param valueField the JSON field to use as map value
   * @return a map of key to value
   * @throws IOException if an I/O error occurs
   */
  public Map<String, String> getNomenclature(
      String nomenclature, String keyField, String valueField) throws IOException {
    return getNomenclature(nomenclature, keyField, valueField, null, null);
  }

  //
  // High-level FG operations
  //

  /**
   * List entries from a route.
   *
   * @param route the API route
   * @param cleName the key name parameter
   * @param clePf the platform key field
   * @param cleFg the FG key field
   * @return list of (platform_key, fg_key) pairs
   * @throws IOException if an I/O or API error occurs
   */
  public List<String[]> list(String route, String cleName, String clePf, String cleFg)
      throws IOException {

    String responseBody = get(route, Map.of(cleName, ""));
    JsonElement parsed = this.gson.fromJson(responseBody, JsonElement.class);

    List<String[]> result = new ArrayList<>();

    if (parsed.isJsonObject()) {
      JsonObject obj = parsed.getAsJsonObject();
      if (obj.has("status") && obj.get("status").getAsInt() == 0) {
        if (obj.has("errors")
            && obj.get("errors").getAsInt() == 1
            && obj.has("list_errors")
            && obj.get("list_errors").getAsString().equals("No available " + route + ".")) {
          return List.of();
        }
        throw new IOException("ERROR: Cannot list " + route + ".");
      }
    }

    if (parsed.isJsonArray()) {
      for (JsonElement e : parsed.getAsJsonArray()) {
        JsonObject obj = e.getAsJsonObject();
        String pf =
            obj.has(clePf) && !obj.get(clePf).isJsonNull() ? obj.get(clePf).getAsString() : null;
        String fg =
            obj.has(cleFg) && !obj.get(cleFg).isJsonNull() ? obj.get(cleFg).getAsString() : null;
        result.add(new String[] {pf, fg});
      }
    }
    return result;
  }

  /** List all projects. */
  public List<String[]> listProjects() throws IOException {
    return list("projets", "cle_projet", "cle_projet_pf", "cle_projet_fg");
  }

  /** Get the FG key for a project by name. */
  public String getCleFgProject(String projectName) throws IOException {
    for (String[] p : listProjects()) {
      if (projectName.equals(p[0])) {
        return p[1];
      }
    }
    return null;
  }

  /** Check if a project exists. */
  public boolean isProjectExists(String projectName) throws IOException {
    return getCleFgProject(projectName) != null;
  }

  /** Get a project's JSON data. */
  public JsonObject getProject(String projectName) throws IOException {
    String cleFg = getCleFgProject(projectName);
    if (cleFg == null) {
      throw new IOException("ERROR: Cannot find unknown project: " + projectName);
    }
    String responseBody = get("projets", Map.of("cle_projet", cleFg));
    JsonArray arr = this.gson.fromJson(responseBody, JsonArray.class);
    return arr.get(0).getAsJsonObject();
  }

  /** Add a project. */
  public void addProject(Map<String, Object> projectDict) throws IOException {
    String responseBody = post("projets", projectDict);
    JsonObject json = this.gson.fromJson(responseBody, JsonObject.class);
    handleRequestMessage(json, "Add", String.valueOf(projectDict.get("nom_projet")), "projets");
  }

  /** Update a project. */
  public void updateProject(String projectName, Map<String, Object> projectDict)
      throws IOException {
    String cleFg = getCleFgProject(projectName);
    if (cleFg == null) {
      throw new IOException("ERROR: Cannot update unknown project: " + projectName);
    }
    String responseBody = put("projets", cleFg, projectDict);
    JsonObject json = this.gson.fromJson(responseBody, JsonObject.class);
    handleRequestMessage(json, "Update", String.valueOf(projectDict.get("nom_projet")), "projets");
  }

  /** Remove a project. */
  public void removeProject(String projectName) throws IOException {
    String cleFg = getCleFgProject(projectName);
    if (cleFg == null) {
      throw new IOException("ERROR: Cannot remove unknown project: " + projectName);
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("cle_projet", cleFg);
    String responseBody = delete("projets", data);
    JsonObject json = this.gson.fromJson(responseBody, JsonObject.class);
    handleRequestMessage(json, "Remove", projectName, "projets");
  }

  /** List all manips. */
  public List<String[]> listManips() throws IOException {
    return list("manips", "cle_manip", "cle_manip_pf", "cle_manip_fg");
  }

  /** Get the FG key for a manip by name. */
  public String getCleFgManip(String manipName) throws IOException {
    for (String[] p : listManips()) {
      if (manipName.equals(p[0])) {
        return p[1];
      }
    }
    return null;
  }

  /** Check if a manip exists. */
  public boolean isManipExists(String manipName) throws IOException {
    return getCleFgManip(manipName) != null;
  }

  /** Get a manip's JSON data. */
  public JsonObject getManip(String manipName) throws IOException {
    String cleFg = getCleFgManip(manipName);
    if (cleFg == null) {
      throw new IOException("ERROR: Cannot find unknown manip: " + manipName);
    }
    String responseBody = get("manips", Map.of("cle_manip", cleFg));
    JsonArray arr = this.gson.fromJson(responseBody, JsonArray.class);
    return arr.get(0).getAsJsonObject();
  }

  /** Add a manip. */
  public void addManip(Map<String, Object> manipDict) throws IOException {
    String responseBody = post("manips", manipDict);
    JsonObject json = this.gson.fromJson(responseBody, JsonObject.class);
    handleRequestMessage(json, "Add", String.valueOf(manipDict.get("cle_manip_pf")), "manips");
  }

  /** Update a manip. */
  public void updateManip(String manipName, Map<String, Object> manipDict) throws IOException {
    String cleFg = getCleFgManip(manipName);
    if (cleFg == null) {
      throw new IOException("ERROR: Cannot update unknown manip: " + manipName);
    }
    String responseBody = put("manips", cleFg, manipDict);
    JsonObject json = this.gson.fromJson(responseBody, JsonObject.class);
    handleRequestMessage(json, "Update", String.valueOf(manipDict.get("cle_manip_pf")), "manips");
  }

  /** Remove a manip. */
  public void removeManip(String manipName) throws IOException {
    String cleFg = getCleFgManip(manipName);
    if (cleFg == null) {
      throw new IOException("ERROR: Cannot remove unknown manip: " + manipName);
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("cle_manip", cleFg);
    String responseBody = delete("manips", data);
    JsonObject json = this.gson.fromJson(responseBody, JsonObject.class);
    handleRequestMessage(json, "Remove", manipName, "manips");
  }

  /**
   * Get all manips for a project.
   *
   * @param projectName the project name
   * @return list of manip JSON objects
   * @throws IOException if an error occurs
   */
  public List<JsonObject> getManipsProject(String projectName) throws IOException {
    String projectKey = getCleFgProject(projectName);
    if (projectKey == null) {
      throw new IOException("ERROR: Unknown project: " + projectName);
    }

    String responseBody = get("manips", Map.of("cle_projet", projectKey, "cle_manip", ""));
    JsonElement parsed = this.gson.fromJson(responseBody, JsonElement.class);

    List<JsonObject> result = new ArrayList<>();

    if (parsed.isJsonObject()) {
      JsonObject obj = parsed.getAsJsonObject();
      if (obj.has("status") && obj.get("status").getAsInt() == 0) {
        if (obj.has("errors")
            && obj.get("errors").getAsInt() == 1
            && obj.has("list_errors")
            && obj.get("list_errors").getAsString().equals("No available manips.")) {
          return List.of();
        }
        throw new IOException("ERROR: Cannot list manips.");
      }
    }

    if (parsed.isJsonArray()) {
      for (JsonElement e : parsed.getAsJsonArray()) {
        JsonObject obj = e.getAsJsonObject();
        if (obj.has("cle_projet_fg")
            && !obj.get("cle_projet_fg").isJsonNull()
            && obj.get("cle_projet_fg").getAsString().equals(projectKey)) {
          result.add(obj);
        }
      }
    }
    return result;
  }

  //
  // Utility methods
  //

  private void handleRequestMessage(
      JsonObject responseJson, String operation, String name, String route) throws IOException {

    int statusVal = responseJson.has("status") ? responseJson.get("status").getAsInt() : -1;
    String statusMessage =
        responseJson.has("status_message") ? responseJson.get("status_message").getAsString() : "";

    String routeName = route.endsWith("/") ? route.substring(0, route.length() - 1) : route;

    System.out.println(
        operation
            + " "
            + name
            + " "
            + routeName
            + ": "
            + (statusVal == 1 ? "OK" : "FAIL")
            + " ("
            + statusMessage
            + ")");

    if (statusVal == 0) {
      System.out.println(this.gson.toJson(responseJson));
      throw new IOException(
          "ERROR: Cannot " + operation.toLowerCase() + " " + routeName + ": " + name);
    }
  }

  /**
   * Remove keys that are numeric strings from a JSON object (recursive).
   *
   * @param element the JSON element to process
   */
  public static void removeJsonIntegerKeys(JsonElement element) {
    if (element == null) {
      return;
    }
    if (element.isJsonArray()) {
      for (JsonElement e : element.getAsJsonArray()) {
        removeJsonIntegerKeys(e);
      }
      return;
    }
    if (element.isJsonObject()) {
      JsonObject obj = element.getAsJsonObject();
      List<String> keysToRemove = new ArrayList<>();
      for (String key : obj.keySet()) {
        try {
          Integer.parseInt(key);
          keysToRemove.add(key);
        } catch (NumberFormatException e) {
          // not numeric, keep
        }
      }
      for (String key : keysToRemove) {
        obj.remove(key);
      }
    }
  }

  //
  // Constructor
  //

  /**
   * Create a new FgApiClient.
   *
   * @param credentials map containing url, domain_kc, realm, username, password
   * @param useOuiNonInJson whether to replace booleans with "oui"/"non" in POST/PUT
   * @param debugApiRequests whether to log debug information for API requests
   */
  public FgApiClient(
      Map<String, String> credentials, boolean useOuiNonInJson, boolean debugApiRequests) {

    super(credentials, "fg", useOuiNonInJson, debugApiRequests, true, false);
  }
}
