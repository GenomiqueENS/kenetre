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
 * List terminated projects from the database.
 *
 * @author Laurent Jourdren
 * @since 0.41
 */
public class ListTerminatedProjectsCommand extends AbstractCommand {

  @Override
  protected String syntax() {
    return "";
  }

  @Override
  protected void internalExecute(List<String> arguments) throws KenetreException {

    try {
      listTerminatedProjects(this.quietMode);
    } catch (IOException e) {
      throw new KenetreException(e);
    }
  }

  /** List terminated projects from the database. */
  public void listTerminatedProjects(boolean quiet) throws IOException {
    if (!quiet) {
      System.out.println("Terminated projects:");
    }
    for (String p : GenomiqueEnsProjectQuery.getTerminatedProjects(this.gensApiClient)) {
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
   * @param actionName The name of the action to execute (e.g., "list-terminated-projects").
   * @param conf The configuration map containing necessary parameters for the command.
   * @param arguments The list of arguments provided to the command.
   * @throws KenetreException If an error occurs while initializing the command (e.g., invalid
   *     configuration).
   */
  public ListTerminatedProjectsCommand(
      String actionName, Map<String, String> conf, List<String> arguments) throws KenetreException {

    super(actionName, conf, arguments, false);
  }
}
