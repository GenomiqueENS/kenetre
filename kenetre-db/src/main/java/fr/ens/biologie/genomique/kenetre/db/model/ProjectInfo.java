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
    boolean autonomousProject,
    String analysisType) {}
