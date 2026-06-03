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

package fr.ens.biologie.genomique.kenetre.storage;

import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.bio.GenomeDescription;
import fr.ens.biologie.genomique.kenetre.bio.readmapper.MapperInstance;
import fr.ens.biologie.genomique.kenetre.log.GenericLogger;
import fr.ens.biologie.genomique.kenetre.util.Utils;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class define a basic GenomeIndexStorage based on an index file.
 *
 * @since 1.1
 * @author Laurent Jourdren
 */
public class FileGenomeIndexStorage extends AbstractFileGenomeIndexStorage {

  private static final String LOG_FILENAME = "genomes_index_storage.log";
  private static final ReentrantLock JVM_LOCK = new ReentrantLock();

  private DataPath logPath;

  @Override
  protected DataPath newDataPath(String source) {

    return new FileDataPath(source);
  }

  @Override
  protected DataPath newDataPath(DataPath parent, String filename) {

    return new FileDataPath(parent, filename);
  }

  @Override
  protected void logGet(
      MapperInstance mapperInstance,
      GenomeDescription genome,
      Map<String, String> additionalDescription,
      DataPath indexArchive) {

    if (this.logPath == null) {
      return;
    }

    try {
      if (!this.logPath.exists()) {

        // Create the log file
        appendLineWithLock(
            this.logPath.toFile().toPath(),
            "#Date\tMapperName\tMapperVersion\tMapperFlavor\tGenomeName\tIndexPath\tIndexMD5Sum");
      }

      StringBuilder sb = new StringBuilder();
      sb.append(OffsetDateTime.now());
      sb.append('\t');
      sb.append(mapperInstance.getName());
      sb.append('\t');
      sb.append(mapperInstance.getVersion());
      sb.append('\t');
      sb.append(mapperInstance.getFlavor());
      sb.append('\t');
      sb.append(genome.getGenomeName());
      sb.append('\t');
      sb.append(indexArchive);
      sb.append('\t');
      sb.append(genome.getMD5Sum());

      appendLineWithLock(this.logPath.toFile().toPath(), sb.toString());

    } catch (IOException e) {
      Utils.nop();
    }
  }

  private static void appendLineWithLock(Path filePath, String line) throws IOException {

    JVM_LOCK.lock();
    try (FileChannel channel =
            FileChannel.open(
                filePath,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE);
        FileLock lock = channel.lock()) {

      channel.write(Charset.defaultCharset().encode(line + System.lineSeparator()));
    } finally {
      JVM_LOCK.unlock();
    }
  }

  //
  // Static methods
  //

  /**
   * Create a GenomeIndexStorage
   *
   * @param dir the path of the genome descriptions storage
   * @param logger the logger
   * @return a GenomeIndexStorage object if the path contains an index storage or null if no index
   *     storage is found
   */
  public static GenomeIndexStorage getInstance(final String dir, GenericLogger logger) {

    return getInstance(dir, false, logger);
  }

  /**
   * Create a GenomeIndexStorage
   *
   * @param dir the path of the genome descriptions storage
   * @param usageLogEnabled enable usage log
   * @param logger the logger
   * @return a GenomeIndexStorage object if the path contains an index storage or null if no index
   *     storage is found
   */
  public static GenomeIndexStorage getInstance(
      final String dir, boolean usageLogEnabled, GenericLogger logger) {

    requireNonNull(dir);

    try {
      return new FileGenomeIndexStorage(new FileDataPath(dir), usageLogEnabled, logger);
    } catch (IOException | NullPointerException e) {
      return null;
    }
  }

  //
  // Constructor
  //

  /**
   * Private constructor.
   *
   * @param dir the path of the genome descriptions storage
   * @param usageLogEnabled enable usage log
   * @param logger logger to use
   * @throws IOException if an error occurs while creating the object
   */
  private FileGenomeIndexStorage(DataPath dir, boolean usageLogEnabled, GenericLogger logger)
      throws IOException {
    super(dir, logger);
    this.logPath = usageLogEnabled ? newDataPath(dir, LOG_FILENAME) : null;
  }
}
