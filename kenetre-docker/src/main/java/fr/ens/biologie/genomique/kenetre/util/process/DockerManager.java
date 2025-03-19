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

package fr.ens.biologie.genomique.kenetre.util.process;

import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.log.GenericLogger;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * This class define a class that manage Eoulsan Docker connections.
 *
 * @author Laurent Jourdren
 * @since 2.0
 */
public class DockerManager {

  /** Available Docker clients. */
  public enum ClientType {
    DEFAULT,
    DOCKER_JAVA,
    SINGULARITY,
    FALLBACK;

    public static ClientType parseClientName(String name) {

      if (name == null) {
        return DEFAULT;
      }

      switch (name.toLowerCase().trim()) {
        case "docker-java":
        case "docker_java":
        case "dockerjava":
          return DOCKER_JAVA;

        case "singularity":
          return SINGULARITY;

        case "fallback":
          return FALLBACK;

        default:
          return DEFAULT;
      }
    }
  };

  private static DockerManager singleton;
  private final DockerClient client;

  /**
   * Create a Docker image instance.
   *
   * @param dockerImage docker image
   * @return a Docker connection object
   * @throws IOException if an error occurs while creating the image instance
   */
  public DockerImageInstance createImageInstance(final String dockerImage) throws IOException {

    return this.client.createConnection(dockerImage);
  }

  /**
   * List the tags of installed images.
   *
   * @return a set with the tags of installed images
   * @throws IOException if an error occurs while listing the tag
   */
  public Set<String> listImageTags() throws IOException {

    return client.listImageTags();
  }

  /**
   * Close Docker connections.
   *
   * @throws IOException if an error occurs while closing the connections
   */
  public static void closeConnections() throws IOException {

    if (singleton != null) {
      singleton.client.close();
    }
  }

  //
  // Singleton method
  //

  /**
   * Get the instance of the DockerManager.
   *
   * @return the instance of the DockerManager
   * @throws IOException if an error occurs while creating the DockerManager instance
   */
  public static DockerManager getInstance() throws IOException {

    return getInstance(ClientType.DEFAULT, URI.create("unix:///var/run/docker.sock"));
  }

  /**
   * Get the instance of the DockerManager.
   *
   * @param clientType Docker client type
   * @param dockerConnection URI of the docker connection
   * @return the instance of the DockerManager
   * @throws IOException if an error occurs while creating the DockerManager instance
   */
  public static DockerManager getInstance(ClientType clientType, URI dockerConnection)
      throws IOException {

    return getInstance(clientType, dockerConnection, null);
  }

  /**
   * Get the instance of the DockerManager.
   *
   * @param clientType Docker client type
   * @param dockerConnection URI of the docker connection
   * @param logger the logger
   * @return the instance of the DockerManager
   * @throws IOException if an error occurs while creating the DockerManager instance
   */
  public static synchronized DockerManager getInstance(
      ClientType clientType, URI dockerConnection, GenericLogger logger) throws IOException {

    if (singleton == null) {
      singleton = new DockerManager(clientType, dockerConnection, logger);
    }

    return singleton;
  }

  //
  // Constructor
  //

  /**
   * Private constructor.
   *
   * @param clientType Docker client type
   * @param dockerConnection URI of the docker connection
   * @param logger the logger
   * @throws IOException if an error occurs while creating the instance
   */
  private DockerManager(ClientType clientType, URI dockerConnection, GenericLogger logger)
      throws IOException {

    requireNonNull(clientType);
    requireNonNull(dockerConnection);

    switch (clientType) {
      case FALLBACK:
        this.client = new FallBackDockerClient(logger);
        break;

      case SINGULARITY:
        this.client = new Singularity3DockerClient(logger);
        break;

      case DEFAULT:
      case DOCKER_JAVA:
        this.client = new DockerJavaDockerClient(logger);
        break;

      default:
        throw new IllegalStateException("Unsupported Docker client implementation: " + clientType);
    }

    this.client.initialize(dockerConnection);
  }
}
