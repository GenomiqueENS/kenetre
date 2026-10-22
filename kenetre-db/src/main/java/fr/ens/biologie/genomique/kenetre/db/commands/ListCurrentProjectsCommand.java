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
import java.util.List;
import java.util.Map;

/**
 * List current (ongoing) projects from the database.
 *
 * @author Laurent Jourdren
 * @since 0.41
 */
public class ListCurrentProjectsCommand extends AbstractCommand {

  @Override
  protected String syntax() {
    return "";
  }

  @Override
  protected void internalExecute(List<String> arguments) throws KenetreException {

    try {
      listCurrentProjects(this.quietMode);
    } catch (IOException e) {
      throw new KenetreException(e);
    }
  }

  /**
   * List current (ongoing) projects from the database.
   *
   * @param quiet If true, print only project names; otherwise, print a header and a list of
   *     projects.
   * @throws IOException If an I/O error occurs while accessing the database.
   */
  private void listCurrentProjects(boolean quiet) throws IOException {
    if (!quiet) {
      System.out.println("Current projects:");
    }
    for (String p : GenomiqueEnsProjectQuery.getCurrentProjects(this.gensApiClient)) {
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
   * Create a new ListCurrentProjectsCommand.
   *
   * @param actionName The name of the action to execute (e.g., "list-current-projects").
   * @param conf A map of configuration parameters (e.g., database connection settings).
   * @param arguments A list of command-line arguments passed to the command.
   * @throws KenetreException If an error occurs while initializing the command (e.g., invalid
   *     configuration).
   */
  public ListCurrentProjectsCommand(
      String actionName, Map<String, String> conf, List<String> arguments) throws KenetreException {

    super(actionName, conf, arguments, false);
  }
}
