package fr.ens.biologie.genomique.kenetre.db.model;

import java.util.List;

/**
 * Holds the raw data retrieved from the database for a project.
 *
 * @since 0.41
 * @author Laurent Jourdren
 */
public record ProjectInfo(
    // Map<String, Object> projectDict,
    List<String> libraryBatchIds,
    List<String> runIds,
    List<String> species,
    List<Integer> taxons,
    String acronym,
    boolean rd,
    String resultSendDate,
    String firstName,
    String lastName,
    String email,
    String employer,
    String status,
    Integer endYear,
    String labIndicator,
    String city,
    String zipCode,
    String labNationalNumber,
    String labName,
    boolean libraryBuilding,
    boolean librarySequencing,
    String analysisType) {}
