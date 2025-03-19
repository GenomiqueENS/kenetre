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

package fr.ens.biologie.genomique.kenetre.bio.readmapper;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.bio.readmapper.MapperExecutor.Result;
import fr.ens.biologie.genomique.kenetre.io.FileUtils;
import fr.ens.biologie.genomique.kenetre.log.GenericLogger;
import fr.ens.biologie.genomique.kenetre.util.Reporter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains utility methods for the package.
 *
 * @author Laurent Jourdren
 * @since 2.0
 */
public class MapperUtils {

  /**
   * Execute a command and get its output.
   *
   * @param executor the mapper executor
   * @param command the command to execute
   * @return a string with the output
   * @throws IOException if an error occurs while executing the command
   */
  public static String executeToString(final MapperExecutor executor, final List<String> command)
      throws IOException {

    requireNonNull(executor, "executor argument cannot be null");
    requireNonNull(command, "command argument cannot be null");

    final Result result = executor.execute(command, null, true, null, true);

    final StringBuilder sb = new StringBuilder();

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(result.getInputStream()))) {

      String line;

      while ((line = reader.readLine()) != null) {
        sb.append(line);
        sb.append('\n');
      }
    }

    return sb.toString();
  }

  /**
   * Get the index path.
   *
   * @param mapperName mapper name
   * @param indexDirectory index directory
   * @param extension extension of the index
   * @param extensionLength extension length
   * @return the index path
   * @throws IOException if cannot get the path of the index
   */
  public static File getIndexPath(
      final String mapperName,
      final File indexDirectory,
      final String extension,
      final int extensionLength)
      throws IOException {

    requireNonNull(mapperName, "mapperName argument cannot be null");
    requireNonNull(indexDirectory, "indexDirectory argument cannot be null");
    requireNonNull(extension, "extension argument cannot be null");

    final File[] indexFiles = FileUtils.listFilesByExtension(indexDirectory, extension);

    if (indexFiles == null || indexFiles.length == 0) {
      throw new IOException(
          "Unable to get index file for "
              + mapperName
              + " with \""
              + extension
              + "\" extension in directory: "
              + indexDirectory);
    }

    if (indexFiles.length > 1) {
      throw new IOException(
          "More than one index file for "
              + mapperName
              + " with \""
              + extension
              + "\" extension in directory: "
              + indexDirectory);
    }

    // Get the path to the index
    final String bwtFile = indexFiles[0].getAbsolutePath();

    return new File(bwtFile.substring(0, bwtFile.length() - extensionLength));
  }

  /**
   * Convert a string that contains a list of arguments to a list of strings.
   *
   * @param s the string to convert
   * @return a list of string
   */
  public static List<String> argumentsAsList(final String s) {

    if (s == null) {
      return Collections.emptyList();
    }

    // Split the mapper arguments
    final String[] tabMapperArguments = s.trim().split(" ");

    final List<String> result = new ArrayList<>();

    // Keep only non empty arguments
    for (String arg : tabMapperArguments) {
      if (!arg.isEmpty()) {
        result.add(arg);
      }
    }

    return result;
  }

  /**
   * This method write SAM output from a mapper in a file.
   *
   * @param in mapper output as an inputStream
   * @param samOutputFile output SAM file
   * @throws IOException if an error occurs while reading or writing mapper output
   */
  public static final void writeMapperOutputToFile(InputStream in, File samOutputFile)
      throws IOException {

    writeMapperOutputToFile(in, samOutputFile, null, null, null);
  }

  /**
   * This method write SAM output from a mapper in a file.
   *
   * @param in mapper output as an inputStream
   * @param samOutputFile output SAM file
   * @param reporter optional reporter for statistic
   * @param counterGroup counter group if a report has been defined
   * @param logger optional logger for log statistics
   * @throws IOException if an error occurs while reading or writing mapper output
   */
  public static final void writeMapperOutputToFile(
      InputStream in,
      File samOutputFile,
      Reporter reporter,
      String counterGroup,
      GenericLogger logger)
      throws IOException {

    requireNonNull(in);
    requireNonNull(samOutputFile);
    if (reporter != null) {
      requireNonNull(counterGroup);
    }

    int entriesParsed = 0;
    try (BufferedReader readerResults = new BufferedReader(new InputStreamReader(in));
        FileWriter writer = new FileWriter(samOutputFile, defaultCharset())) {

      String line;
      while ((line = readerResults.readLine()) != null) {

        writer.write(line);
        writer.write('\n');

        String trimmedLine = line.trim();
        if ("".equals(trimmedLine) || trimmedLine.startsWith("@")) {
          continue;
        }

        if (trimmedLine.indexOf('\t') != -1) {

          entriesParsed++;
          reporter.incrCounter(counterGroup, "output mapping alignments", 1);
        }
      }
    }

    if (logger != null) {
      logger.info(entriesParsed + " entries parsed in output file");
    }
  }
}
