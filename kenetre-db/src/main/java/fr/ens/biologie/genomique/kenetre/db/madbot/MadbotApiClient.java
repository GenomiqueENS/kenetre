package fr.ens.biologie.genomique.kenetre.db.madbot;

import static java.util.Objects.requireNonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.ens.biologie.genomique.kenetre.db.ApiClient;
import java.io.IOException;
import java.util.*;

public class MadbotApiClient extends ApiClient {

  private static final String WORKSPACE_HEADER_NAME = "x-workspace";

  /** Holds the UUID and mandatory flag for a metadata field. */
  public record MetadataFieldInfo(UUID uuid, boolean mandatory) {}

  private final String sftpServer;
  private final String sftpLogin;
  private final String sftpPassword;
  private final String sftpBasePath;

  /**
   * Get SFTP server base path.
   *
   * @return the base path on the SFTP server
   */
  public String getSftpBasePath() {
    return this.sftpBasePath;
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
  public UUID getWorkspaceUuid(String workspaceName) throws IOException {
    requireNonNull(workspaceName, "workspaceName must not be null");
    String responseBody = get("workspaces", null, null, null);

    JsonArray results =
        this.gson.fromJson(responseBody, JsonObject.class).getAsJsonArray("results");
    for (JsonElement elem : results) {
      JsonObject ws = elem.getAsJsonObject();
      if (workspaceName.equals(ws.get("name").getAsString())) {
        return UUID.fromString(ws.get("id").getAsString());
      }
    }
    return null;
  }

  /**
   * Check if a workspace with the given name exists.
   *
   * @param workspaceName the workspace name
   * @return {@code true} if the workspace exists, {@code false} otherwise
   */
  public boolean isWorkspace(String workspaceName) {
    try {
      return getWorkspaceUuid(workspaceName) != null;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Create a workspace, or return the UUID of the existing one if it already exists.
   *
   * @param workspaceName the workspace name
   * @return the workspace UUID
   * @throws IOException if an I/O error occurs
   */
  public UUID createWorkspace(String workspaceName) throws IOException {
    requireNonNull(workspaceName, "workspaceName must not be null");

    UUID existingUuid = getWorkspaceUuid(workspaceName);
    if (existingUuid != null) {
      return existingUuid;
    }

    String responseBody =
        this.post(
            "workspaces",
            Map.of("name", workspaceName, "description", "workspace " + workspaceName),
            null);
    return UUID.fromString(
        this.gson.fromJson(responseBody, JsonObject.class).get("id").getAsString());
  }

  /**
   * Remove a workspace. Does nothing if the workspace does not exist.
   *
   * @param workspaceName the workspace name
   * @throws IOException if an I/O error occurs
   */
  public String removeWorkspace(String workspaceName) throws IOException {
    requireNonNull(workspaceName, "workspaceName must not be null");

    UUID uuid = getWorkspaceUuid(workspaceName);
    if (uuid == null) {
      throw new IOException("Workspace " + workspaceName + " does not exist");
    }

    return this.delete("workspaces", uuid.toString(), null, null);
  }

  //
  // Node methods
  //

  /**
   * Create a project node in a workspace.
   *
   * @param workspaceUuid the workspace UUID
   * @param projectName the project name
   * @return the new project node UUID
   * @throws IOException if an I/O error occurs
   */
  public UUID createProjectNode(UUID workspaceUuid, String projectName) throws IOException {

    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(projectName, "projectName must not be null");

    var responseBody =
        this.post(
            "nodes",
            Map.of("title", projectName, "description", "Project " + projectName),
            Map.of(WORKSPACE_HEADER_NAME, workspaceUuid.toString()));

    return UUID.fromString(
        this.gson.fromJson(responseBody, JsonObject.class).get("id").getAsString());
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

    var responseBody =
        this.get(
            "nodes",
            null,
            Map.of("search", projectName),
            Map.of(WORKSPACE_HEADER_NAME, workspaceUuid));

    JsonArray results =
        this.gson.fromJson(responseBody, JsonObject.class).getAsJsonArray("results");
    if (!results.isEmpty()) {
      return results.get(0).getAsJsonObject().get("id").getAsString();
    }
    throw new IOException("Project " + projectName + " not found");
  }

  //
  // Sample methods
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
  public UUID createSample(
      UUID workspaceUuid, UUID projectUuid, String sampleName, String sampleDescription)
      throws IOException {

    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(projectUuid, "projectUuid must not be null");
    requireNonNull(sampleName, "sampleName must not be null");

    var responseBody =
        this.post(
            "nodes/" + projectUuid + "/samples",
            Map.of("alias", sampleName, "title", sampleDescription, "node", projectUuid),
            Map.of(WORKSPACE_HEADER_NAME, workspaceUuid.toString()));

    return UUID.fromString(
        this.gson.fromJson(responseBody, JsonObject.class).get("id").getAsString());
  }

  /**
   * Get the list of sample UUIDs for a project.
   *
   * @param workspaceUuid the workspace UUID
   * @param projectUuid the project node UUID
   * @return the list of sample UUIDs
   * @throws IOException if an I/O error occurs
   */
  public List<String> getSamples(String workspaceUuid, String projectUuid) throws IOException {

    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(projectUuid, "projectUuid must not be null");

    var responseBody =
        this.get(
            "nodes/" + projectUuid + "/samples",
            null,
            Map.of(WORKSPACE_HEADER_NAME, workspaceUuid));

    JsonArray results =
        this.gson.fromJson(responseBody, JsonObject.class).getAsJsonArray("results");
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

    var responseBody =
        this.get(
            "nodes/" + projectUuid + "/samples" + sampleUuid + "/metadata",
            null,
            null,
            Map.of(WORKSPACE_HEADER_NAME, workspaceUuid));

    return responseBody;
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
  public String setSampleMetadata(
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

    return this.post(
        "nodes/" + projectUuid + "/samples/" + sampleUuid + "/metadata",
        Map.of("plugin_field", metadataUuid, "value", value != null ? value : ""),
        Map.of(WORKSPACE_HEADER_NAME, workspaceUuid));
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
  public String setSampleMetadata(
      UUID workspaceUuid, UUID projectUuid, UUID sampleUuid, UUID metadataUuid, Object value)
      throws IOException {

    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(projectUuid, "projectUuid must not be null");
    requireNonNull(sampleUuid, "sampleUuid must not be null");
    requireNonNull(metadataUuid, "metadataUuid must not be null");

    return this.post(
        "nodes/" + projectUuid + "/samples/" + sampleUuid + "/metadata",
        Map.of("plugin_field", metadataUuid, "value", value != null ? value : ""),
        Map.of(WORKSPACE_HEADER_NAME, workspaceUuid.toString()));
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
      UUID workspaceUuid, UUID projectUuid, UUID sampleUuid, UUID metadataUuid) throws IOException {
    setSampleMetadata(workspaceUuid, projectUuid, sampleUuid, metadataUuid, "");
  }

  //
  // Plugin methods
  //

  /**
   * Get the slug of a plugin by name.
   *
   * @param pluginName the plugin name (e.g. {@code "ENA"})
   * @return the plugin slug (e.g. {@code "ena"}), or {@code null} if not found
   * @throws IOException if an I/O error occurs
   */
  public String getPluginSlug(String pluginName) throws IOException {

    requireNonNull(pluginName, "pluginName must not be null");

    String responseBody = this.get("plugins", null, null, null);

    JsonArray results =
        this.gson.fromJson(responseBody, JsonObject.class).getAsJsonArray("results");

    String result = null;

    for (JsonElement elem : results) {
      if (pluginName.equalsIgnoreCase(elem.getAsJsonObject().get("name").getAsString())) {
        return elem.getAsJsonObject().get("slug").getAsString();
      }
    }

    return null;
  }

  /**
   * Get the JSON schema of a plugin.
   *
   * @param source the plugin source (e.g. {@code "ENA"})
   * @param slug the plugin slug (e.g. {@code "ena__raw_read__library_layout"})
   * @return the raw JSON response body containing the plugin schema
   * @throws IOException if an I/O error occurs
   */
  public String getPluginSchema(String source, String slug) throws IOException {

    requireNonNull(source, "source must not be null");
    requireNonNull(slug, "slug must not be null");

    return this.get("schemas/plugins/" + source.toLowerCase(Locale.ROOT), slug, null, null);
  }

  public String getPluginInfo(String name) throws IOException {

    requireNonNull(name, "name must not be null");

    return this.get("plugins/" + name, null, null, null);
  }

  /**
   * Get the list of available plugin slugs.
   *
   * @return a list of plugin slugs
   * @throws IOException if an I/O error occurs
   */
  public List<String> getPluginNames() throws IOException {

    String responseBody = this.get("plugins", null, null, null);

    JsonArray results =
        this.gson.fromJson(responseBody, JsonObject.class).getAsJsonArray("results");

    List<String> plugins = new ArrayList<>();

    for (JsonElement elem : results) {
      plugins.add(elem.getAsJsonObject().get("name").getAsString());

      if ("SSHFS".equals(elem.getAsJsonObject().get("name").getAsString())) {
        System.out.println(elem.getAsJsonObject());
      }
    }

    return plugins;
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

    return this.get("connections", null, null, Map.of(WORKSPACE_HEADER_NAME, workspaceUuid));
  }

  /**
   * Create an SSHFS connection in a workspace.
   *
   * @param workspaceUuid the workspace UUID
   * @return the UUID of the created connection
   * @throws IOException if an I/O error occurs
   */
  public UUID createSshfsConnection(UUID workspaceUuid) throws IOException {
    return createSshfsConnection(workspaceUuid, this.sftpServer, this.sftpLogin, this.sftpPassword);
  }

  /**
   * Create an SSHFS connection in a workspace.
   *
   * @param workspaceUuid the workspace UUID
   * @param host the SSHFS host
   * @param username the SSHFS username
   * @param password the SSHFS password
   * @return the UUID of the created connection
   * @throws IOException if an I/O error occurs
   */
  public UUID createSshfsConnection(
      UUID workspaceUuid, String host, String username, String password) throws IOException {

    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(username, "username must not be null");
    requireNonNull(password, "credentials must not be null");

    var payload =
        Map.of(
            "plugin",
            "madbot_sshfs.plugins.SSHFSPlugin",
            "shared_params",
            Map.of("host", host),
            "private_params",
            Map.of("username", username, "credential", Map.of("password", password)));

    var responseBody =
        this.post("connections", payload, Map.of(WORKSPACE_HEADER_NAME, workspaceUuid.toString()));

    return UUID.fromString(
        this.gson.fromJson(responseBody, JsonObject.class).get("id").getAsString());
  }

  public UUID getExistingSshfsConnections(UUID workspaceUuid) throws IOException {
    return getExistingSshfsConnections(workspaceUuid, this.sftpServer);
  }

  public UUID getExistingSshfsConnections(UUID workspaceUuid, String host) throws IOException {

    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(host, "host must not be null");

    var responseBody =
        this.get(
            "connections", null, null, Map.of(WORKSPACE_HEADER_NAME, workspaceUuid.toString()));

    var array = this.gson.fromJson(responseBody, JsonObject.class).get("results").getAsJsonArray();
    for (var element : array) {

      var jsonObject = element.getAsJsonObject();
      var name = jsonObject.get("name").getAsString();
      if ("SSHFS".equals(name)) {
        return UUID.fromString(jsonObject.get("id").getAsString());
      }
    }
    return null;
  }

  //
  //  Data
  //

  /**
   * Create a data object in a workspace.
   *
   * @param workspaceUuid workspace UUID
   * @param connection connection UUID
   * @param externalId path of the data on the remote connection (e.g. path/to/file.txt)
   * @return the UUID of the created data object
   * @throws IOException if an I/O error occurs
   */
  public UUID createData(UUID workspaceUuid, UUID connection, String externalId)
      throws IOException {

    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(connection, "connection must not be null");
    requireNonNull(externalId, "externalId must not be null");

    var responseBody =
        this.post(
            "data",
            Map.of("connection", connection.toString(), "external_id", externalId),
            Map.of(WORKSPACE_HEADER_NAME, workspaceUuid.toString()));

    return UUID.fromString(
        this.gson.fromJson(responseBody, JsonObject.class).get("id").getAsString());
  }

  /**
   * Create a data link between a sample and a data object.
   *
   * @param workspaceUuid the workspace UUID
   * @param project the project UUID
   * @param sample the sample UUID
   * @param data the data UUID
   * @return the UUID of the created data link
   * @throws IOException if an I/O error occurs
   */
  public UUID createDataLink(UUID workspaceUuid, UUID project, UUID sample, UUID data)
      throws IOException {

    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(sample, "sample must not be null");
    requireNonNull(data, "data must not be null");

    var responseBody =
        this.post(
            "nodes/" + project.toString() + "/datalinks",
            Map.of("node", sample.toString(), "data", data.toString()),
            Map.of(WORKSPACE_HEADER_NAME, workspaceUuid.toString()));

    return UUID.fromString(
        this.gson.fromJson(responseBody, JsonObject.class).get("id").getAsString());
  }

  /**
   * Create bound sample.
   *
   * @param workspaceUuid the workspace UUID
   * @param sample the sample UUID
   * @param data the data UUID
   * @param dataLink the datalink UUID
   * @return the UUID of the created bound
   * @throws IOException if an I/O error occurs
   */
  public UUID createBoundSample(UUID workspaceUuid, UUID sample, UUID data, UUID dataLink)
      throws IOException {

    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(sample, "sample must not be null");
    requireNonNull(data, "data must not be null");
    requireNonNull(dataLink, "dataLink must not be null");

    var responseBody =
        this.post(
            "data/" + data.toString() + "/bound_samples",
            Map.of(
                "sample",
                sample.toString(),
                "data",
                data.toString(),
                "datalink",
                dataLink.toString()),
            Map.of(WORKSPACE_HEADER_NAME, workspaceUuid.toString()));

    return UUID.fromString(
        this.gson.fromJson(responseBody, JsonObject.class).get("id").getAsString());
  }

  /**
   * Create association.
   *
   * @param workspaceUuid the workspace UUID
   * @param dataList a list with data UUIDs
   * @return the UUID of the created association
   * @throws IOException if an I/O error occurs
   */
  public UUID createSampleAssociation(UUID workspaceUuid, List<UUID> dataList) throws IOException {

    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(dataList, "dataList must not be null");

    // Throw exception if there is less than 2 data
    if (dataList.size() < 2) {
      throw new IllegalArgumentException("dataList must have at least 2 elements");
    }

    UUID firstUuid = dataList.get(0);
    List<String> otherUuid = new ArrayList<>();
    for (UUID uuid : dataList.subList(1, dataList.size())) {
      otherUuid.add(uuid.toString());
    }

    var responseBody =
        this.post(
            "data/" + firstUuid.toString() + "/associations",
            Map.of("data_objects", otherUuid),
            Map.of(WORKSPACE_HEADER_NAME, workspaceUuid.toString()));

    return UUID.fromString(
        this.gson.fromJson(responseBody, JsonObject.class).get("id").getAsString());
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
   * @return a map from field slug to {@link MadbotApiClient.MetadataFieldInfo}
   * @throws IOException if an I/O error occurs
   */
  public Map<String, MetadataFieldInfo> getMetadataCategory(
      UUID workspaceUuid, String pluginName, String group) throws IOException {

    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(pluginName, "pluginName must not be null");
    requireNonNull(group, "group must not be null");

    var responseBody =
        this.get(
            "plugins/" + pluginName + "/groups",
            null,
            Map.of("include_fields", "true", "parent", group),
            Map.of(WORKSPACE_HEADER_NAME, workspaceUuid.toString()));

    JsonArray arr = this.gson.fromJson(responseBody, JsonArray.class);

    Map<String, MetadataFieldInfo> result = new LinkedHashMap<>();
    for (JsonElement e1 : arr) {
      JsonArray fields = e1.getAsJsonObject().getAsJsonArray("plugin_metadata_fields");
      if (fields != null) {
        for (JsonElement e2 : fields) {
          JsonObject field = e2.getAsJsonObject();
          String slug = field.get("slug").getAsString();
          UUID id = UUID.fromString(field.get("id").getAsString());
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
  public String setProjectMetadata(
      UUID workspaceUuid, UUID projectUuid, UUID metadataUuid, Object value) throws IOException {

    // TODO

    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(projectUuid, "projectUuid must not be null");
    requireNonNull(metadataUuid, "metadataUuid must not be null");

    return this.post(
        "nodes/" + projectUuid + "/metadata",
        Map.of("plugin_field", metadataUuid, "value", value != null ? value : ""),
        Map.of(WORKSPACE_HEADER_NAME, workspaceUuid.toString()));
  }

  /**
   * Add an empty metadata entry on a project node.
   *
   * @param workspaceUuid the workspace UUID
   * @param projectUuid the project node UUID
   * @param metadataUuid the plugin field UUID
   * @throws IOException if an I/O error occurs
   */
  public void addEmptyProjectMetadata(UUID workspaceUuid, UUID projectUuid, UUID metadataUuid)
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
   * @return a map from field slug to {@link MadbotApiClient.MetadataFieldInfo}
   * @throws IOException if an I/O error occurs
   */
  public Map<String, MetadataFieldInfo> getMetadataGroup(
      UUID workspaceUuid, String pluginName, String group) throws IOException {

    requireNonNull(workspaceUuid, "workspaceUuid must not be null");
    requireNonNull(pluginName, "pluginName must not be null");
    requireNonNull(group, "group must not be null");

    var responseBody =
        this.get(
            "plugins/" + pluginName + "/metadata_mapping",
            null,
            Map.of("group", workspaceUuid.toString()),
            Map.of(WORKSPACE_HEADER_NAME, workspaceUuid.toString()));

    JsonArray arr = this.gson.fromJson(responseBody, JsonArray.class);
    Map<String, MetadataFieldInfo> result = new LinkedHashMap<>();
    for (JsonElement e1 : arr) {
      JsonObject d = e1.getAsJsonObject().getAsJsonObject("plugin_metadata_field");
      if (d != null) {
        String slug = d.get("slug").getAsString();
        UUID id = UUID.fromString(d.get("id").getAsString());
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
   * Create a new MadbotApiClient.
   *
   * @param credentials map containing url, domain_kc, realm, username, password
   * @param debugApiRequests whether to log debug information for API requests
   */
  public MadbotApiClient(Map<String, String> credentials, boolean debugApiRequests) {
    super(credentials, "madbot", false, debugApiRequests, false, true);

    this.sftpServer = credentials.get("sftp.server");
    this.sftpLogin = credentials.get("sftp.login");
    this.sftpPassword = credentials.get("sftp.password");
    this.sftpBasePath = credentials.get("sftp.base.path");
  }
}
