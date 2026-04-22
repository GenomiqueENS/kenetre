package fr.ens.biologie.genomique.kenetre.db;

import static java.util.Objects.requireNonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Common REST API Client. Manages bearer token authentication and CRUD operations.
 *
 * @since 0.41
 * @author Laurent Jourdren
 */
public class ApiClient {

  private final String apiUrl;
  private final boolean keycloak;
  private final String keycloakDomain;
  private final String realm;
  private final String clientId;
  private final String clientSecret;
  private final boolean useOuiNonInJson;
  private final boolean debugApiRequests;

  private String bearer;
  private final HttpClient httpClient;
  protected final Gson gson;

  private static String requireField(Map<String, String> map, String key) {
    String value = map.get(key);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Missing required credential field: " + key);
    }
    return value;
  }

  //
  // Bearer token
  //

  /**
   * Obtain or return the cached bearer token.
   *
   * @return the bearer token string
   * @throws IOException if an I/O error occurs
   */
  public String getBearerToken() throws IOException {
    if (this.bearer != null) {
      return this.bearer;
    }

    String url = this.keycloakDomain + "/realms/" + this.realm + "/protocol/openid-connect/token";

    String formBody =
        "grant_type=client_credentials&client_id="
            + URLEncoder.encode(this.clientId, StandardCharsets.UTF_8)
            + "&client_secret="
            + URLEncoder.encode(this.clientSecret, StandardCharsets.UTF_8);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formBody))
            .build();

    try {
      HttpResponse<String> response =
          this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      checkHttpStatusCode(response, 200, url, "POST", formBody);
      JsonObject json = this.gson.fromJson(response.body(), JsonObject.class);
      this.bearer = json.get("access_token").getAsString();
      return this.bearer;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while obtaining bearer token", e);
    }
  }

  //
  // HTTP methods
  //

  /**
   * Perform a GET request.
   *
   * @param route the API route
   * @return the response body as a string
   * @throws IOException if an I/O or HTTP error occurs
   */
  public String get(String route) throws IOException {
    return get(route, null, null);
  }

  /**
   * Perform a GET request.
   *
   * @param route the API route
   * @param entryId the entry identifier
   * @return the response body as a string
   * @throws IOException if an I/O or HTTP error occurs
   */
  public String get(String route, String entryId) throws IOException {
    return get(route, entryId, null);
  }

  /**
   * Perform a GET request.
   *
   * @param route the API route
   * @param params query parameters
   * @return the response body as a string
   * @throws IOException if an I/O or HTTP error occurs
   */
  public String get(String route, Map<String, String> params) throws IOException {
    return get(route, null, params);
  }

  /**
   * Perform a GET request.
   *
   * @param route the API route
   * @param entryId the entry identifier
   * @param params query parameters
   * @return the response body as a string
   * @throws IOException if an I/O or HTTP error occurs
   */
  public String get(String route, String entryId, Map<String, String> params) throws IOException {

    String url = createApiUrl(route, entryId);

    if (params != null && !params.isEmpty()) {
      String queryString =
          params.entrySet().stream()
              .map(
                  e ->
                      URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                          + "="
                          + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
              .collect(Collectors.joining("&"));
      url = url + "?" + queryString;
    }

    // Create HTTP request
    HttpRequest.Builder builder =
        HttpRequest.newBuilder().uri(URI.create(url)).header("Accept", "application/json").GET();
    if (this.keycloak) {
      builder.header("Authorization", "Bearer " + getBearerToken());
    }
    HttpRequest request = builder.build();

    try {
      HttpResponse<String> response =
          this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      checkHttpStatusCode(response, 200, url, "GET", params != null ? params.toString() : null);
      return response.body();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted during GET request", e);
    }
  }

  /**
   * Perform a POST request.
   *
   * @param route the API route
   * @param data the data map to send
   * @return the response body as a string
   * @throws IOException if an I/O or HTTP error occurs
   */
  public String post(String route, Map<String, Object> data) throws IOException {
    if (this.useOuiNonInJson) {
      replaceBoolsWithStrings(data);
    }
    String url = createApiUrl(route, null);
    String formBody = encodeFormData(data);

    // Create HTTP request
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formBody));
    if (this.keycloak) {
      builder.header("Authorization", "Bearer " + getBearerToken());
    }
    HttpRequest request = builder.build();

    try {
      HttpResponse<String> response =
          this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      checkHttpStatusCode(response, 200, url, "POST", formBody);
      return response.body();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted during POST request", e);
    }
  }

  /**
   * Perform a PUT request.
   *
   * @param route the API route
   * @param entryId the entry identifier
   * @param data the data map to send
   * @return the response body as a string
   * @throws IOException if an I/O or HTTP error occurs
   */
  public String put(String route, String entryId, Map<String, Object> data) throws IOException {
    if (this.useOuiNonInJson) {
      replaceBoolsWithStrings(data);
    }
    String url = createApiUrl(route, entryId);
    String formBody = encodeFormData(data);

    // Create HTTP request
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .PUT(HttpRequest.BodyPublishers.ofString(formBody));

    if (this.keycloak) {
      builder.header("Authorization", "Bearer " + getBearerToken());
    }
    HttpRequest request = builder.build();

    try {
      HttpResponse<String> response =
          this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      checkHttpStatusCode(response, 200, url, "PUT", formBody);
      return response.body();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted during PUT request", e);
    }
  }

  /**
   * Perform a DELETE request.
   *
   * @param route the API route
   * @param data the data map to send
   * @return the response body as a string
   * @throws IOException if an I/O or HTTP error occurs
   */
  public String delete(String route, Map<String, Object> data) throws IOException {
    String url = createApiUrl(route, null);
    String formBody = encodeFormData(data);

    // Create HTTP request
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .method("DELETE", HttpRequest.BodyPublishers.ofString(formBody))
            .header("Content-Type", "application/x-www-form-urlencoded");
    if (this.keycloak) {
      builder.header("Authorization", "Bearer " + getBearerToken());
    }
    HttpRequest request = builder.build();

    try {
      HttpResponse<String> response =
          this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      checkHttpStatusCode(response, 200, url, "DELETE", null);
      return response.body();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted during DELETE request", e);
    }
  }

  //
  // Utility methods
  //

  private String createApiUrl(String route) {
    return createApiUrl(route, null);
  }

  private String createApiUrl(String route, String entryId) {
    if (entryId == null) {
      return this.apiUrl + "/" + route + "/";
    }
    return this.apiUrl + "/" + route + "/" + entryId;
  }

  private void checkHttpStatusCode(
      HttpResponse<String> response, int expectedCode, String url, String method, String data)
      throws IOException {

    int status = response.statusCode();
    String body = response.body();

    if (this.debugApiRequests
        || ((status < 400 || status >= 500) && (body == null || body.isEmpty()))) {
      System.err.println("Empty " + method + " server response!");
      System.err.println(method + " request URL: " + url);
      System.err.println(method + " request data: " + data);
      System.err.println(method + " response status_code: " + status);
      System.err.println(method + " response headers: " + response.headers().map());
      System.err.println(method + " response content: " + body);
      System.err.println();
    }

    switch (status) {
      case 400 -> throw new IOException("HTTP error 400: Bad request.");
      case 401 -> throw new IOException("HTTP error 401: Unauthorized (token expired or invalid).");
      case 403 -> throw new IOException("HTTP error 403: Forbidden access (bad or missing token).");
      case 404 -> throw new IOException("HTTP error 404: Not found.");
      case 405 ->
          throw new IOException("HTTP error 405: Method Not Allowed (FG operation not allowed).");
      default -> {
        if (status != expectedCode) {
          throw new IOException(
              "ERROR: expected HTTP " + expectedCode + " status code, got " + status + ".");
        }
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        if (!contentType.startsWith("application/json")) {
          System.err.println("response.headers: " + response.headers().map());
          System.err.println("response.text: " + body);
          throw new IOException(
              "ERROR: Invalid response content type (application/json was expected).");
        }
      }
    }
  }

  private static String encodeFormData(Map<String, Object> data) {
    if (data == null || data.isEmpty()) {
      return "";
    }
    return data.entrySet().stream()
        .map(
            e ->
                URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                    + "="
                    + URLEncoder.encode(
                        e.getValue() != null ? String.valueOf(e.getValue()) : "",
                        StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));
  }

  /**
   * Replace boolean values with "oui"/"non" strings in a map (recursive).
   *
   * @param data the map to modify in place
   */
  @SuppressWarnings("unchecked")
  private static void replaceBoolsWithStrings(Map<String, Object> data) {
    if (data == null) {
      return;
    }
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      Object v = entry.getValue();
      if (v instanceof Boolean b) {
        entry.setValue(b ? "oui" : "non");
      } else if (v instanceof Map) {
        replaceBoolsWithStrings((Map<String, Object>) v);
      } else if (v instanceof List) {
        replaceBoolsWithStringsList((List<Object>) v);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static void replaceBoolsWithStringsList(List<Object> list) {
    if (list == null) {
      return;
    }
    for (int i = 0; i < list.size(); i++) {
      Object item = list.get(i);
      if (item instanceof Boolean b) {
        list.set(i, b ? "oui" : "non");
      } else if (item instanceof Map) {
        replaceBoolsWithStrings((Map<String, Object>) item);
      } else if (item instanceof List) {
        replaceBoolsWithStringsList((List<Object>) item);
      }
    }
  }

  /**
   * Get the Gson instance used by this client.
   *
   * @return the Gson instance
   */
  public Gson getGson() {
    return this.gson;
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
  public ApiClient(
      Map<String, String> credentials,
      String credentialPrefix,
      boolean useOuiNonInJson,
      boolean debugApiRequests) {

    requireNonNull(credentials, "credentials must not be null");
    requireNonNull(credentialPrefix, "credentialPrefix must not be null");

    this.apiUrl = requireField(credentials, credentialPrefix + ".url");

    this.keycloak = Boolean.parseBoolean(requireField(credentials, credentialPrefix + ".keycloak"));
    if (this.keycloak) {
      this.keycloakDomain = requireField(credentials, credentialPrefix + ".domain.kc");
      this.realm = requireField(credentials, credentialPrefix + ".realm");
      this.clientId = requireField(credentials, credentialPrefix + ".username");
      this.clientSecret = requireField(credentials, credentialPrefix + ".password");
    } else {
      this.keycloakDomain = null;
      this.realm = null;
      this.clientId = null;
      this.clientSecret = null;
    }

    this.useOuiNonInJson = useOuiNonInJson;
    this.debugApiRequests = debugApiRequests;
    this.httpClient = HttpClient.newHttpClient();
    this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
  }
}
