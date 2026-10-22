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

import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * This interface define a Docker client.
 *
 * @author Laurent Jourdren
 * @since 2.0
 */
public interface DockerClient {

  /**
   * Initialize the Docker client.
   *
   * @param dockerConnectionURI the URI of the Docker connection.
   * @throws IOException if an error occurs while initialize the Docker connection
   */
  void initialize(URI dockerConnectionURI) throws IOException;

  /**
   * Create a Docker connection.
   *
   * @param dockerImage the Docker image to use with the connection
   * @return a new Docker connection
   * @throws IOException if an error occurs while creating the connection
   */
  DockerImageInstance createConnection(String dockerImage) throws IOException;

  /**
   * Create a Docker connection.
   *
   * @param dockerImage the Docker image to use with the connection
   * @param mountFileIndirections if true, indirections for files and directories will be also
   *     mounted
   * @return a new Docker connection
   * @throws IOException if an error occurs while creating the connection
   */
  DockerImageInstance createConnection(String dockerImage, boolean mountFileIndirections)
      throws IOException;

  /**
   * List the Docker images tags.
   *
   * @return a set with the Docker image tags
   * @throws IOException if an error occurs while listing the Docker image tags
   */
  Set<String> listImageTags() throws IOException;

  /**
   * Close the Docker client.
   *
   * @throws IOException if an error occurs while closing the client
   */
  void close() throws IOException;
}
