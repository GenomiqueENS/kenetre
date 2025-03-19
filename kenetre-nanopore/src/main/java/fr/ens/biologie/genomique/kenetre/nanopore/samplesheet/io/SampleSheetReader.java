package fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.io;

import fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet;
import java.io.IOException;
import java.util.Collection;

/**
 * This interface define a reader for Nanopore software sample sheets.
 *
 * @since 0.20
 * @author Laurent Jourdren
 */
public interface SampleSheetReader extends AutoCloseable {

  /**
   * Allow to accept any field.
   *
   * @param allow the value to set
   */
  void allowAnyField(boolean allow);

  /**
   * Allow additional field name.
   *
   * @param fieldName field name to allow
   */
  void addAllowedField(String fieldName);

  /**
   * Allow additional field names.
   *
   * @param fieldNames field names to allow
   */
  void addAllowedFields(Collection<String> fieldNames);

  /**
   * Allow additional field names.
   *
   * @param fieldNames field names to allow
   */
  void addAllowedFields(String... fieldNames);

  /**
   * Read a sample sheet.
   *
   * @return a SampleSheet object
   * @throws IOException if an error occurs while reading the sample sheet
   */
  SampleSheet read() throws IOException;

  @Override
  void close() throws IOException;
}
