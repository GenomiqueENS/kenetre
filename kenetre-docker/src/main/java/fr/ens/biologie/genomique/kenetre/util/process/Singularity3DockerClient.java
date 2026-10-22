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

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import fr.ens.biologie.genomique.kenetre.log.DummyLogger;
import fr.ens.biologie.genomique.kenetre.log.GenericLogger;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class define a Docker client using the Singularity command line.
 *
 * @author Laurent Jourdren
 * @since 2.6
 */
public class Singularity3DockerClient implements DockerClient {

  private final GenericLogger logger;

  @Override
  public void initialize(URI dockerConnectionURI) {
    // Nothing to do
  }

  @Override
  public DockerImageInstance createConnection(String dockerImage) {

    return createConnection(dockerImage, false);
  }

  @Override
  public DockerImageInstance createConnection(String dockerImage, boolean mountFileIndirections) {

    return new Singukarity3DockerImageInstance(dockerImage, mountFileIndirections, this.logger);
  }

  @Override
  public void close() {
    // Nothing to do
  }

  @Override
  public Set<String> listImageTags() throws IOException {

    Set<String> result = new HashSet<>();
    String output = ProcessUtils.execToString("docker images");

    Splitter lineSplitter = Splitter.on('\n');
    Splitter fieldSplitter = com.google.common.base.Splitter.on(' ').omitEmptyStrings();

    boolean first = true;

    for (String line : lineSplitter.split(output)) {

      if (first) {
        first = false;
        continue;
      }

      List<String> fields = Lists.newArrayList(fieldSplitter.split(line));

      if (fields.size() >= 2) {
        String tagName = fields.get(0) + ':' + fields.get(1);
        if (!"<none>:<none>".equals(tagName)) {
          result.add(tagName);
        }
      }
    }

    return result;
  }

  //
  // Constructors
  //

  /** Constructor. */
  public Singularity3DockerClient() {

    this(null);
  }

  /**
   * Constructor.
   *
   * @param logger logger to use
   */
  public Singularity3DockerClient(GenericLogger logger) {

    this.logger = logger == null ? new DummyLogger() : logger;
  }
}
