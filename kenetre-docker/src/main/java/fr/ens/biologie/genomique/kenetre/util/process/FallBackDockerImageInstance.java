package fr.ens.biologie.genomique.kenetre.util.process;

import static com.google.common.base.Preconditions.checkArgument;
import static fr.ens.biologie.genomique.kenetre.util.Utils.filterNull;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.log.GenericLogger;
import fr.ens.biologie.genomique.kenetre.util.SystemUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class define a Docker image instance using the Docker command line.
 *
 * @author Laurent Jourdren
 * @since 2.0
 */
public class FallBackDockerImageInstance extends AbstractSimpleProcess
    implements DockerImageInstance {

  private final String dockerImage;
  private final int userUid;
  private final int userGid;
  private final boolean convertNFSFilesToMountRoots;
  private final boolean gpus;
  private final GenericLogger logger;

  @Override
  public AdvancedProcess start(
      final List<String> commandLine,
      final File executionDirectory,
      final Map<String, String> environmentVariables,
      final File temporaryDirectory,
      final File stdoutFile,
      final File stderrFile,
      final boolean redirectErrorStream,
      final File... filesUsed)
      throws IOException {

    requireNonNull(commandLine, "commandLine argument cannot be null");
    requireNonNull(stdoutFile, "stdoutFile argument cannot be null");
    requireNonNull(stderrFile, "stderrFile argument cannot be null");

    this.logger.debug(
        getClass().getSimpleName()
            + ": commandLine="
            + commandLine
            + ", executionDirectory="
            + executionDirectory
            + ", environmentVariables="
            + environmentVariables
            + ", temporaryDirectory="
            + temporaryDirectory
            + ", stdoutFile="
            + stdoutFile
            + ", stderrFile="
            + stderrFile
            + ", redirectErrorStream="
            + redirectErrorStream
            + ", filesUsed="
            + Arrays.toString(filesUsed));

    if (executionDirectory != null) {
      checkArgument(
          executionDirectory.isDirectory(),
          "execution directory does not exists or is not a directory: "
              + executionDirectory.getAbsolutePath());
    }

    // Pull image if needed
    pullImageIfNotExists();

    final List<String> command = new ArrayList<>();
    command.add("docker");
    command.add("run");

    if (this.gpus) {
      command.add("--gpus");
      command.add("all");
    }

    // File/directories to mount
    List<File> directoriesToBind = new ArrayList<>();
    if (filesUsed != null) {
      directoriesToBind.addAll(filterNull(asList(filesUsed)));
    }

    // Execution directory
    if (executionDirectory != null) {
      directoriesToBind.add(executionDirectory);
      command.add("--workdir");
      command.add(executionDirectory.getAbsolutePath());
    }

    // Environment variables
    if (environmentVariables != null) {
      for (Map.Entry<String, String> e : environmentVariables.entrySet()) {
        command.add("--env");
        command.add(e.getKey() + '=' + e.getValue());
      }
    }

    // Temporary directory
    if (temporaryDirectory != null && temporaryDirectory.isDirectory()) {
      directoriesToBind.add(temporaryDirectory);
    }

    // Bind directories
    toBind(command, directoriesToBind, this.convertNFSFilesToMountRoots, this.logger);

    // Set the UID and GID of the docker process
    if (this.userUid >= 0 && this.userGid >= 0) {
      command.add("--user");
      command.add(this.userUid + ":" + this.userGid);
    }

    // Remove container at the end of the execution
    command.add("--rm");

    // Docker image to use
    command.add(this.dockerImage);

    command.addAll(commandLine);

    // Redirect outputs
    final ProcessBuilder pb = new ProcessBuilder(command);
    pb.redirectOutput(stdoutFile);
    pb.redirectErrorStream(redirectErrorStream);
    if (!redirectErrorStream) {
      pb.redirectError(stderrFile);
    }

    final Process process = pb.start();

    return () -> {
      try {
        return process.waitFor();
      } catch (InterruptedException e) {
        throw new IOException(e);
      }
    };
  }

  /**
   * Add the volume arguments to the Docker command line.
   *
   * @param command the command line
   * @param files the share files to add
   * @throws IOException if an error occurs when converting the file path
   */
  private static void toBind(
      final List<String> command,
      final List<File> files,
      final boolean convertNFSFilesToMountRoots,
      final GenericLogger logger)
      throws IOException {

    for (File file :
        DockerUtils.fileIndirections(
            DockerUtils.convertNFSFileToMountPoint(files, convertNFSFilesToMountRoots, logger))) {

      command.add("--volume");
      command.add(file.getAbsolutePath() + ':' + file.getAbsolutePath());
    }
  }

  @Override
  public void pullImageIfNotExists() throws IOException {

    String images =
        ProcessUtils.execToString("docker images | tr -s ' ' | cut -f 1,2 -d ' ' | tr ' ' :");

    for (String image : images.split("\n")) {

      if (this.dockerImage.equals(image)) {
        // Nothing to do, the image has been already download
        return;
      }
    }

    this.logger.debug("Pull Docker image: " + this.dockerImage);
    Process p = new ProcessBuilder("docker", "pull", this.dockerImage).start();
    int exitCode;
    try {
      exitCode = p.waitFor();
    } catch (InterruptedException e) {
      throw new IOException("Error while pulling Docker image: " + this.dockerImage);
    }

    if (exitCode != 0) {
      throw new IOException("Error while pulling Docker image: " + this.dockerImage);
    }
  }

  @Override
  public void pullImageIfNotExists(final ProgressHandler progress) throws IOException {

    if (progress != null) {
      progress.update(0);
    }

    pullImageIfNotExists();

    if (progress != null) {
      progress.update(1);
    }
  }

  //
  // Constructor
  //

  /**
   * Constructor.
   *
   * @param dockerImage Docker image
   * @param mountFileIndirections true if indirection must be mounted
   * @param gpus enable gpus
   * @param logger logger to use
   */
  FallBackDockerImageInstance(
      final String dockerImage,
      final boolean mountFileIndirections,
      final boolean gpus,
      final GenericLogger logger) {

    this(dockerImage, mountFileIndirections, gpus, SystemUtils.uid(), SystemUtils.gid(), logger);
  }

  /**
   * Constructor.
   *
   * @param dockerImage Docker image
   * @param mountFileIndirections true if indirection must be mounted
   * @param gpus enable gpus
   * @param uid uid of the user to use for executing a process
   * @param gid gid of the user to use for executing a process
   * @param logger logger to use
   */
  FallBackDockerImageInstance(
      final String dockerImage,
      final boolean mountFileIndirections,
      final boolean gpus,
      final int userUid,
      final int userGid,
      final GenericLogger logger) {

    requireNonNull(dockerImage, "dockerImage argument cannot be null");
    requireNonNull(logger, "logger argument cannot be null");

    logger.debug(getClass().getSimpleName() + " docker image used: " + dockerImage);

    this.dockerImage = dockerImage;
    this.userUid = userUid;
    this.userGid = userGid;

    this.convertNFSFilesToMountRoots = mountFileIndirections;
    this.gpus = gpus;
    this.logger = logger;
  }
}
