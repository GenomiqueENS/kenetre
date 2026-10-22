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

import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import fr.ens.biologie.genomique.kenetre.log.DummyLogger;
import fr.ens.biologie.genomique.kenetre.log.GenericLogger;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class define a Docker client using the DockerClient client library.
 *
 * @author Laurent Jourdren
 * @since 2.6
 */
public class DockerJavaDockerClient implements DockerClient {

  private com.github.dockerjava.api.DockerClient client;
  private final GenericLogger logger;

  @Override
  public void initialize(URI dockerConnectionURI) throws IOException {

    requireNonNull(dockerConnectionURI);

    synchronized (this) {
      if (this.client != null) {
        return;
      }

      synchronized (this) {
        DockerClientConfig standard =
            DefaultDockerClientConfig.createDefaultConfigBuilder().build();

        DockerHttpClient httpClient =
            new ZerodepDockerHttpClient.Builder().dockerHost(dockerConnectionURI).build();

        this.client = DockerClientImpl.getInstance(standard, httpClient);

        if (this.client == null) {
          throw new IOException("Unable to connect to Docker deamon: " + dockerConnectionURI);
        }
      }
    }
  }

  @Override
  public DockerImageInstance createConnection(String dockerImage) throws IOException {

    return createConnection(dockerImage, false);
  }

  @Override
  public DockerImageInstance createConnection(String dockerImage, boolean mountFileIndirections)
      throws IOException {

    if (client == null) {
      throw new IllegalStateException("Docker client not initialized");
    }

    return new DockerJavaDockerImageInstance(
        this.client, dockerImage, mountFileIndirections, this.logger);
  }

  @Override
  public Set<String> listImageTags() throws IOException {

    final Set<String> result = new HashSet<>();

    List<Image> list = this.client.listImagesCmd().exec();

    for (Image image : list) {

      if (image.getRepoTags() != null && image.getRepoTags().length > 0) {
        result.add(image.getRepoTags()[0]);
      }
    }

    return result;
  }

  @Override
  public void close() throws IOException {

    this.client.close();
  }

  //
  // Constructors
  //

  /** Constructor. */
  public DockerJavaDockerClient() {

    this(null);
  }

  /**
   * Constructor.
   *
   * @param logger logger to use
   */
  public DockerJavaDockerClient(GenericLogger logger) {

    this.logger = logger == null ? new DummyLogger() : logger;
  }
}
