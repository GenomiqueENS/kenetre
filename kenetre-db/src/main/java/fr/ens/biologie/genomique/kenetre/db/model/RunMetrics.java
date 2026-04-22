package fr.ens.biologie.genomique.kenetre.db.model;

/**
 * Holds the raw data retrieved from the database for a sequencing run metrics.
 *
 * @since 0.41
 * @author Laurent Jourdren
 */
public record RunMetrics(
    int cycleCount,
    boolean pairedEnd,
    String model,
    String fgModel,
    long readCount,
    long baseCount,
    boolean success) {}
