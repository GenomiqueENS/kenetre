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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Common REST API Client. Manages bearer token authentication and CRUD operations.
 *
 * @since 0.41
 * @author Laurent Jourdren
 */
public class ApiClient {

  private final String apiUrl;
  private final boolean trailSlash;
  private final boolean keycloak;
  private final String keycloakDomain;
  private final String realm;
  private final String clientId;
  private final String clientSecret;
  private final boolean useOuiNonInJson;
  private final boolean debugApiRequests;
  private final boolean correctHttpCodes;
  private final boolean jsonContentType = true;

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
      checkHttpStatusCode(request, response, 200);
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
    return get(route, null, params, null);
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

    return get(route, entryId, params, null);
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
  public String get(
      String route, String entryId, Map<String, String> params, Map<String, String> headers)
      throws IOException {

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
    HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).GET();
    HttpRequest request = defineHeader(builder, headers).build();

    try {
      HttpResponse<String> response =
          this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      checkHttpStatusCode(request, response, 200);
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

    return post(route, data, null);
  }

  /**
   * Perform a POST request.
   *
   * @param route the API route
   * @param data the data map to send
   * @return the response body as a string
   * @throws IOException if an I/O or HTTP error occurs
   */
  public String post(String route, Map<String, Object> data, Map<String, String> headers)
      throws IOException {
    if (this.useOuiNonInJson) {
      replaceBoolsWithStrings(data);
    }
    String url = createApiUrl(route, null);
    String formBody = this.jsonContentType ? this.gson.toJson(data) : encodeFormData(data);

    // Create HTTP request
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(formBody));
    HttpRequest request = defineHeader(builder, headers).build();

    try {
      HttpResponse<String> response =
          this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      checkHttpStatusCode(request, response, Set.of(200, 201));
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
    return put(route, entryId, data, null);
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
  public String put(
      String route, String entryId, Map<String, Object> data, Map<String, String> headers)
      throws IOException {
    if (this.useOuiNonInJson) {
      replaceBoolsWithStrings(data);
    }
    String url = createApiUrl(route, entryId);
    String formBody = this.jsonContentType ? this.gson.toJson(data) : encodeFormData(data);

    // Create HTTP request
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .PUT(HttpRequest.BodyPublishers.ofString(formBody));
    HttpRequest request = defineHeader(builder, headers).build();

    try {
      HttpResponse<String> response =
          this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      checkHttpStatusCode(request, response, 200);
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

    return delete(route, null, data, null);
  }

  /**
   * Perform a DELETE request.
   *
   * @param route the API route
   * @param data the data map to send
   * @return the response body as a string
   * @throws IOException if an I/O or HTTP error occurs
   */
  public String delete(
      String route, String entryId, Map<String, Object> data, Map<String, String> headers)
      throws IOException {

    String url = createApiUrl(route, entryId);
    String formBody = this.jsonContentType ? this.gson.toJson(data) : encodeFormData(data);

    // Create HTTP request
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .method("DELETE", HttpRequest.BodyPublishers.ofString(formBody));
    HttpRequest request = defineHeader(builder, headers).build();

    try {
      HttpResponse<String> response =
          this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      checkHttpStatusCode(request, response, this.correctHttpCodes ? 204 : 200);
      return response.body();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted during DELETE request", e);
    }
  }

  HttpRequest.Builder defineHeader(HttpRequest.Builder builder, Map<String, String> headers)
      throws IOException {

    builder.header("Accept", "application/json");

    // FG API does not like JSON GET requests
    if (!builder.build().method().equals("GET")) {
      builder.header(
          "Content-Type",
          this.jsonContentType ? "application/json" : "application/x-www-form-urlencoded");
    }

    if (this.bearer != null || this.keycloak) {
      builder.header("Authorization", "Bearer " + getBearerToken());
    }
    if (headers != null && !headers.isEmpty()) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        builder.header(entry.getKey(), entry.getValue());
      }
    }

    return builder;
  }

  //
  // Utility methods
  //

  private String createApiUrl(String route) {
    return createApiUrl(route, null);
  }

  private String createApiUrl(String route, String entryId) {
    if (entryId == null) {
      return this.apiUrl + "/" + route + (this.trailSlash ? "/" : "");
    }
    return this.apiUrl + "/" + route + "/" + entryId;
  }

  private void checkHttpStatusCode(
      HttpRequest request, HttpResponse<String> response, int expectedCode) throws IOException {

    checkHttpStatusCode(request, response, Set.of(expectedCode));
  }

  private void checkHttpStatusCode(
      HttpRequest request, HttpResponse<String> response, Set<Integer> expectedCodes)
      throws IOException {
    checkHttpStatusCode(request, response, expectedCodes, null);
  }

  private void checkHttpStatusCode(
      HttpRequest request,
      HttpResponse<String> response,
      Set<Integer> expectedCodes,
      String requestBodyContent)
      throws IOException {

    int status = response.statusCode();
    String body = response.body();
    String method = request.method();
    String data = request.bodyPublisher().map(p -> p.toString()).orElse("");

    if (this.debugApiRequests
        || ((status < 400 || status >= 500) && (body == null || body.isEmpty()))) {
      printHttpRequestAndResponse(request, requestBodyContent, response);
    }

    switch (status) {
      case 400 -> throw new IOException("HTTP error 400: Bad request: " + body);
      case 401 -> throw new IOException("HTTP error 401: Unauthorized (token expired or invalid)");
      case 403 -> throw new IOException("HTTP error 403: Forbidden access (bad or missing token).");
      case 404 -> throw new IOException("HTTP error 404: Not found.");
      case 405 ->
          throw new IOException("HTTP error 405: Method Not Allowed (FG operation not allowed).");
      default -> {
        if (!expectedCodes.contains(status)) {
          throw new IOException(
              "ERROR: expected HTTP "
                  + expectedCodes
                  + " status code, got "
                  + status
                  + ". Body received: "
                  + body);
        }
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        if (!"".equals(contentType) && !contentType.startsWith("application/json")) {
          System.err.println("response.headers: " + response.headers().map());
          System.err.println("response.text: " + body);
          throw new IOException(
              "ERROR: Invalid response content type (application/json was expected).");
        }
      }
    }
  }

  private static void printHttpRequestAndResponse(
      HttpRequest request, String requestBodyContent, HttpResponse<String> response) {

    String method = request.method();
    System.err.println(method + " request URI: " + request.uri());
    System.err.println(method + " request headers: " + request.headers().map());
    System.err.println(method + " request data: " + requestBodyContent);
    System.err.println(method + " response status_code: " + response.statusCode());
    System.err.println(method + " response headers: " + response.headers().map());
    System.err.println(method + " response content: " + response.body());
    System.err.println();
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
      boolean debugApiRequests,
      boolean trailSlash,
      boolean correctHttpCodes) {

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

    if (credentials.containsKey(credentialPrefix + ".bearer")) {
      this.bearer = requireField(credentials, credentialPrefix + ".bearer");
    }

    this.useOuiNonInJson = useOuiNonInJson;
    this.debugApiRequests = debugApiRequests;
    this.trailSlash = trailSlash;
    this.correctHttpCodes = correctHttpCodes;

    this.httpClient = HttpClient.newHttpClient();
    this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
  }
}
