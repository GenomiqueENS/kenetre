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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This interface define how to easily launch a subprocess.
 *
 * @author Laurent Jourdren
 * @since 2.0
 */
public interface SimpleProcess {

  interface AdvancedProcess {

    int waitFor() throws IOException;
  }

  /**
   * Execute a process.
   *
   * @param commandLine command line
   * @param executionDirectory execution directory
   * @param temporaryDirectory temporary directory
   * @param stdoutFile stdout file
   * @param stderrFile stderr file
   * @param filesUsed files used by the process
   * @return the exit code of the process
   * @throws IOException if an error occurs while running the process
   */
  int execute(
      List<String> commandLine,
      File executionDirectory,
      File temporaryDirectory,
      File stdoutFile,
      File stderrFile,
      File... filesUsed)
      throws IOException;

  /**
   * Execute a process.
   *
   * @param commandLine command line
   * @param executionDirectory execution directory
   * @param environmentVariables environment variables
   * @param uLimits ulimits of the execution
   * @param temporaryDirectory temporary directory
   * @param stdoutFile stdout file
   * @param stderrFile stderr file
   * @param redirectErrorStream true if stderr must be redirected in stdout
   * @param filesUsed files used by the process
   * @return the exit code of the process
   * @throws IOException if an error occurs while running the process
   */
  int execute(
      List<String> commandLine,
      File executionDirectory,
      Map<String, String> environmentVariables,
      Map<String, String> uLimits,
      File temporaryDirectory,
      File stdoutFile,
      File stderrFile,
      boolean redirectErrorStream,
      File... filesUsed)
      throws IOException;

  /**
   * Start a process.
   *
   * @param commandLine command line
   * @param executionDirectory execution directory
   * @param environmentVariables environment variables
   * @param uLimits ulimits of the execution
   * @param temporaryDirectory temporary directory
   * @param stdoutFile stdout file
   * @param stderrFile stderr file
   * @param redirectErrorStream true if stderr must be redirected in stdout
   * @param filesUsed files used by the process
   * @return an AdvancedProcess object
   * @throws IOException if an error occurs while starting the process
   */
  AdvancedProcess start(
      List<String> commandLine,
      File executionDirectory,
      Map<String, String> environmentVariables,
      Map<String, String> uLimits,
      File temporaryDirectory,
      File stdoutFile,
      File stderrFile,
      boolean redirectErrorStream,
      File... filesUsed)
      throws IOException;
}
