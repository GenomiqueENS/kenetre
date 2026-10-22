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

import fr.ens.biologie.genomique.kenetre.log.DummyLogger;
import fr.ens.biologie.genomique.kenetre.log.GenericLogger;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class define how to easily launch a subprocess using the Java Process API.
 *
 * @author Laurent Jourdren
 * @since 2.0
 */
public class SystemSimpleProcess extends AbstractSimpleProcess {

  private final GenericLogger logger;

  @Override
  public AdvancedProcess start(
      final List<String> commandLine,
      final File executionDirectory,
      final Map<String, String> environmentVariables,
      final Map<String, String> uLimits,
      final File temporaryDirectory,
      final File stdoutFile,
      final File stderrFile,
      final boolean redirectErrorStream,
      final File... filesUsed)
      throws IOException {

    this.logger.debug(
        getClass().getName()
            + " : commandLine="
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

    final ProcessBuilder pb = new ProcessBuilder(commandLine);
    // Set execution directory
    if (executionDirectory != null) {
      pb.directory(executionDirectory);
    }

    // Set environment variables
    if (environmentVariables != null) {
      pb.environment().putAll(environmentVariables);
    }

    // Set temporary directory
    if (temporaryDirectory != null) {
      pb.environment().put(TMP_DIR_ENV_VARIABLE, temporaryDirectory.getAbsolutePath());
    }

    // Redirect stdout
    if (stdoutFile != null) {
      pb.redirectOutput(stdoutFile);
    }

    // Redirect stderr
    if (redirectErrorStream) {
      pb.redirectErrorStream(true);
    } else if (stderrFile != null) {
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

  //
  // Constructors
  //

  /** Constructor. */
  public SystemSimpleProcess() {

    this(null);
  }

  /**
   * Constructor.
   *
   * @param logger logger to use
   */
  public SystemSimpleProcess(GenericLogger logger) {

    this.logger = logger == null ? new DummyLogger() : logger;
  }
}
