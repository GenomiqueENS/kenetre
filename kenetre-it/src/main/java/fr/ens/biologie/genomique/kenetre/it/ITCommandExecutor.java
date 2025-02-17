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
package fr.ens.biologie.genomique.kenetre.it;

import static fr.ens.biologie.genomique.kenetre.it.ITLogger.getLogger;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import fr.ens.biologie.genomique.kenetre.KenetreException;

/**
 * The class represents an executor to command line.
 * @author Sandrine Perrin
 * @since 2.0
 */
public class ITCommandExecutor {

  /** The Constant STDERR_FILENAME. */
  private static final String STDERR_FILENAME = "STDERR";

  /** The Constant STDOUT_FILENAME. */
  private static final String STDOUT_FILENAME = "STDOUT";

  /** The Constant CMDLINE_FILENAME. */
  private static final String CMDLINE_FILENAME = "CMDLINE";

  /** The test conf. */
  private final Properties testConf;

  /** The output test directory. */
  private final File outputTestDirectory;

  /** The duration max. */
  private final int durationMax;

  /** The cmd line file. */
  private final File cmdLineFile;

  // Compile current environment variable and set in configuration file with
  // prefix PREFIX_ENV_VAR
  /** The environment variables. */
  private final String[] environmentVariables;

  /**
   * Execute a script from a command line retrieved from the test configuration.
   * @param scriptConfKey key for configuration to get command line
   * @param suffixNameOutputFile suffix for standard and error output file on
   *          process
   * @param desc description on command line
   * @param isApplicationCmdLine true if application to run, otherwise false
   *          corresponding to annexes script
   * @return result of execution command line, if command line not found in
   *         configuration return null
   */
  public ITCommandResult executeCommand(final String scriptConfKey,
      final String suffixNameOutputFile, final String desc,
      final boolean isApplicationCmdLine) {

    if (this.testConf.getProperty(scriptConfKey) == null) {
      return null;
    }

    // Get command line from the configuration
    final String cmdLine = this.testConf.getProperty(scriptConfKey);

    if (cmdLine.isEmpty()) {
      return null;
    }

    // Save command line in file
    if (isApplicationCmdLine) {

      try {
        Files.write(this.cmdLineFile.toPath(), singleton(cmdLine));
      } catch (final IOException e) {

        getLogger().warning(
            "Error while writing the application command line in file: "
                + e.getMessage());
      }
    }

    // Define stdout and stderr file
    final File stdoutFile = createSdtoutFile(suffixNameOutputFile);
    final File stderrFile = createSdterrFile(suffixNameOutputFile);

    int exitValue = -1;
    final Stopwatch timer = Stopwatch.createStarted();

    final ITCommandResult cmdResult = new ITCommandResult(cmdLine,
        this.outputTestDirectory, desc, durationMax);

    try {

      final Process p = Runtime.getRuntime().exec(cmdLine,
          this.environmentVariables, this.outputTestDirectory);

      // Save stdout
      if (stdoutFile != null) {
        new CopyProcessOutput(p.getInputStream(), stdoutFile, "stdout").start();
      }

      // Save stderr
      if (stderrFile != null) {
        new CopyProcessOutput(p.getErrorStream(), stderrFile, "stderr").start();
      }

      // Wait the end of the process
      exitValue = p.waitFor();

      cmdResult.setExitValue(exitValue);

      // Execution script fail, create an exception
      if (exitValue != 0) {

        cmdResult.setException(new KenetreException("\tCommand line: "
            + cmdLine + "\n\tDirectory: " + this.outputTestDirectory
            + "\n\tMessage: bad exit value: " + exitValue));
        cmdResult.setErrorFileOnProcess(stderrFile);

      } else if (exitValue == 0 && !isApplicationCmdLine) {
        // Success execution, remove standard and error output file
        if (!stdoutFile.delete()) {
          getLogger().warning("Unable to deleted stdout file: " + stdoutFile);
        }
        if (!stderrFile.delete()) {
          getLogger().warning("Unable to deleted stderr file: " + stdoutFile);
        }
      }

    } catch (IOException | InterruptedException e) {
      cmdResult.setException(e,
          "\tError before execution.\n\tCommand line: "
              + cmdLine + "\n\tDirectory: " + this.outputTestDirectory
              + "\n\tMessage: " + e.getMessage());

    } finally {
      cmdResult.setDuration(timer.elapsed(TimeUnit.MILLISECONDS));
      timer.stop();
    }

    return cmdResult;
  }

  /**
   * Create standard output file with suffix name, if not empty.
   * @param suffixName the suffix name
   * @return file
   */
  private File createSdtoutFile(final String suffixName) {
    return new File(this.outputTestDirectory,
        STDOUT_FILENAME + (suffixName.isEmpty() ? "" : "_" + suffixName));
  }

  /**
   * Create error output file with suffix name, if not empty.
   * @param suffixName the suffix name
   * @return file
   */
  private File createSdterrFile(final String suffixName) {
    return new File(this.outputTestDirectory,
        STDERR_FILENAME + (suffixName.isEmpty() ? "" : "_" + suffixName));
  }

  //
  // Constructor
  //
  /**
   * Public constructor.
   * @param testConf properties on the test
   * @param outputTestDirectory output test directory
   * @param environmentVariables environment variables to run test
   * @param durationMax the duration maximum in minutes
   */
  public ITCommandExecutor(final Properties testConf,
      final File outputTestDirectory, final List<String> environmentVariables,
      final int durationMax) {

    this.testConf = testConf;
    this.outputTestDirectory = outputTestDirectory;

    // Extract environment variable from current context and configuration test
    this.environmentVariables = environmentVariables.toArray(new String[0]);
    this.cmdLineFile = new File(this.outputTestDirectory, CMDLINE_FILENAME);

    this.durationMax = durationMax;

  }

  /**
   * This internal class allow to save Process outputs.
   * @author Laurent Jourdren
   */
  private static final class CopyProcessOutput extends Thread {

    /** The path. */
    private final Path path;

    /** The in. */
    private final InputStream in;

    /** The desc. */
    private final String desc;

    /*
     * (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

      try {
        Files.copy(this.in, this.path, StandardCopyOption.REPLACE_EXISTING);
      } catch (final IOException e) {
        getLogger().warning(
            "Error while copying " + this.desc + ": " + e.getMessage());
      }

    }

    /**
     * Instantiates a new copy process output.
     * @param in the in
     * @param file the file
     * @param desc the desc
     */
    CopyProcessOutput(final InputStream in, final File file,
        final String desc) {

      requireNonNull(in, "in argument cannot be null");
      requireNonNull(file, "file argument cannot be null");
      requireNonNull(desc, "desc argument cannot be null");

      this.in = in;
      this.path = file.toPath();
      this.desc = desc;
    }

  }

}
