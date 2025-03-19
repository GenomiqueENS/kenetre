/*
 *                  Eoulsan development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public License version 2.1 or
 * later and CeCILL-C. This should be distributed with the code.
 * If you do not have a copy, see:
 *
 *      http://www.gnu.org/licenses/lgpl-2.1.txt
 *      http://www.cecill.info/licences/Licence_CeCILL-C_V1-en.txt
 *
 * Copyright for this code is held jointly by the Genomic platform
 * of the Institut de Biologie de l'École normale supérieure and
 * the individual authors. These should be listed in @author doc
 * comments.
 *
 * For more information on the Eoulsan project and its aims,
 * or to join the Eoulsan Google group, visit the home page
 * at:
 *
 *      http://outils.genomique.biologie.ens.fr/eoulsan
 *
 */

package fr.ens.biologie.genomique.kenetre.util;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.ens.biologie.genomique.kenetre.io.FileUtils;
import fr.ens.biologie.genomique.kenetre.log.DummyLogger;
import fr.ens.biologie.genomique.kenetre.log.GenericLogger;

/**
 * This class is used to install binaries bundled in the jar.
 * @since 1.0
 * @author Laurent Jourdren
 */
public class BinariesInstaller {

  private static final int BUFFER_SIZE = 32 * 1024;

  /** Platforms where the application is available. */
  public static final Set<String> AVAILABLE_BINARY_ARCH =
      Collections.unmodifiableSet(
          new HashSet<>(Arrays.asList("linux\tamd64", "linux\tx86_64")));

  /** Platforms alias. */
  public static final Map<String, String> AVAILABLE_BINARY_ARCH_ALIAS =
      Collections.unmodifiableMap(
          Collections.singletonMap("linux\tx86_64", "linux\tamd64"));

  private GenericLogger logger;
  private String appName;
  private String appLowerCaseName;
  private String appVersion;

  private void install(final String inputPath, final String file,
      final String outputPath) throws IOException {

    if (new File(outputPath, file).isFile()) {
      this.logger.debug(file + " is already installed.");
      return;
    }

    final File outputDir = new File(outputPath);

    if (!outputDir.isDirectory()) {
      if (!outputDir.mkdirs()) {
        throw new IOException(
            "Can't create directory for binaries installation: "
                + outputDir.getAbsolutePath());
      }
      outputDir.setWritable(true, false);
    }

    final String resourcePath = inputPath.toLowerCase() + "/" + file;
    final InputStream is =
        BinariesInstaller.class.getResourceAsStream(resourcePath);

    if (is == null) {
      throw new FileNotFoundException(
          "Unable to find the correct resource (" + resourcePath + ")");
    }

    final File outputFile = new File(outputDir, file);
    OutputStream fos = FileUtils.createOutputStream(outputFile);

    byte[] buf = new byte[BUFFER_SIZE];
    int i = 0;

    while ((i = is.read(buf)) != -1) {
      fos.write(buf, 0, i);
    }

    is.close();
    fos.close();

    outputFile.setExecutable(true, false);  
    outputFile.setReadable(true, false);
  }

  /**
   * Install a binary bundled in the jar in a temporary directory. If no
   * temporary directory is defined, use the "java.io.tmpdir" property.
   * @param softwarePackage software name
   * @param packageVersion software version
   * @param binaryFilename program to install
   * @param tempDir temporary directory where to install the binary
   * @return a string with the path of the installed binary
   * @throws IOException if an error occurs while installing binary
   */
  public String install(final String softwarePackage,
      final String packageVersion, final String binaryFilename,
      final String tempDir) throws IOException {

    final File tempDirFile = new File(tempDir == null
        ? System.getProperty("java.io.tmpdir") : tempDir.trim());

    if (!tempDirFile.exists()) {
      throw new IOException(
          "Temporary directory does not exits: " + tempDirFile);
    }

    if (!tempDirFile.isDirectory()) {
      throw new IOException(
          "Temporary directory is not a directory: " + tempDirFile);
    }

    final String outputPath = tempDirFile.getAbsolutePath()
        + "/" + this.appLowerCaseName + "/" + this.appVersion + "/"
        + softwarePackage + "/" + packageVersion;

    // Test if the file is already installed
    if (new File(outputPath, binaryFilename).isFile()) {
      this.logger.info(binaryFilename + " is already installed.");
      return outputPath + "/" + binaryFilename;
    }

    final String os = System.getProperty("os.name").toLowerCase();
    final String arch = System.getProperty("os.arch").toLowerCase();

    this.logger.debug("Try to install \""
        + binaryFilename + "\" of " + softwarePackage + " package for " + os
        + " (" + arch + ")");

    // Get inputPath
    final String inputPath = getInputPath(softwarePackage, packageVersion);

    // install the file
    install(inputPath, binaryFilename, outputPath);

    this.logger.debug(
        "Successful installation of " + binaryFilename + " in " + outputPath);
    return outputPath + "/" + binaryFilename;
  }

  /**
   * Check if a software is available.
   * @param softwarePackage software name
   * @param packageVersion software version
   * @param binaryFilename software binary
   * @return true if the software is available
   */
  public boolean check(final String softwarePackage,
      final String packageVersion, final String binaryFilename) {

    try {

      final String inputPath = getInputPath(softwarePackage, packageVersion);
      final String resourcePath =
          inputPath.toLowerCase() + "/" + binaryFilename;

      final InputStream is =
          BinariesInstaller.class.getResourceAsStream(resourcePath);

      if (is == null) {
        return false;
      }

      is.close();

    } catch (IOException e) {
      return false;
    }

    return true;
  }

  /**
   * Get the directory path of a binary.
   * @param softwarePackage the software package
   * @param packageVersion the package version
   * @return the directory path as a String
   * @throws IOException if the software is not available
   */
  private String getInputPath(final String softwarePackage,
      final String packageVersion) throws IOException {

    if (!SystemUtils.isUnix()) {
      throw new IOException("Can only install binaries on *nix systems.");
    }

    final String os = System.getProperty("os.name").toLowerCase();
    final String arch = System.getProperty("os.arch").toLowerCase();

    String osArchKey = os + "\t" + arch;

    // Check if platform is allowed
    if (!AVAILABLE_BINARY_ARCH.contains(osArchKey)) {
      throw new FileNotFoundException(
          "There is no executable for your platform ("
              + os + ") included in " + this.appName);
    }

    // Change the os and arch if alias
    if (AVAILABLE_BINARY_ARCH_ALIAS.containsKey(osArchKey)) {
      osArchKey = AVAILABLE_BINARY_ARCH_ALIAS.get(osArchKey);
    }

    return '/'
        + osArchKey.replace(" ", "").replace('\t', '/')
        + (softwarePackage == null ? "" : '/' + softwarePackage.trim())
        + (packageVersion == null ? "" : '/' + packageVersion);
  }

  //
  // Constructors
  //

  /**
   * Constructor.
   * @param applicationName application name
   * @param version application version
   */
  public BinariesInstaller(String applicationName, String version) {
    this(applicationName, version, null);
  }

  /**
   * Constructor.
   * @param applicationName application name
   * @param applicationVersion application version
   * @param logger logger
   */
  public BinariesInstaller(String applicationName, String applicationVersion,
      GenericLogger logger) {

    requireNonNull(applicationName);
    requireNonNull(applicationVersion);

    this.appName = applicationName.trim();
    this.appLowerCaseName = this.appName.toLowerCase();
    this.appVersion = applicationVersion != null
        ? applicationVersion.trim() : "unknown-version";
    this.logger = logger != null ? logger : new DummyLogger();
  }

}
