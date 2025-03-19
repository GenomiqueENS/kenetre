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

import fr.ens.biologie.genomique.kenetre.log.DummyLogger;
import fr.ens.biologie.genomique.kenetre.log.GenericLogger;
import java.io.File;
import java.util.Objects;

/**
 * This class define a mapper builder.
 *
 * @author Laurent Jourdren
 * @since 2.6
 */
public class MapperBuilder {

  private final String mapperName;
  private GenericLogger logger = new DummyLogger();
  private String applicationName = "kenetre";
  private String applicationVersion = "unknown";
  private File tempDir = new File(System.getProperty("java.io.tmpdir"));
  private File executablesTempDir = new File(System.getProperty("java.io.tmpdir"));
  private boolean useNewServiceInstance;

  /**
   * Set the logger.
   *
   * @param logger the logger to set
   * @return the MapperBuilder instance
   */
  public MapperBuilder withLogger(GenericLogger logger) {

    requireNonNull(logger);

    this.logger = logger;
    return this;
  }

  /**
   * Set the application name.
   *
   * @param applicationName the applicationName to set
   * @return the MapperBuilder instance
   */
  public MapperBuilder withApplicationName(String applicationName) {

    requireNonNull(applicationName);

    this.applicationName = applicationName;
    return this;
  }

  /**
   * Set the application version.
   *
   * @param applicationVersion the applicationVersion to set
   * @return the MapperBuilder instance
   */
  public MapperBuilder withApplicationVersion(String applicationVersion) {

    requireNonNull(applicationVersion);

    this.applicationVersion = applicationVersion;
    return this;
  }

  /**
   * Set the temporary directory.
   *
   * @param tempDir the temporary directory to set
   * @return the MapperBuilder instance
   */
  public MapperBuilder withTempDirectory(File tempDir) {

    requireNonNull(tempDir);

    this.tempDir = tempDir;
    return this;
  }

  /**
   * Set the temporary directory for executables.
   *
   * @param executablesTempDir the temporary directory for executables to set
   * @return the MapperBuilder instance
   */
  public MapperBuilder withExecutablesTempDirectory(File executablesTempDir) {

    requireNonNull(executablesTempDir);

    this.executablesTempDir = executablesTempDir;
    return this;
  }

  /**
   * Force the usage of a new service instance to get ReadFilter objects.
   *
   * @param forceUseNewServiceInstance force new service instance usage
   */
  public void useNewServiceInstance(boolean forceUseNewServiceInstance) {

    this.useNewServiceInstance = forceUseNewServiceInstance;
  }

  //
  // Build method
  //

  /**
   * Build the mapper instance.
   *
   * @return a mapper instance
   */
  public Mapper build() {

    MapperProvider provider =
        MapperProviderService.getInstance(this.useNewServiceInstance).newService(this.mapperName);

    if (provider == null) {
      this.logger.error("Unknown mapper: " + this.mapperName);
      return null;
    }

    return new Mapper(
        provider,
        this.tempDir,
        this.executablesTempDir,
        this.logger,
        this.applicationName,
        this.applicationVersion);
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   *
   * @param mapperName mapper name
   */
  public MapperBuilder(String mapperName) {

    Objects.requireNonNull(mapperName);
    this.mapperName = mapperName;
  }

  /**
   * Public constructor.
   *
   * @param mapper mapper to use for initial configuration of the builder
   */
  public MapperBuilder(Mapper mapper) {

    Objects.requireNonNull(mapper);

    this.mapperName = mapper.getName();
    this.logger = mapper.getLogger();
    this.applicationName = mapper.getApplicationName();
    this.applicationVersion = mapper.getApplicationVersion();
    this.tempDir = mapper.getTemporaryDirectory();
    this.executablesTempDir = mapper.getExecutablesTemporaryDirectory();
  }
}
