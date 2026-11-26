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
 * Auto-submit all non-submitted terminated projects. *
 *
 * @author Laurent Jourdren
 * @since 0.41
 */
public class AutoSubmitProjectsCommand extends AbstractCommand {

  @Override
  protected String syntax() {
    return "";
  }

  @Override
  protected void internalExecute(List<String> arguments) throws KenetreException {

    try {
      autoSubmitProjects(this.force);
    } catch (IOException e) {
      throw new KenetreException(e);
    }
  }

  /**
   * Auto-submit all non-submitted terminated projects.
   *
   * @param force If true, force submission even if the project is already submitted; otherwise,
   *     submit only non-submitted projects.
   * @throws IOException If an I/O error occurs while accessing the database.
   */
  private void autoSubmitProjects(boolean force) throws IOException, KenetreException {

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

    for (String p : toSubmit) {
      SubmitProjectCommand.submitProject(
          this.fgApiClient, this.gensApiClient, this.newSubmissionBuilder(), p, force);
    }
  }

  //
  // Constructor
  //

  /**
   * Constructor.
   *
   * @param actionName The name of the action to execute (e.g., "auto-submit-projects").
   * @param conf A map of configuration parameters (e.g., database connection settings).
   * @param arguments A list of command-line arguments passed to the command.
   * @throws KenetreException If an error occurs while initializing the command (e.g., invalid
   *     configuration).
   */
  public AutoSubmitProjectsCommand(
      String actionName, Map<String, String> conf, List<String> arguments) throws KenetreException {

    super(actionName, conf, arguments, true);
  }
}
