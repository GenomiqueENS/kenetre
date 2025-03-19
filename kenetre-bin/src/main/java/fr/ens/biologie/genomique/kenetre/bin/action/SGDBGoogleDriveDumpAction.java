package fr.ens.biologie.genomique.kenetre.bin.action;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.security.GeneralSecurityException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class allow to create a dump of files on Google drive.
 *
 * @author Laurent Jourdren
 * @since 0.28
 */
public class SGDBGoogleDriveDumpAction implements Action {

  private static final String APPLICATION_NAME = "SGDB GDrive Dump";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private static final int MAX_FOLDER_FILE_COUNT = 1000;
  private static final int WAIT_AFTER_CREDENTIALS_IN_MS = 30000;
  private static final int WAIT_AFTER_FAIL_IN_MS = 30000;

  /**
   * Global instance of the scopes required by this quickstart. If modifying these scopes, delete
   * your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES =
      Arrays.asList(DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE_READONLY);

  private static final String OK_LOG_MESSAGE = "OK";

  /**
   * Creates an authorized Credential object.
   *
   * @param credentialPath credential file
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @param tokensDirectoryPath token directory path
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private static Credential getCredentials(
      String credentialPath, NetHttpTransport HTTP_TRANSPORT, String tokensDirectoryPath)
      throws IOException {

    // Load client secrets.
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(
            JSON_FACTORY, new FileReader(credentialPath, StandardCharsets.UTF_8));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDirectoryPath)))
            .setAccessType("offline")
            .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }

  private static void saveFile(
      Drive service,
      File driveFile,
      Path localOutputPath,
      String outputFilename,
      String exportMimeType,
      Map<String, String> exportLinks)
      throws FileNotFoundException, IOException {

    Path localPath =
        Paths.get(localOutputPath.toString(), Normalizer.normalize(outputFilename, Form.NFC));

    // Check file date, do not overwrite file if there is no change
    // if (localPath.toFile().isFile()) {
    if (Files.isRegularFile(localPath)) {

      FileTime localLastModified = Files.getLastModifiedTime(localPath);
      FileTime driveLastModified =
          FileTime.from(Instant.parse(driveFile.getModifiedTime().toStringRfc3339()));

      if (ChronoUnit.MILLIS.between(localLastModified.toInstant(), driveLastModified.toInstant())
          < 1000) {
        return;
      }
    }

    if (exportMimeType != null) {

      try (OutputStream outputStream = new FileOutputStream(localPath.toFile())) {
        service
            .files()
            .export(driveFile.getId(), exportMimeType)
            .executeMediaAndDownloadTo(outputStream);
      }

    } else {
      try (OutputStream outputStream = new FileOutputStream(localPath.toFile())) {
        service.files().get(driveFile.getId()).executeMediaAndDownloadTo(outputStream);
      }
    }

    // Set last modified time
    Files.setLastModifiedTime(
        localPath, FileTime.from(Instant.parse(driveFile.getModifiedTime().toStringRfc3339())));
  }

  private static void deleteDirectoryRecursion(Path path) throws IOException {
    if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
      try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
        for (Path entry : entries) {
          deleteDirectoryRecursion(entry);
        }
      }
    }
    Files.delete(path);
  }

  @SuppressWarnings("unchecked")
  private static void saveDirectory(
      Drive service, String folderId, Path outputPath, Map<String, String> log) throws IOException {

    FileList result =
        service
            .files()
            .list()
            .setQ("\"" + folderId + "\" in parents")
            .setPageSize(MAX_FOLDER_FILE_COUNT)
            .setFields("nextPageToken, files(id, name, mimeType, modifiedTime, exportLinks)")
            .execute();
    List<File> driveFiles = result.getFiles();

    if (driveFiles == null || driveFiles.isEmpty()) {
    } else {

      // Get the list of already download files
      Set<String> existingFilenames = new HashSet<>();
      if (Files.isDirectory(outputPath)) {
        for (String filename : outputPath.toFile().list()) {
          if (Files.isRegularFile(outputPath.resolve(filename))) {
            existingFilenames.add(filename);
          }
        }
      } else {
        Files.createDirectory(outputPath);
      }

      for (File driveFile : driveFiles) {
        String fileName = driveFile.getName().replace('/', '∕');
        String outputFilename = null;
        String exportMimeType = null;
        boolean regularFile = true;

        switch (driveFile.getMimeType()) {
          case "application/vnd.google-apps.document":
            outputFilename = fileName + ".docx";
            exportMimeType =
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            break;

          case "application/vnd.google-apps.spreadsheet":
            outputFilename = fileName + ".xlsx";
            exportMimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            break;

          case "application/vnd.google-apps.presentation":
            outputFilename = fileName + ".pptx";
            exportMimeType =
                "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            break;

          case "application/vnd.google-apps.folder":
            outputFilename = fileName;
            regularFile = false;
            saveDirectory(service, driveFile.getId(), outputPath.resolve(outputFilename), log);
            break;

          // Excluded
          case "application/vnd.google-apps.form":
            outputFilename = null;
            exportMimeType = null;
            break;

          default:
            outputFilename = fileName;
            exportMimeType = null;
            break;
        }

        // If there is something to save
        if (outputFilename != null) {

          // Remove the file from the list of existing files
          existingFilenames.remove(outputFilename);

          // Save the file
          if (regularFile) {
            int count = 0;
            boolean success = false;

            // Try 3 time to retrieve the file
            while (!success) {
              try {
                count++;
                saveFile(
                    service,
                    driveFile,
                    outputPath,
                    outputFilename,
                    exportMimeType,
                    (Map<String, String>) driveFile.get("exportLinks"));
                success = true;
              } catch (IOException e) {

                // Wait 30 seconds before retry
                try {
                  Thread.sleep(WAIT_AFTER_FAIL_IN_MS);
                } catch (InterruptedException e1) {
                }

                if (count > 3) {
                  log.put(driveFile.getName(), e.getMessage());
                  break;
                }
              }
            }
            if (success) {
              log.put(driveFile.getName(), OK_LOG_MESSAGE);
            }
          }
        }
      }

      // Remove deleted files
      for (String filename : existingFilenames) {
        Path path = outputPath.resolve(filename);
        if (Files.isDirectory(path)) {
          deleteDirectoryRecursion(path);
        } else {
          path.toFile().delete();
        }
      }
    }
  }

  //
  // Action methods
  //

  @Override
  public String getName() {
    return "drivedump";
  }

  @Override
  public String getDescription() {
    return "Dump a Google drive directory";
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public void action(List<String> arguments) {

    // Check arguments
    if (arguments.size() != 4) {
      System.err.println("Invalid number of arguments");
      System.err.println(
          "Syntax: drivedump <credential file> <tokens dir> <folder id> <output dir>");
      System.exit(1);
    }

    // Get arguments
    String credentialPath = arguments.get(0);
    String tokensPath = arguments.get(1);
    String folderId = arguments.get(2);
    String outputPath = arguments.get(3);

    // Check if output path exist
    if (!Files.isDirectory(Paths.get(tokensPath))) {
      System.err.println("Tokens path is not exists: " + tokensPath);
      System.exit(1);
    }

    // Check if output path exist
    if (!Files.isDirectory(Paths.get(outputPath))) {
      System.err.println("Output path is not exists: " + outputPath);
      System.exit(1);
    }

    try {

      // Build a new authorized API client service.
      final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      Credential credentials = getCredentials(credentialPath, HTTP_TRANSPORT, tokensPath);

      // Wait few seconds
      try {
        Thread.sleep(WAIT_AFTER_CREDENTIALS_IN_MS);
      } catch (InterruptedException e) {
        // Do nothing
      }

      // Open service
      Drive service =
          new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
              .setApplicationName(APPLICATION_NAME)
              .build();

      Map<String, String> log = new LinkedHashMap<>();
      // Save data
      saveDirectory(service, folderId, Paths.get(outputPath), log);

      // Check if dump is a success
      boolean success = true;
      for (String v : log.values()) {
        if (!OK_LOG_MESSAGE.equals(v)) {
          success = false;
        }
      }

      // Print on stderr files that has not been downloaded
      if (!success) {
        for (Map.Entry<String, String> e : log.entrySet()) {
          if (!OK_LOG_MESSAGE.equals(e.getValue())) {
            System.err.println(e.getKey() + "\t" + e.getValue());
          }
        }
        System.exit(1);
      }

    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
