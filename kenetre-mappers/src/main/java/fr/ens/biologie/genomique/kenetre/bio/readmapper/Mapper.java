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

package fr.ens.biologie.genomique.kenetre.bio.readmapper;

import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.log.GenericLogger;
import java.io.File;
import java.io.IOException;

/**
 * This class define a class allow to define the mapper name, the version, the flavor and how a
 * mapper will be executed.
 *
 * @author Laurent Jourdren
 * @since 2.0
 */
public class Mapper {

  private final MapperProvider provider;
  private final GenericLogger logger;
  private final File tempDir;
  private final File executablesTempDir;
  private final String applicationName;
  private final String applicationVersion;

  //
  // Getters
  //

  /**
   * Get the name of the mapper
   *
   * @return the name of the mapper
   */
  public String getName() {
    return this.provider.getName();
  }

  /**
   * Get the provider of the mapper.
   *
   * @return the provider of the mapper
   */
  public MapperProvider getProvider() {
    return this.provider;
  }

  /**
   * Get the logger of the mapper.
   *
   * @return the logger of the mapper
   */
  public GenericLogger getLogger() {
    return this.logger;
  }

  /**
   * Get the temporary directory.
   *
   * @return the temporary directory
   */
  public File getTemporaryDirectory() {
    return this.tempDir;
  }

  /**
   * Get the executable temporary directory.
   *
   * @return the executable temporary directory
   */
  public File getExecutablesTemporaryDirectory() {
    return this.executablesTempDir;
  }

  /**
   * Get application name.
   *
   * @return the application name
   */
  public String getApplicationName() {
    return this.applicationName;
  }

  /**
   * Get application version.
   *
   * @return the application version
   */
  public String getApplicationVersion() {
    return this.applicationVersion;
  }

  /**
   * Get the software package of the mapper.
   *
   * @return the software package of the mapper
   */
  private String getSoftwarePackage() {
    return getName();
  }

  /**
   * Test if the mapper can only be use for generate the mapper index.
   *
   * @return true if the mapper is a fake mapper
   */
  public boolean isIndexGeneratorOnly() {
    return this.provider.isIndexGeneratorOnly();
  }

  /**
   * Test if the mapping can be split for parallelization.
   *
   * @return true if the mapping can be split for parallelization
   */
  public boolean isSplitsAllowed() {
    return this.provider.isSplitsAllowed();
  }

  /**
   * Test if the mapper index must be compressed in ZIP archive.
   *
   * @return true if the mapper index must be compressed
   */
  public boolean isCompressIndex() {
    return this.provider.isCompressedIndex();
  }

  //
  // Mapper instance creation methods
  //

  /**
   * Create a new mapper instance.
   *
   * @param version the version of the mapper
   * @param flavor the flavor of the mapper
   * @param useBundledBinaries true if bundled binaries must be used
   * @param dockerImage the Docker image to use to executed the mapper
   * @return a new MapperInstance object
   * @throws IOException if an error occurs while creating the mapper instance
   */
  MapperInstance newMapperInstance(
      final String version,
      final String flavor,
      final boolean useBundledBinaries,
      final String dockerImage)
      throws IOException {

    // Define the version to use
    final String versionToUse = chooseVersion(version, this.provider.getDefaultVersion());

    // Define the falvor to use
    final String flavorToUse = chooseVersion(flavor, this.provider.getDefaultFlavor());

    // Create the executor
    MapperExecutor executor = getExecutor(versionToUse, dockerImage, useBundledBinaries);

    return new MapperInstance(
        this,
        executor,
        versionToUse,
        flavorToUse,
        getTemporaryDirectory(),
        this.applicationName,
        this.logger);
  }

  /**
   * Select the best suited executor.
   *
   * @param version the version of the mapper
   * @param dockerImage the docker image of the mapper
   * @param useBundledBinaries true if bundled binaries must be used
   * @return a MapperExecutor instance
   * @throws IOException if an error occurs while creating the executor
   */
  private MapperExecutor getExecutor(
      final String version, final String dockerImage, final boolean useBundledBinaries)
      throws IOException {

    // Set the executor to use
    if (dockerImage != null && !dockerImage.isEmpty()) {
      return new DockerMapperExecutor(dockerImage, getTemporaryDirectory(), this.logger);
    }

    if (useBundledBinaries) {
      return new BundledMapperExecutor(
          getSoftwarePackage(),
          version,
          getExecutablesTemporaryDirectory(),
          this.logger,
          this.applicationName,
          this.applicationVersion);
    }

    return new PathMapperExecutor(this.logger);
  }

  //
  // Static methods
  //

  /**
   * Choose the version to use.
   *
   * @param requiredVersion the version required by user. Can be null or empty
   * @param defaultVersion the default version
   * @return the version to use. This version is the default version if the required version is null
   *     or empty
   */
  private static String chooseVersion(final String requiredVersion, final String defaultVersion) {

    if (defaultVersion == null) {
      throw new NullPointerException("the defaultVersion argument cannot be null");
    }

    if (requiredVersion == null) {
      return defaultVersion;
    }

    String result = requiredVersion.trim().toLowerCase();

    return result.isEmpty() ? defaultVersion : result;
  }

  //
  // Constructor
  //

  /**
   * Private constructor.
   *
   * @param provider the provider to use the the Mapper class.
   * @param tempDir temporary directory
   * @param executablesTempDir temporary directory for executables
   * @param logger the logger to use
   * @param applicationName name of the application
   * @param applicationVersion version of the application
   */
  Mapper(
      final MapperProvider provider,
      final File tempDir,
      final File executablesTempDir,
      final GenericLogger logger,
      final String applicationName,
      final String applicationVersion) {

    requireNonNull(provider, "provider cannot be null");
    this.provider = provider;
    this.logger = logger;
    this.tempDir = tempDir != null ? tempDir : new File(System.getProperty("java.io.tmpdir"));
    this.executablesTempDir = executablesTempDir != null ? executablesTempDir : this.tempDir;
    this.applicationName = applicationName != null ? applicationName : "UnknownApplication";
    this.applicationVersion = applicationVersion != null ? applicationVersion : "UnknownVersion";
  }
}
