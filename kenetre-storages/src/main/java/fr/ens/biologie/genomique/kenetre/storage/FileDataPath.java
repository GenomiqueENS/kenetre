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

import fr.ens.biologie.genomique.kenetre.io.CompressionType;
import fr.ens.biologie.genomique.kenetre.io.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class define a bridge between Kenetre DataPath objects and Java File objects.
 *
 * @since 2.6
 * @author Laurent Jourdren
 */
public class FileDataPath implements DataPath, Comparable<FileDataPath> {

  private final File file;

  @Override
  public String getName() {

    return this.file.getName();
  }

  @Override
  public String getSource() {

    return this.file.getPath();
  }

  @Override
  public boolean exists() {
    return this.file.exists();
  }

  @Override
  public InputStream open() throws IOException {

    CompressionType ct = CompressionType.getCompressionTypeByFilename(this.file.getName());

    return ct.createInputStream(rawOpen());
  }

  @Override
  public InputStream rawOpen() throws IOException {

    return new FileInputStream(this.file);
  }

  @Override
  public OutputStream create() throws IOException {

    CompressionType ct = CompressionType.getCompressionTypeByFilename(this.file.getName());

    return ct.createOutputStream(rawCreate());
  }

  @Override
  public OutputStream rawCreate() throws IOException {

    return new FileOutputStream(this.file);
  }

  @Override
  public long getContentLength() {

    return this.file.length();
  }

  @Override
  public long getLastModified() {

    return this.file.lastModified();
  }

  @Override
  public DataPath canonicalize() throws IOException {

    return new FileDataPath(this.file.getCanonicalPath());
  }

  @Override
  public List<DataPath> list() throws IOException {

    if (!this.file.exists()) {
      throw new FileNotFoundException("File not found: " + this.file);
    }

    if (!this.file.isDirectory()) {
      throw new IOException("The file is not a directory: " + this.file);
    }

    // List directory
    final File[] files = this.file.listFiles();

    if (files == null) {
      return Collections.emptyList();
    }

    // Convert the File array to a list of DataFile
    final List<DataPath> result = new ArrayList<>(files.length);
    for (File f : files) {
      result.add(new FileDataPath(f));
    }

    // Return an unmodifiable list
    return Collections.unmodifiableList(result);
  }

  @Override
  public File toFile() {

    return this.file;
  }

  @Override
  public void copy(DataPath output) throws IOException {

    requireNonNull(output);

    FileUtils.copy(open(), output.create());
  }

  @Override
  public void symlinkOrCopy(DataPath link) throws IOException {

    requireNonNull(link);

    if (link.exists()) {
      throw new IOException("the symlink already exists");
    }

    final Path targetPath = this.file.toPath();
    final Path linkPath = link.toFile().toPath();

    Files.createSymbolicLink(linkPath, targetPath);
  }

  //
  // Object methods
  //

  @Override
  public int compareTo(FileDataPath o) {

    if (o == null) {
      return -1;
    }

    return this.file.compareTo(o.file);
  }

  @Override
  public int hashCode() {

    return this.file.hashCode();
  }

  @Override
  public boolean equals(Object obj) {

    return this.file.equals(obj);
  }

  @Override
  public String toString() {

    return this.file.toString();
  }

  //
  // Constructor
  //

  /**
   * Constructor.
   *
   * @param source source of the file
   */
  public FileDataPath(String source) {

    requireNonNull(source);

    this.file = new File(source);
  }

  /**
   * Constructor.
   *
   * @param file file object to wrap
   */
  public FileDataPath(File file) {

    requireNonNull(file);

    this.file = file;
  }

  /**
   * Constructor.
   *
   * @param parent parent file
   * @param filename filename
   */
  public FileDataPath(DataPath parent, String filename) {

    requireNonNull(parent);
    requireNonNull(filename);

    if (!(parent instanceof FileDataPath)) {
      throw new IllegalArgumentException("parent is not a PathDataPath object");
    }

    FileDataPath p = (FileDataPath) parent;

    this.file = new File(p.file, filename);
  }
}
