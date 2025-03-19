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
