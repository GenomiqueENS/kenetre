package fr.ens.biologie.genomique.kenetre.db;

import fr.ens.biologie.genomique.kenetre.db.model.ProjectInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Provides methods for querying projects from the database views.
 *
 * @since 0.41
 * @author Laurent Jourdren
 */
public final class GenomiqueEnsProjectQuery {

  /**
   * Get acronyms of non-internal terminated projects for a given year.
   *
   * @param genomiqueEnsApiClient API connection
   * @param year year of project termination
   * @return list of project acronyms
   */
  public static List<String> getNonInternalTerminatedProjects(
      GenomiqueEnsApiClient genomiqueEnsApiClient, int year) throws IOException {
    return getProjects(genomiqueEnsApiClient, "Terminé", year, null, false, null);
  }

  /**
   * Get acronyms of non-internal ongoing projects.
   *
   * @param genomiqueEnsApiClient API connection
   * @return list of project acronyms
   */
  public static List<String> getNonInternalOngoingProjects(
      GenomiqueEnsApiClient genomiqueEnsApiClient) throws IOException {
    return getProjects(genomiqueEnsApiClient, "En cours", null, null, false, null);
  }

  /**
   * Get acronyms of projects filtered by status, year, and internal/external status.
   *
   * @param genomiqueEnsApiClient API connection
   * @param status project status to filter (default: "Terminé")
   * @param year specific year of project end (may be null)
   * @param sinceYear minimum year of project end (may be null)
   * @param internal if true, only internal; if false, only external; if null, all
   * @param libraryBuildingOrSequencing if true, only projects with library building or sequencing;
   *     if null, no filter
   * @return list of project acronyms
   * @throws IOException if an API error occurs
   */
  public static List<String> getProjects(
      // Connection conn,
      GenomiqueEnsApiClient genomiqueEnsApiClient,
      String status,
      Integer year,
      Integer sinceYear,
      Boolean internal,
      Boolean libraryBuildingOrSequencing)
      throws IOException {

    Objects.requireNonNull(genomiqueEnsApiClient);

    var filters = new HashMap<String, String>();
    if (year != null) {
      filters.put("annee", "" + year);
    }
    if (sinceYear != null) {
      filters.put("depuisAnnee", "" + sinceYear);
    }
    if (status != null) {
      filters.put("statut", status);
    }

    List<String> result = new ArrayList<>();
    for (ProjectInfo project : genomiqueEnsApiClient.fetchProjectInfos(filters)) {

      if (status != null && !status.equals(project.status())) {
        continue;
      }

      if (year != null && (project.endYear() == null || project.endYear() != year)) {
        continue;
      }

      if (sinceYear != null && (project.endYear() == null || project.endYear() < sinceYear)) {
        continue;
      }

      if (internal == Boolean.TRUE && !"GenomiqueENS".equals(project.labName())) {
        continue;
      }

      if (internal == Boolean.FALSE && "GenomiqueENS".equals(project.labName())) {
        continue;
      }

      if (libraryBuildingOrSequencing == Boolean.TRUE
          && !(project.libraryBuilding() || project.librarySequencing())) {
        continue;
      }

      if (libraryBuildingOrSequencing == Boolean.FALSE
          && (project.libraryBuilding() || project.librarySequencing())) {
        continue;
      }

      result.add(project.acronym());
    }

    return result;
  }
}
