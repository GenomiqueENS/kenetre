package fr.ens.biologie.genomique.kenetre.db.madbot;

import static java.util.Objects.requireNonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for the Madbot REST API (France Bioinformatique). Provides workspace, node, sample, and
 * metadata management operations.
 *
 * @since 0.41
 * @author Laurent Jourdren
 */
public class PfMadbot {

  /** Holds the UUID and mandatory flag for a metadata field. */
  public static final class MetadataFieldInfo {

    private final String id;
    private final boolean mandatory;

    /**
     * Get the metadata field UUID.
     *
     * @return the metadata field UUID
     */
    public String getId() {
      return this.id;
    }

    /**
     * Return whether the field is mandatory.
     *
     * @return {@code true} if the field is mandatory
     */
    public boolean isMandatory() {
      return this.mandatory;
    }

    MetadataFieldInfo(String id, boolean mandatory) {
      this.id = id;
      this.mandatory = mandatory;
    }
  }

  private final String baseUrl;
  private final String bearer;
  private final HttpClient httpClient;
  private final Gson gson;

  //
  // HTTP helpers
  //

  private HttpRequest.Builder authenticatedRequestBuilder(String url, String workspaceUuid) {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + this.bearer)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");
    if (workspaceUuid != null) {
      builder.header("x-workflow", workspaceUuid);
      builder.header("x-workspace", workspaceUuid);
    }
    return builder;
  }

  private String httpGet(String url, String workspaceUuid) throws IOException {
    HttpRequest request = authenticatedRequestBuilder(url, workspaceUuid).GET().build();
    try {
      HttpResponse<String> response =
          this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      checkStatus(response, url, "GET");
      return response.body();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted during GET request", e);
    }
  }

  private String httpPost(String url, String workspaceUuid, JsonObject payload) throws IOException {
    String body = this.gson.toJson(payload);
    HttpRequest request =
        authenticatedRequestBuilder(url, workspaceUuid)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
    try {
      HttpResponse<String> response =
          this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      checkStatus(response, url, "POST");
      return response.body();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted during POST request", e);
    }
  }

  private void httpDelete(String url) throws IOException {
    HttpRequest request = authenticatedRequestBuilder(url, null).DELETE().build();
    try {
      HttpResponse<String> response =
          this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 204) {
        throw new IOException(
            "Error deleting resource: HTTP " + response.statusCode() + " - " + response.body());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted during DELETE request", e);
    }
  }

  private static void checkStatus(HttpResponse<String> response, String url, String method)
      throws IOException {
    int status = response.statusCode();
    if (status < 200 || status >= 300) {
      throw new IOException(
          "Error on " + method + " " + url + ": HTTP " + status + " - " + response.body());
    }
  }

  //
  // Workspace management
  //

  /**
   * Get the UUID of a workspace by name.
   *
   * @param workspaceName the workspace name
   * @return the workspace UUID, or {@code null} if not found
   * @throws IOException if an I/O error occurs
   */
  public String getWorkspaceUuid(String workspaceName) throws IOException {
    requireNonNull(workspaceName, "workspaceName must not be null");
    String body = httpGet(this.baseUrl + "/workspaces", null);
    JsonArray results = this.gson.fromJson(body, JsonObject.class).getAsJsonArray("results");
    for (JsonElement elem : results) {
      JsonObject ws = elem.getAsJsonObject();
      if (workspaceName.equals(ws.get("name").getAsString())) {
        return ws.get("id").getAsString();
      }
    }
    return null;
  }

  /**
   * Check whether a workspace exists.
   *
   * @param workspaceName the workspace name
   * @return {@code true} if the workspace exists
   * @throws IOException if an I/O error occurs
   */
  public boolean isWorkspaceExists(String workspaceName) throws IOException {
    return getWorkspaceUuid(workspaceName) != null;
  }

  /**
   * Create a workspace, or return the UUID of the existing one if it already exists.
   *
   * @param workspaceName the workspace name
   * @return the workspace UUID
   * @throws IOException if an I/O error occurs
   */
  public String createWorkspace(String workspaceName) throws IOException {
    requireNonNull(workspaceName, "workspaceName must not be null");
    String existingUuid = getWorkspaceUuid(workspaceName);
    if (existingUuid != null) {
      return existingUuid;
    }
    JsonObject payload = new JsonObject();
    payload.addProperty("name", workspaceName);
    payload.addProperty("description", "workspace " + workspaceName);
    String body = httpPost(this.baseUrl + "/workspaces", null, payload);
    return this.gson.fromJson(body, JsonObject.class).get("id").getAsString();
  }

  /**
   * Remove a workspace. Does nothing if the workspace does not exist.
   *
   * @param workspaceName the workspace name
   * @throws IOException if an I/O error occurs
   */
  public void removeWorkspace(String workspaceName) throws IOException {
    requireNonNull(workspaceName, "workspaceName must not be null");
    String uuid = getWorkspaceUuid(workspaceName);
    if (uuid == null) {
      return;
    }
    httpDelete(this.baseUrl + "/workspaces/" + uuid);
  }

  //
  // Node functions
  //

  /**
   * Create a project node in a workspace.
   *
   * @param workspaceUuid the workspace UUID
   * @param projectName the project name
   * @return the new project node UUID
   * @throws IOException if an I/O error occurs
   */
  public String createProjectNode(String workspaceUuid, String projectName) throws IOException {
    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(projectName, "projectName must not be null");
    JsonObject payload = new JsonObject();
    payload.addProperty("title", projectName);
    payload.addProperty("description", "Project " + projectName);
    String body = httpPost(this.baseUrl + "/nodes", workspaceUuid, payload);
    return this.gson.fromJson(body, JsonObject.class).get("id").getAsString();
  }

  /**
   * Get the UUID of a project by name.
   *
   * @param workspaceUuid the workspace UUID
   * @param projectName the project name to search
   * @return the project UUID
   * @throws IOException if an I/O error occurs or the project is not found
   */
  public String getProjectUuid(String workspaceUuid, String projectName) throws IOException {
    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(projectName, "projectName must not be null");
    String body = httpGet(this.baseUrl + "/nodes?search=" + projectName, workspaceUuid);
    JsonArray results = this.gson.fromJson(body, JsonObject.class).getAsJsonArray("results");
    if (!results.isEmpty()) {
      return results.get(0).getAsJsonObject().get("id").getAsString();
    }
    throw new IOException("Project " + projectName + " not found");
  }

  //
  // Sample functions
  //

  /**
   * Create a sample inside a project node.
   *
   * @param workspaceUuid the workspace UUID
   * @param projectUuid the project node UUID
   * @param sampleName the sample alias
   * @param sampleDescription the sample description (title)
   * @return the new sample UUID
   * @throws IOException if an I/O error occurs
   */
  public String createSample(
      String workspaceUuid, String projectUuid, String sampleName, String sampleDescription)
      throws IOException {
    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(projectUuid, "projectUuid must not be null");
    requireNonNull(sampleName, "sampleName must not be null");
    JsonObject payload = new JsonObject();
    payload.addProperty("alias", sampleName);
    payload.addProperty("title", sampleDescription);
    payload.addProperty("node", projectUuid);
    String body =
        httpPost(this.baseUrl + "/nodes/" + projectUuid + "/samples", workspaceUuid, payload);
    return this.gson.fromJson(body, JsonObject.class).get("id").getAsString();
  }

  /**
   * Get the list of sample UUIDs for a project.
   *
   * @param workspaceUuid the workspace UUID
   * @param projectUuid the project node UUID
   * @return the list of sample UUIDs (may be empty)
   * @throws IOException if an I/O error occurs
   */
  public List<String> getSamples(String workspaceUuid, String projectUuid) throws IOException {
    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(projectUuid, "projectUuid must not be null");
    String body = httpGet(this.baseUrl + "/nodes/" + projectUuid + "/samples", workspaceUuid);
    JsonArray results = this.gson.fromJson(body, JsonObject.class).getAsJsonArray("results");
    List<String> ids = new ArrayList<>();
    for (JsonElement elem : results) {
      ids.add(elem.getAsJsonObject().get("id").getAsString());
    }
    return ids;
  }

  /**
   * Get the raw JSON metadata of a sample.
   *
   * @param workspaceUuid the workspace UUID
   * @param projectUuid the project node UUID
   * @param sampleUuid the sample UUID
   * @return the raw JSON response body
   * @throws IOException if an I/O error occurs
   */
  public String getSampleMetadata(String workspaceUuid, String projectUuid, String sampleUuid)
      throws IOException {
    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(projectUuid, "projectUuid must not be null");
    requireNonNull(sampleUuid, "sampleUuid must not be null");
    return httpGet(
        this.baseUrl + "/nodes/" + projectUuid + "/samples/" + sampleUuid + "/metadata",
        workspaceUuid);
  }

  /**
   * Set a metadata field on a sample.
   *
   * @param workspaceUuid the workspace UUID
   * @param projectUuid the project node UUID
   * @param sampleUuid the sample UUID
   * @param metadataUuid the plugin field UUID
   * @param value the value to set
   * @throws IOException if an I/O error occurs
   */
  public void setSampleMetadata(
      String workspaceUuid,
      String projectUuid,
      String sampleUuid,
      String metadataUuid,
      String value)
      throws IOException {
    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(projectUuid, "projectUuid must not be null");
    requireNonNull(sampleUuid, "sampleUuid must not be null");
    requireNonNull(metadataUuid, "metadataUuid must not be null");
    JsonObject payload = new JsonObject();
    payload.addProperty("plugin_field", metadataUuid);
    payload.addProperty("value", value != null ? value : "");
    httpPost(
        this.baseUrl + "/nodes/" + projectUuid + "/samples/" + sampleUuid + "/metadata",
        workspaceUuid,
        payload);
  }

  /**
   * Add an empty metadata entry on a sample.
   *
   * @param workspaceUuid the workspace UUID
   * @param projectUuid the project node UUID
   * @param sampleUuid the sample UUID
   * @param metadataUuid the plugin field UUID
   * @throws IOException if an I/O error occurs
   */
  public void addEmptySampleMetadata(
      String workspaceUuid, String projectUuid, String sampleUuid, String metadataUuid)
      throws IOException {
    setSampleMetadata(workspaceUuid, projectUuid, sampleUuid, metadataUuid, "");
  }

  //
  // Connections
  //

  /**
   * List available connections in a workspace.
   *
   * @param workspaceUuid the workspace UUID
   * @return the raw JSON response body
   * @throws IOException if an I/O error occurs
   */
  public String listConnections(String workspaceUuid) throws IOException {
    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    return httpGet(this.baseUrl + "/connections", workspaceUuid);
  }

  //
  // Project metadata
  //

  /**
   * Get the metadata fields available in a plugin group for projects.
   *
   * @param workspaceUuid the workspace UUID
   * @param pluginName the plugin name (e.g. {@code "ena"})
   * @param group the parent group name (e.g. {@code "study"})
   * @return a map from field slug to {@link MetadataFieldInfo}
   * @throws IOException if an I/O error occurs
   */
  public Map<String, MetadataFieldInfo> getMetadataCategory(
      String workspaceUuid, String pluginName, String group) throws IOException {
    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(pluginName, "pluginName must not be null");
    requireNonNull(group, "group must not be null");
    String body =
        httpGet(
            this.baseUrl + "/plugins/" + pluginName + "/groups?include_fields=true&parent=" + group,
            workspaceUuid);
    JsonArray arr = this.gson.fromJson(body, JsonArray.class);
    Map<String, MetadataFieldInfo> result = new LinkedHashMap<>();
    for (JsonElement e1 : arr) {
      JsonArray fields = e1.getAsJsonObject().getAsJsonArray("plugin_metadata_fields");
      if (fields != null) {
        for (JsonElement e2 : fields) {
          JsonObject field = e2.getAsJsonObject();
          String slug = field.get("slug").getAsString();
          String id = field.get("id").getAsString();
          boolean mandatory = "mandatory".equals(field.get("level").getAsString());
          result.put(slug, new MetadataFieldInfo(id, mandatory));
        }
      }
    }
    return result;
  }

  /**
   * Set a metadata field on a project node.
   *
   * @param workspaceUuid the workspace UUID
   * @param projectUuid the project node UUID
   * @param metadataUuid the plugin field UUID
   * @param value the value to set
   * @throws IOException if an I/O error occurs
   */
  public void setProjectMetadata(
      String workspaceUuid, String projectUuid, String metadataUuid, String value)
      throws IOException {
    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(projectUuid, "projectUuid must not be null");
    requireNonNull(metadataUuid, "metadataUuid must not be null");
    JsonObject payload = new JsonObject();
    payload.addProperty("plugin_field", metadataUuid);
    payload.addProperty("value", value != null ? value : "");
    httpPost(this.baseUrl + "/nodes/" + projectUuid + "/metadata", workspaceUuid, payload);
  }

  /**
   * Add an empty metadata entry on a project node.
   *
   * @param workspaceUuid the workspace UUID
   * @param projectUuid the project node UUID
   * @param metadataUuid the plugin field UUID
   * @throws IOException if an I/O error occurs
   */
  public void addEmptyProjectMetadata(String workspaceUuid, String projectUuid, String metadataUuid)
      throws IOException {
    setProjectMetadata(workspaceUuid, projectUuid, metadataUuid, "");
  }

  //
  // Sample metadata group
  //

  /**
   * Get the metadata fields defined by a plugin for a given sample metadata group.
   *
   * @param workspaceUuid the workspace UUID
   * @param pluginName the plugin name (e.g. {@code "ena"})
   * @param group the group name (e.g. {@code "sample__erc000011"})
   * @return a map from field slug to {@link MetadataFieldInfo}
   * @throws IOException if an I/O error occurs
   */
  public Map<String, MetadataFieldInfo> getMetadataGroup(
      String workspaceUuid, String pluginName, String group) throws IOException {
    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(pluginName, "pluginName must not be null");
    requireNonNull(group, "group must not be null");
    String body =
        httpGet(
            this.baseUrl + "/plugins/" + pluginName + "/metadata_mapping?group=" + group,
            workspaceUuid);
    JsonArray arr = this.gson.fromJson(body, JsonArray.class);
    Map<String, MetadataFieldInfo> result = new LinkedHashMap<>();
    for (JsonElement e1 : arr) {
      JsonObject d = e1.getAsJsonObject().getAsJsonObject("plugin_metadata_field");
      if (d != null) {
        String slug = d.get("slug").getAsString();
        String id = d.get("id").getAsString();
        boolean mandatory = "mandatory".equals(d.get("level").getAsString());
        result.put(slug, new MetadataFieldInfo(id, mandatory));
      }
    }
    return result;
  }

  //
  // Constructor
  //

  /**
   * Create a new PfMadbot client.
   *
   * @param baseUrl the base URL of the Madbot API (e.g. {@code
   *     "https://api.madbot.france-bioinformatique.fr/api"})
   * @param bearer the bearer token for authentication
   */
  public PfMadbot(String baseUrl, String bearer) {
    requireNonNull(baseUrl, "baseUrl must not be null");
    requireNonNull(bearer, "bearer must not be null");
    this.baseUrl = baseUrl;
    this.bearer = bearer;
    this.httpClient = HttpClient.newHttpClient();
    this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
  }
}
