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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface DataPath {

  /**
   * Check if this DataFile exists.
   *
   * @return true if this DataFile exists
   */
  boolean exists();

  /**
   * Get the source of this DataFile.
   *
   * @return a String with the source of this DataFile
   */
  String getSource();

  /**
   * Create an InputStream for the DataFile. If the DataFile is compressed, the input stream will be
   * automatically uncompress.
   *
   * @return an InputStream object
   * @throws IOException if an error occurs while opening the DataFile
   */
  InputStream open() throws IOException;

  /**
   * Create an InputStream for the DataFile. The input stream will not automatically uncompress
   * data.
   *
   * @return an InputStream object
   * @throws IOException if an error occurs while opening the DataFile
   */
  InputStream rawOpen() throws IOException;

  /**
   * Create an OutputStream for the DataFile. If the DataFile is declared as compressed by its
   * content type or its extension, the output stream will be automatically compress data.
   *
   * @return an OutputStream object
   * @throws IOException if an error occurs while creating the DataFile
   */
  OutputStream create() throws IOException;

  /**
   * Create an OutputStream for the DataFile. The output stream will not automatically compress
   * data.
   *
   * @return an OutputStream object
   * @throws IOException if an error occurs while creating the DataFile
   */
  OutputStream rawCreate() throws IOException;

  /**
   * Get the name of this DataFile.
   *
   * @return a String with the name of this DataFile
   */
  String getName();

  /**
   * Get the content length of the file.
   *
   * @return the content length or -1 if unavailable
   */
  long getContentLength();

  /**
   * Get the date of the last modification of the file.
   *
   * @return the last modified date in seconds since epoch of -1 if unavailable
   */
  long getLastModified();

  /**
   * Canonicalize symbolic link.
   *
   * @return a canonicalized file
   * @throws IOException if an error occurs while canonicalize the file
   */
  DataPath canonicalize() throws IOException;

  /**
   * List the content of a directory.
   *
   * @return a List with the content of the directory
   * @throws IOException if an error occurs while listing the directory
   */
  List<DataPath> list() throws IOException;

  /**
   * Convert the DataPah object to File object if the underlying protocol allow it. Only local
   * protocol can return a value.
   *
   * @return a File object or null if the underlying protocol does not allow it
   */
  File toFile();

  /**
   * Copy a file, if input data is compressed, data will be uncompressed and if output require to be
   * compressed output will be compressed.
   *
   * @param dest destination file.
   * @throws IOException if an error occurs while copying data
   */
  void copy(DataPath dest) throws IOException;

  /**
   * Create a symbolic link if the input and output use the same protocol and if symbolic links are
   * supported by the protocol. If symbolic link cannot be created, the input file will be copied.
   *
   * @param output output file
   * @throws IOException if an error occurs while copying data or creating the symbolic link
   */
  void symlinkOrCopy(DataPath output) throws IOException;
}
