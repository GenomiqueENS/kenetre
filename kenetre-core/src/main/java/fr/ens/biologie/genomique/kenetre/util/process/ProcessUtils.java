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

import fr.ens.biologie.genomique.kenetre.io.FileUtils;
import fr.ens.biologie.genomique.kenetre.log.GenericLogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Utility class for launching external process.
 *
 * @since 1.0
 * @author Laurent Jourdren
 */
public final class ProcessUtils {

  private static Random random;

  /**
   * Execute a command with the OS.
   *
   * @param cmd Command to execute
   * @param stdOutput don't show the result of the command on the standard output
   * @throws IOException if an error occurs while running the process
   */
  public static void exec(final String cmd, final boolean stdOutput) throws IOException {

    exec(cmd, stdOutput, null);
  }

  /**
   * Execute a command with the OS.
   *
   * @param cmd Command to execute
   * @param stdOutput don't show the result of the command on the standard output
   * @param logger logger to use
   * @throws IOException if an error occurs while running the process
   */
  public static void exec(final String cmd, final boolean stdOutput, final GenericLogger logger)
      throws IOException {

    if (logger != null) {
      logger.debug("execute (Thread " + Thread.currentThread().getId() + "): " + cmd);
    }

    final long startTime = System.currentTimeMillis();

    Process p = Runtime.getRuntime().exec(cmd);

    InputStream std = p.getInputStream();
    BufferedReader stdr = new BufferedReader(new InputStreamReader(std, Charset.defaultCharset()));

    String l = null;

    while ((l = stdr.readLine()) != null) {
      if (stdOutput) {
        System.out.println(l);
      }
    }

    InputStream err = p.getInputStream();
    BufferedReader errr = new BufferedReader(new InputStreamReader(err, Charset.defaultCharset()));

    String l2 = null;

    while ((l2 = errr.readLine()) != null) {
      System.err.println(l2);
    }

    stdr.close();
    errr.close();

    if (logger != null) {
      logEndTime(p, cmd, startTime, logger);
    }
  }

  /**
   * Execute a command with the OS and save the output in file.
   *
   * @param cmd Command to execute
   * @param outputFile The output file
   * @throws IOException if an error occurs while running the process
   */
  public static void execWriteOutput(final String cmd, final File outputFile) throws IOException {

    execWriteOutput(cmd, outputFile, null);
  }

  /**
   * Execute a command with the OS and save the output in file.
   *
   * @param cmd Command to execute
   * @param outputFile The output file
   * @param logger logger to use. Can be null
   * @throws IOException if an error occurs while running the process
   */
  public static void execWriteOutput(
      final String cmd, final File outputFile, final GenericLogger logger) throws IOException {

    if (logger != null) {
      logger.debug("execute (Thread " + Thread.currentThread().getId() + "): " + cmd);
    }

    final long startTime = System.currentTimeMillis();

    Process p = Runtime.getRuntime().exec(cmd);

    InputStream std = p.getInputStream();

    final OutputStream fos = FileUtils.createOutputStream(outputFile);

    FileUtils.copy(std, fos);

    InputStream err = p.getInputStream();
    BufferedReader errr = new BufferedReader(new InputStreamReader(err, Charset.defaultCharset()));

    String l2 = null;

    while ((l2 = errr.readLine()) != null) {
      System.err.println(l2);
    }

    fos.close();
    errr.close();

    if (logger != null) {
      logEndTime(p, cmd, startTime, logger);
    }
  }

  /**
   * Execute a command with the OS and return the output in a string.
   *
   * @param cmd Command to execute
   * @return a string with the output the command
   * @throws IOException if an error occurs while running the process
   */
  public static String execToString(final String cmd) throws IOException {

    return execToString(cmd, false, true);
  }

  /**
   * Execute a command with the OS and return the output in a string.
   *
   * @param cmd Command to execute
   * @param addStdErr add the output of stderr in the result
   * @param checkExitCode if true, non 0 exit code will throw an IOException
   * @return a string with the output the command
   * @throws IOException if an error occurs while running the process
   */
  public static String execToString(
      final String cmd, final boolean addStdErr, final boolean checkExitCode) throws IOException {

    return execToString(cmd, addStdErr, checkExitCode, null);
  }

  /**
   * Execute a command with the OS and return the output in a string.
   *
   * @param cmd Command to execute
   * @param addStdErr add the output of stderr in the result
   * @param logger logger to use. Can be null
   * @param checkExitCode if true, non 0 exit code will throw an IOException
   * @return a string with the output the command
   * @throws IOException if an error occurs while running the process
   */
  public static String execToString(
      final String cmd,
      final boolean addStdErr,
      final boolean checkExitCode,
      final GenericLogger logger)
      throws IOException {

    if (logger != null) {
      logger.debug("execute (Thread " + Thread.currentThread().getId() + "): " + cmd);
    }

    final long startTime = System.currentTimeMillis();

    final Process p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", cmd});

    final InputStream std = p.getInputStream();

    final BufferedReader stdr =
        new BufferedReader(new InputStreamReader(std, Charset.defaultCharset()));

    final StringBuilder sb = new StringBuilder();
    String l1 = null;

    while ((l1 = stdr.readLine()) != null) {
      sb.append(l1);
      sb.append('\n');
    }

    InputStream err = p.getErrorStream();
    BufferedReader errr = new BufferedReader(new InputStreamReader(err, Charset.defaultCharset()));

    String l2 = null;

    while ((l2 = errr.readLine()) != null) {
      if (addStdErr) {
        sb.append(l2);
        sb.append('\n');
      } else {
        System.err.println(l2);
      }
    }

    stdr.close();
    errr.close();

    if (checkExitCode && logger != null) {
      logEndTime(p, cmd, startTime, logger);
    }

    return sb.toString();
  }

  /**
   * Log the time of execution of a process.
   *
   * @param p Process to log
   * @param cmd Command of the process
   * @param startTime Start time in ms
   * @param logger logger to use
   * @throws IOException if an error occurs at the end of the process
   */
  public static void logEndTime(
      final Process p, final String cmd, final long startTime, final GenericLogger logger)
      throws IOException {

    Objects.requireNonNull(logger);

    try {

      final int exitValue = p.waitFor();
      final long endTime = System.currentTimeMillis();

      throwExitCodeException(exitValue, cmd);

      logger.debug(
          "Done (Thread "
              + Thread.currentThread().getId()
              + ", exit code: "
              + exitValue
              + ") in "
              + (endTime - startTime)
              + " ms.");
    } catch (InterruptedException e) {

      logger.error("Interrupted exception: " + e.getMessage());
    }
  }

  /**
   * Throw an IOException if the exit code of a process is not equals to 0.
   *
   * @param exitCode the exit code
   * @param command the executed command
   * @throws IOException if the exit code if not 0
   */
  public static void throwExitCodeException(final int exitCode, final String command)
      throws IOException {

    switch (exitCode) {
      case 0:
        return;

      case 126:
        throw new IOException("Command invoked cannot execute: " + command);

      case 127:
        throw new IOException("Command not found: " + command);

      case 134:
        throw new IOException("Abort: " + command);

      case 139:
        throw new IOException("Segmentation fault: " + command);

      default:
        throw new IOException("Error while executing (exit code " + exitCode + "): " + command);
    }
  }

  /**
   * Return a set withs pid of existing executable.
   *
   * @param executableName name of the executable to look for
   * @return a set of integers with pid of existing executable
   */
  public static Set<Integer> getExecutablePids(final String executableName) {

    if (executableName == null) {
      return null;
    }

    Set<Integer> result = new HashSet<>();

    try {
      final String s = ProcessUtils.execToString("pgrep -x " + executableName.trim());

      final String[] lines = s.split("\n");
      for (String line : lines) {
        try {
          result.add(Integer.parseInt(line));
        } catch (NumberFormatException e) {
          continue;
        }
      }

    } catch (IOException e) {
      return result;
    }

    return result;
  }

  /**
   * Wait the end of the execution of all the instance of an executable.
   *
   * @param executableName name of the executable
   */
  public static void waitUntilExecutableRunning(final String executableName) {

    if (executableName == null) {
      return;
    }

    while (true) {

      final Set<Integer> pids = getExecutablePids(executableName);

      if (pids.size() == 0) {
        return;
      }

      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * Wait a random number of milliseconds.
   *
   * @param maxMilliseconds the maximum number of milliseconds to wait
   */
  public static void waitRandom(final int maxMilliseconds) {

    if (maxMilliseconds <= 0) {
      return;
    }

    if (random == null) {
      random = new Random(System.currentTimeMillis());
    }

    try {
      Thread.sleep(random.nextInt(maxMilliseconds));
    } catch (InterruptedException e) {
    }
  }

  private ProcessUtils() {}
}
