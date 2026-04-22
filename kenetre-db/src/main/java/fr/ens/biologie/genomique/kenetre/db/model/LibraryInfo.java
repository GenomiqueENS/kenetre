package fr.ens.biologie.genomique.kenetre.db.model;

import java.util.List;

/**
 * Holds the raw data retrieved from the database for a library batch.
 *
 * @since 0.41
 * @author Laurent Jourdren
 */
public record LibraryInfo(
    String libraryBatchId,
    Object libraryFirstMadeCount,
    Object librarySequencedCount,
    String protocol,
    String fgApplication,
    String receiptDate,
    String qcDate,
    List<String> runs) {}
