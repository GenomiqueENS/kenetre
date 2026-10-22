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

package fr.ens.biologie.genomique.kenetre.db.commands;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.GenomiqueEnsProjectQuery;
import java.io.IOException;
import java.util.*;

/**
 * List non-submitted terminated projects.
 *
 * @author Laurent Jourdren
 * @since 0.41
 */
public class ListNonSubmittedProjectsCommand extends AbstractCommand {

  @Override
  protected String syntax() {
    return "";
  }

  @Override
  protected void internalExecute(List<String> arguments) throws KenetreException {

    try {
      listNonSubmittedProjects(this.quietMode);
    } catch (IOException e) {
      throw new KenetreException(e);
    }
  }

  /**
   * List non-submitted terminated projects.
   *
   * @param quiet If true, print only project names; otherwise, print a header and a list of
   *     projects.
   */
  private void listNonSubmittedProjects(boolean quiet) throws IOException {

    Set<String> terminatedProjects =
        new HashSet<>(
            GenomiqueEnsProjectQuery.getTerminatedProjectsSinceYear(
                this.gensApiClient, this.sinceYear));
    Set<String> submittedProjects = new HashSet<>();
    for (String[] p : this.fgApiClient.listProjects()) {
      submittedProjects.add(p[0]);
    }

    Set<String> toSubmit = new TreeSet<>(terminatedProjects);
    toSubmit.removeAll(submittedProjects);

    if (!quiet) {
      System.out.println("Non-submitted projects:");
    }

    for (String p : toSubmit) {
      if (quiet) {
        System.out.println(p);
      } else {
        System.out.println(" - " + p);
      }
    }
  }

  //
  // Constructor
  //

  /**
   * Constructor.
   *
   * @param actionName The name of the action to execute (e.g., "list-non-submitted-projects").
   * @param conf A map of configuration parameters (e.g., database connection settings).
   * @param arguments The list of arguments passed to the command.
   * @throws KenetreException If an error occurs while initializing the command (e.g., invalid
   *     configuration).
   */
  public ListNonSubmittedProjectsCommand(
      String actionName, Map<String, String> conf, List<String> arguments) throws KenetreException {

    super(actionName, conf, arguments, true);
  }
}
