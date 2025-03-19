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
import fr.ens.biologie.genomique.kenetre.log.DummyLogger;
import fr.ens.biologie.genomique.kenetre.log.GenericLogger;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class define a genome mapper indexer.
 *
 * @since 1.0
 * @author Laurent Jourdren
 */
public class FileGenomeMapperIndexer {

  private final MapperInstance mapperInstance;
  private final AbstractFileGenomeIndexStorage storage;
  private final String indexerArguments;
  private final int threads;
  private final LinkedHashMap<String, String> additionalDescription;
  private final File temporaryDirectory;

  private final GenericLogger logger;

  /**
   * Create an archived genome index.
   *
   * @param genomePath genome to index
   * @param genomeDescription description of the genome
   * @param mapperIndexFile output genome index archive
   * @throws IOException if an error occurs while creating the genome
   */
  public void createIndex(
      final File genomePath, final GenomeDescription genomeDescription, final File mapperIndexFile)
      throws IOException {

    requireNonNull(genomePath);
    requireNonNull(genomePath);

    createIndex(new FileDataPath(genomePath), genomeDescription, new FileDataPath(mapperIndexFile));
  }

  /**
   * Create an archived genome index.
   *
   * @param genomeDataFile genome to index
   * @param genomeDescription description of the genome
   * @param mapperIndexDataFile output genome index archive
   * @throws IOException if an error occurs while creating the genome
   */
  protected void createIndex(
      final DataPath genomeDataFile,
      final GenomeDescription genomeDescription,
      final DataPath mapperIndexDataFile)
      throws IOException {

    requireNonNull(genomeDataFile);
    requireNonNull(genomeDescription);
    requireNonNull(mapperIndexDataFile);

    final DataPath precomputedIndexDataFile;

    this.logger.info("Mapper name: " + this.mapperInstance.getName());
    this.logger.info("Mapper version: " + this.mapperInstance.getVersion());
    this.logger.info("Mapper flavor: " + this.mapperInstance.getFlavor());
    this.logger.info("Indexer arguments: " + this.indexerArguments);

    if (this.storage == null) {
      precomputedIndexDataFile = null;
    } else {
      precomputedIndexDataFile =
          this.storage.getDataPath(
              this.mapperInstance, genomeDescription, this.additionalDescription);
    }

    // If no index storage or if the index does not already exists compute it
    if (precomputedIndexDataFile == null) {

      this.logger.info("Mapper index not found, must compute it");

      // Compute mapper index
      computeIndex(genomeDataFile, mapperIndexDataFile);

      // Save mapper index in storage
      if (this.storage != null) {
        this.storage.put(
            this.mapperInstance,
            genomeDescription,
            this.additionalDescription,
            mapperIndexDataFile);
      }
    } else {

      this.logger.info(
          "Mapper index found, no need to recompute it (mapper index file: "
              + precomputedIndexDataFile
              + ")");

      this.logger.info(
          "Copy or create a symbolic link for the mapper index file "
              + "(Created file or symbolic link: "
              + mapperIndexDataFile
              + ")");

      // Else download it
      precomputedIndexDataFile.symlinkOrCopy(mapperIndexDataFile);
    }
  }

  /**
   * This this method that really launch index computation.
   *
   * @param genome the path to the genome
   * @param mapperIndex the path to the output archive index
   * @throws IOException if an error occurs while computing index
   */
  private void computeIndex(final DataPath genome, final DataPath mapperIndex) throws IOException {

    File outputFile = mapperIndex.toFile();
    if (outputFile == null) {
      outputFile =
          File.createTempFile(
              this.mapperInstance.getName() + "-index-archive-", ".zip", this.temporaryDirectory);
    }

    if (genome.toFile() != null) {
      this.mapperInstance.makeArchiveIndex(
          genome.toFile(), outputFile, this.indexerArguments, this.threads);
    } else {
      this.mapperInstance.makeArchiveIndex(
          genome.open(), outputFile, this.indexerArguments, this.threads);
    }

    this.logger.info("mapperIndexDataFile: " + mapperIndex);

    if (mapperIndex.toFile() == null) {

      new FileDataPath(outputFile).copy(mapperIndex);

      if (!outputFile.delete()) {
        this.logger.error(
            "Unable to delete temporary " + this.mapperInstance.getName() + " archive index.");
      }
    }
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   *
   * @param mapperInstance Mapper to use for the index generator
   * @param additionalArguments additional indexer arguments
   * @param additionalDescription additional indexer arguments description
   * @param threads number of threads to use when creating the index
   * @param storage the genome index storage
   * @param temporaryDirectory temporary directory for the indexer
   * @param logger the logger
   */
  public FileGenomeMapperIndexer(
      final MapperInstance mapperInstance,
      final String additionalArguments,
      final Map<String, String> additionalDescription,
      final int threads,
      GenomeIndexStorage storage,
      File temporaryDirectory,
      GenericLogger logger) {

    requireNonNull(mapperInstance, "Mapper is null");
    requireNonNull(additionalDescription, "additionalDescription is null");

    // Set the mapper
    this.mapperInstance = mapperInstance;

    // Get genome Index storage path
    if (storage != null) {
      if (!(storage instanceof AbstractFileGenomeIndexStorage)) {
        throw new IllegalArgumentException(
            "storage must be an instance of AbstractGenomeIndexStorage");
      }
      this.storage = (AbstractFileGenomeIndexStorage) storage;
    } else {
      this.storage = null;
    }

    // Set indexer additional arguments of the indexer
    this.indexerArguments =
        additionalArguments == null || additionalArguments.trim().isEmpty()
            ? ""
            : additionalArguments;

    // Set the threads number
    this.threads = threads;

    // Get the additional description
    this.additionalDescription = new LinkedHashMap<>(additionalDescription);

    // Set the temporary directory
    this.temporaryDirectory =
        temporaryDirectory != null
            ? temporaryDirectory
            : new File(System.getProperty("java.io.tmpdir"));

    // Set the logger
    this.logger = logger == null ? new DummyLogger() : logger;
  }
}
