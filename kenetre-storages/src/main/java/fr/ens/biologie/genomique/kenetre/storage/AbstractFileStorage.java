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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class define a storage for files.
 *
 * @since 2.6
 * @author Laurent Jourdren
 */
public abstract class AbstractFileStorage {

  private final DataPath rootPath;

  private final List<String> extensions;

  /**
   * Get the list of the file extensions of the files to search.
   *
   * @return a list with file extensions of the files to search
   */
  protected List<String> getExtensions() {

    return Collections.unmodifiableList(this.extensions);
  }

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
   * Get the underlying Data.
   *
   * @param shortName short name of the source to use
   * @return null if not file found
   * @throws IOException if an error occurs while getting the underlying DataFile
   */
  public File getFile(final String shortName) throws IOException {

    DataPath result = getDataPath(shortName);

    return result != null ? result.toFile() : null;
  }

  /**
   * Get the underlying Data.
   *
   * @param shortName short name of the source to use
   * @return null if not file found
   * @throws IOException if an error occurs while getting the underlying DataFile
   */
  protected DataPath getDataPath(final String shortName) throws IOException {

    requireNonNull(shortName);

    if (!this.rootPath.exists()) {
      throw new IOException("Storage base path does not exists: " + this.rootPath.getSource());
    }

    // List the content of the directory if it can be listed
    final List<DataPath> dirList = this.rootPath.list();

    // Add no extension to the list of allowed extensions
    final List<String> extensions = new ArrayList<>(getExtensions());
    extensions.add("");

    for (String extension : extensions) {

      if (extension == null) {
        throw new NullPointerException("One of the extensions of the storage protocol is null");
      }

      final String filename = shortName.trim() + extension;

      for (CompressionType c : CompressionType.values()) {

        final DataPath file = newDataPath(this.rootPath, filename + c.getExtension());

        // Check if the file in the right case exists
        if (file.exists()) {
          return file.canonicalize();
        }

        // Insensitive case search file in the directory list
        if (!dirList.isEmpty()) {

          // Compare filename in lower case
          final String filenameLower = file.getName().toLowerCase();
          for (DataPath f : dirList) {

            if (f.getName().toLowerCase().equals(filenameLower) && f.exists()) {
              return f.canonicalize();
            }
          }
        } else {

          // For non browsable base directory

          // Check if the directory exists in lower case
          final DataPath fileLower = newDataPath(this.rootPath, file.getName().toLowerCase());
          if (fileLower.exists()) {
            return fileLower.canonicalize();
          }

          // Check if the directory exists in upper case
          final DataPath fileUpper = newDataPath(this.rootPath, file.getName().toUpperCase());
          if (fileUpper.exists()) {
            return fileUpper.canonicalize();
          }
        }
      }
    }

    return null;
  }

  //
  // Constructor
  //

  /**
   * Constructor.
   *
   * @param rootPath root of the storage
   * @param extensions extension of the files
   */
  protected AbstractFileStorage(String rootPath, List<String> extensions) {

    requireNonNull(rootPath);
    requireNonNull(extensions);

    if (extensions.isEmpty()) {
      throw new IllegalArgumentException("extension list cannot be empty");
    }

    this.rootPath = newDataPath(rootPath);
    this.extensions = extensions;
  }
}
