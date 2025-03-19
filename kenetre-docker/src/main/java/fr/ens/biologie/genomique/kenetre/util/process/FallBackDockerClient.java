package fr.ens.biologie.genomique.kenetre.util.process;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import fr.ens.biologie.genomique.kenetre.log.DummyLogger;
import fr.ens.biologie.genomique.kenetre.log.GenericLogger;
import fr.ens.biologie.genomique.kenetre.util.SystemUtils;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class define a Docker client using the Docker command line.
 *
 * @author Laurent Jourdren
 * @since 2.0
 */
public class FallBackDockerClient implements DockerClient {

  private final GenericLogger logger;
  private boolean gpus;
  private int userUid = SystemUtils.uid();
  private int userGid = SystemUtils.gid();

  //
  // Getters
  //

  /**
   * Test if gpus are enabled.
   *
   * @return true if gpus are enabled
   */
  public boolean isGpusEnabled() {
    return this.gpus;
  }

  /**
   * Get the uid for executing the instance.
   *
   * @return the uid for executing the instance
   */
  public int uid() {

    return this.userUid;
  }

  /**
   * Get the uid for executing the instance.
   *
   * @return the uid for executing the instance
   */
  public int gid() {

    return this.userUid;
  }

  //
  // Setters
  //

  /**
   * Enable the GPUs.
   *
   * @param enable enable the gpus
   */
  public void enableGpus(boolean enable) {
    this.gpus = enable;
  }

  /**
   * Set the UID.
   *
   * @param uid the UID to set
   */
  public void setUid(int uid) {

    if (uid < 0) {
      throw new IllegalArgumentException("Invalid UID value: " + uid);
    }

    this.userUid = uid;
  }

  /**
   * Set the GID.
   *
   * @param gid the GID to set
   */
  public void setGid(int gid) {

    if (gid < 0) {
      throw new IllegalArgumentException("Invalid GID value: " + gid);
    }

    this.userGid = gid;
  }

  /** Set the UID and GID for root. */
  public void useRootUser() {

    setUid(0);
    setGid(0);
  }

  /** Set the UID and GID for nobody. */
  public void useNobodyUser() {

    setUid(65534);
    setGid(65534);
  }

  //
  // DockerClient methods
  //

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

    return new FallBackDockerImageInstance(
        dockerImage, mountFileIndirections, this.gpus, this.userUid, this.userGid, this.logger);
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
  public FallBackDockerClient() {

    this(null);
  }

  /**
   * Constructor.
   *
   * @param logger logger to use
   */
  public FallBackDockerClient(GenericLogger logger) {

    this.logger = logger == null ? new DummyLogger() : logger;
  }
}
