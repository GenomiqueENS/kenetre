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

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.bio.GenomeDescription;
import fr.ens.biologie.genomique.kenetre.bio.readmapper.MapperInstance;
import fr.ens.biologie.genomique.kenetre.io.FileUtils;
import fr.ens.biologie.genomique.kenetre.log.DummyLogger;
import fr.ens.biologie.genomique.kenetre.log.GenericLogger;
import fr.ens.biologie.genomique.kenetre.util.StringUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * This class define an abstract GenomeIndexStorage based on an index file.
 *
 * @since 2.6
 * @author Laurent Jourdren
 */
public abstract class AbstractFileGenomeIndexStorage implements GenomeIndexStorage {

  private static final String INDEX_FILENAME = "genomes_index_storage.txt";

  private final DataPath dir;
  private final Map<String, IndexEntry> entries = new LinkedHashMap<>();
  private final GenericLogger logger;

  /**
   * This inner class define an entry of the index file.
   *
   * @author Laurent Jourdren
   */
  private static final class IndexEntry {

    String genomeName;
    int sequences;
    long length;
    String genomeMD5;
    String mapperName;
    String fileName;
    String description;

    String getKey() {
      return createKey(this.mapperName, this.genomeMD5);
    }

    @Override
    public String toString() {
      return this.getClass().getSimpleName()
          + "{genomeName="
          + this.genomeName
          + ", sequences="
          + this.sequences
          + ", length="
          + this.length
          + ", genomeMD5="
          + this.genomeMD5
          + ", mapperName= "
          + this.mapperName
          + ", fileName="
          + this.fileName
          + "}";
    }
  }

  //
  // Protected methods
  //

  /**
   * Create a new DataPath object.
   *
   * @param source source of the DataPath object
   * @return a new DataPath object
   */
  protected abstract DataPath newDataPath(String source);

  /**
   * Create a new DataPath object.
   *
   * @param parent parent of the DataPath object
   * @param filename name of the file
   * @return a new DataPath object
   */
  protected abstract DataPath newDataPath(DataPath parent, String filename);

  /**
   * Get from the storage the DataPath of the index of a mapper for a genome and its parameters.
   *
   * @param mapperInstance mapper to use
   * @param genome genome description
   * @param additionalDescription additional parameter for the index
   * @return the path of the index
   */
  protected DataPath getDataPath(
      final MapperInstance mapperInstance,
      final GenomeDescription genome,
      final Map<String, String> additionalDescription) {

    requireNonNull(mapperInstance, "Mapper is null");
    requireNonNull(genome, "Genome description is null");
    requireNonNull(additionalDescription, "additionalDescription is null");

    final IndexEntry entry =
        this.entries.get(createKey(mapperInstance, genome, additionalDescription, this.logger));

    return entry == null ? null : newDataPath(this.dir, entry.fileName);
  }

  /**
   * Put in the storage an index.
   *
   * @param mapperInstance mapper to use
   * @param genome genome description
   * @param additionalDescription additional parameter for the index
   * @param indexArchive DataPath of the index
   */
  protected void put(
      final MapperInstance mapperInstance,
      final GenomeDescription genome,
      final Map<String, String> additionalDescription,
      final DataPath indexArchive) {

    requireNonNull(mapperInstance, "Mapper is null");
    requireNonNull(genome, "Genome description is null");
    requireNonNull(additionalDescription, "additionalDescription is null");
    requireNonNull(indexArchive, "IndexArchive is null");

    // Update the index to avoid to lost entries when several instances of
    // Eoulsan are running
    try {
      load();
    } catch (IOException e1) {
      this.logger.warn("Unable to reload the index mapper storage");
    }

    if (!indexArchive.exists()) {
      return;
    }

    final String key = createKey(mapperInstance, genome, additionalDescription, this.logger);

    if (this.entries.containsKey(key)) {
      return;
    }

    final IndexEntry entry = createIndexEntry(mapperInstance, genome, additionalDescription);
    if (entry == null) {
      return;
    }

    try {

      FileUtils.copy(indexArchive.rawOpen(), newDataPath(this.dir, entry.fileName).create());
      this.entries.put(entry.getKey(), entry);
      save();
      this.logger.info(
          "Successfully added "
              + indexArchive.getName()
              + " index archive to genome index storage.");
    } catch (IOException e) {
      this.logger.warn(
          "Failed to add "
              + indexArchive.getName()
              + " index archive to genome index storage: "
              + e.getMessage());
    }
  }

  //
  // Interface methods
  //

  @Override
  public File get(
      final MapperInstance mapperInstance,
      final GenomeDescription genome,
      final Map<String, String> additionalDescription) {

    DataPath result = getDataPath(mapperInstance, genome, additionalDescription);

    return result == null ? null : result.toFile();
  }

  @Override
  public void put(
      final MapperInstance mapper,
      final GenomeDescription genome,
      final Map<String, String> additionalDescription,
      final File indexArchive) {

    requireNonNull(indexArchive, "IndexArchive is null");

    put(mapper, genome, additionalDescription, new FileDataPath(indexArchive));
  }

  //
  // Sum creation method
  //

  private IndexEntry createIndexEntry(
      final MapperInstance mapper,
      final GenomeDescription genome,
      final Map<String, String> additionalDescription) {

    final IndexEntry entry = new IndexEntry();
    entry.genomeName = genome.getGenomeName().trim();
    entry.sequences = genome.getSequenceCount();
    entry.length = genome.getGenomeLength();
    entry.mapperName = mapper.getName().toLowerCase().trim();

    final Map<String, String> md5Map = createMD5SumMap(mapper, genome, additionalDescription);
    final String md5Sum = createMD5Sum(md5Map, this.logger);
    if (md5Sum == null) {
      return null;
    }

    entry.genomeMD5 = md5Sum;
    entry.fileName = entry.mapperName + "-" + entry.genomeMD5 + ".zip";
    entry.description = md5Map.toString();

    return entry;
  }

  private static Map<String, String> createMD5SumMap(
      final MapperInstance mapperInstance,
      final GenomeDescription genome,
      final Map<String, String> additionalDescription) {

    final LinkedHashMap<String, String> map = new LinkedHashMap<>();

    map.put("mapper.name", nullToEmpty(mapperInstance.getName()));
    map.put("mapper.version", nullToEmpty(mapperInstance.getVersion()).trim());
    map.put("mapper.flavor", nullToEmpty(mapperInstance.getFlavor()).trim());
    map.put("genome.md5sum", nullToEmpty(genome.getMD5Sum()).trim());

    // Add sorted additional description
    map.putAll(new TreeMap<>(additionalDescription));

    return map;
  }

  private static String createMD5Sum(final Map<String, String> map, final GenericLogger logger) {

    MessageDigest md5Digest;
    try {
      md5Digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      logger.warn("Failled to create checksum for mapper index: " + e.getMessage());
      return null;
    }

    for (Map.Entry<String, String> e : map.entrySet()) {

      md5Digest.update(e.getKey().getBytes(Charset.defaultCharset()));
      md5Digest.update(e.getValue().getBytes(Charset.defaultCharset()));
    }

    return StringUtils.md5DigestToString(md5Digest);
  }

  //
  // Index management methods
  //

  /**
   * Load the information from the index file
   *
   * @throws IOException if an error occurs while loading the index file
   */
  private void load() throws IOException {

    if (!this.dir.exists()) {
      throw new IOException("Genome index storage directory not found: " + this.dir.getSource());
    }

    final DataPath indexFile = newDataPath(this.dir, INDEX_FILENAME);

    // Create an empty index file if no index exists
    if (!indexFile.exists()) {

      save();
      return;
    }

    // Clear the entries (useful when reloading the index)
    this.entries.clear();

    try (final BufferedReader br =
        new BufferedReader(new InputStreamReader(indexFile.open(), Charset.defaultCharset()))) {

      final Pattern pattern = Pattern.compile("\t");
      String line = null;

      while ((line = br.readLine()) != null) {

        final String trimmedLine = line.trim();
        if ("".equals(trimmedLine) || trimmedLine.startsWith("#")) {
          continue;
        }

        final List<String> fields = Arrays.asList(pattern.split(trimmedLine));

        if (fields.size() < 6 || fields.size() > 7) {
          continue;
        }

        final IndexEntry e = new IndexEntry();
        e.genomeName = fields.get(0);
        e.genomeMD5 = fields.get(1);
        e.sequences = parseSequenceField(fields.get(2));
        e.length = parseLengthField(fields.get(3));
        e.mapperName = fields.get(4);
        e.fileName = fields.get(5);

        if (newDataPath(this.dir, e.fileName).exists()) {
          this.entries.put(e.getKey(), e);
        }

        if (fields.size() == 7) {
          e.description = fields.get(6);
        }
      }
    }
  }

  /**
   * Save the information in the index file
   *
   * @throws IOException if an error occurs while saving the index file
   */
  private void save() throws IOException {

    if (!this.dir.exists()) {
      throw new IOException("Genome index storage directory not found: " + this.dir.getSource());
    }

    final DataPath indexFile = newDataPath(this.dir, INDEX_FILENAME);

    // Create an empty index file
    try (final BufferedWriter writer =
        new BufferedWriter(new OutputStreamWriter(indexFile.create(), Charset.defaultCharset()))) {
      writer.write(
          "#Genome\tChecksum\tGenomeSequences\tGenomeLength\tMapper\tIndexFile\tDescription\n");

      for (Map.Entry<String, IndexEntry> e : this.entries.entrySet()) {

        IndexEntry ie = e.getValue();

        writer.append(ie.genomeName == null ? "???" : ie.genomeName);
        writer.append("\t");
        writer.append(ie.genomeMD5);
        writer.append("\t");
        writer.append(Integer.toString(ie.sequences));
        writer.append("\t");
        writer.append(Long.toString(ie.length));
        writer.append("\t");
        writer.append(ie.mapperName);
        writer.append("\t");
        writer.append(ie.fileName);

        if (ie.description != null) {
          writer.append("\t");
          writer.append(ie.description);
        }

        writer.append("\n");
      }
    }
  }

  //
  // Other methods
  //

  private static String createKey(
      final MapperInstance mapperInstance,
      final GenomeDescription genome,
      final Map<String, String> additionalDescription,
      final GenericLogger logger) {

    return createKey(
        mapperInstance.getName(),
        createMD5Sum(createMD5SumMap(mapperInstance, genome, additionalDescription), logger));
  }

  private static String createKey(final String mapperName, final String genomeMD5) {

    return mapperName.toLowerCase().trim() + '\t' + genomeMD5;
  }

  private static int parseSequenceField(String s) {

    if (s == null) {
      return 0;
    }

    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private static long parseLengthField(String s) {

    if (s == null) {
      return 0;
    }

    try {
      return Long.parseLong(s);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  //
  // Constructor
  //

  /**
   * Private constructor.
   *
   * @param dir Path to the index storage
   * @param logger the logger
   * @throws IOException if an error occurs while testing the index storage
   */
  protected AbstractFileGenomeIndexStorage(final DataPath dir, final GenericLogger logger)
      throws IOException {

    requireNonNull(dir, "Index directory is null");

    this.dir = dir;
    this.logger = logger != null ? logger : new DummyLogger();
    load();

    this.logger.info(
        "Genome index storage found." + this.entries.size() + " entries in : " + dir.getSource());
  }
}
