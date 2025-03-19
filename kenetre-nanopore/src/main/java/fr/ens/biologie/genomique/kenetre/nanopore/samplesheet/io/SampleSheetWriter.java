package fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.io;

import fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet;
import java.io.IOException;

/**
 * This interface define a writer for Nanopore software sample sheets.
 *
 * @since 0.20
 * @author Laurent Jourdren
 */
public interface SampleSheetWriter extends AutoCloseable {

  /**
   * Write a sample sheet.
   *
   * @param samplesheet sample sheet to write
   * @throws IOException if an error occurs while writing the samplesheet
   */
  void writer(SampleSheet sampleSheet) throws IOException;

  @Override
  void close() throws IOException;
}
