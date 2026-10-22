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
 * This class an abstract SimpleProcess class.
 *
 * @author Laurent Jourdren
 * @since 2.0
 */
public abstract class AbstractSimpleProcess implements SimpleProcess {

  protected static final String TMP_DIR_ENV_VARIABLE = "TMPDIR";

  @Override
  public int execute(
      final List<String> commandLine,
      final File executionDirectory,
      final File temporaryDirectory,
      final File stdoutFile,
      final File stderrFile,
      final File... filesUsed)
      throws IOException {

    return execute(
        commandLine,
        executionDirectory,
        null,
        null,
        temporaryDirectory,
        stdoutFile,
        stderrFile,
        false,
        filesUsed);
  }

  @Override
  public int execute(
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

    AdvancedProcess process =
        start(
            commandLine,
            executionDirectory,
            environmentVariables,
            uLimits,
            temporaryDirectory,
            stdoutFile,
            stderrFile,
            redirectErrorStream,
            filesUsed);

    return process.waitFor();
  }
}
