package fr.ens.biologie.genomique.kenetre.db.model;

/**
 * Holds the raw data retrieved from the database for a sequencing run.
 *
 * @since 0.41
 * @author Laurent Jourdren
 */
public record RunInfo(
    String runId,
    String runDate,
    String qcDate,
    String problem,
    String flowcell,
    String flowCellFg,
    String supplier,
    String fgInstrument) {}
