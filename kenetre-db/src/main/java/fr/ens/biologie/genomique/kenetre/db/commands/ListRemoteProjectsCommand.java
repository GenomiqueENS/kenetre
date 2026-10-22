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
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * List remote (submitted) projects.
 *
 * @author Laurent Jourdren
 * @since 0.41
 */
public class ListRemoteProjectsCommand extends AbstractCommand {

  @Override
  protected String syntax() {
    return "";
  }

  @Override
  protected void internalExecute(List<String> arguments) throws KenetreException {

    try {
      listRemoteProjects(this.quietMode);
    } catch (IOException e) {
      throw new KenetreException(e);
    }
  }

  /** List remote (submitted) projects. */
  private void listRemoteProjects(boolean quiet) throws IOException {
    if (!quiet) {
      System.out.println("Submitted projects:");
    }
    for (String[] p : this.fgApiClient.listProjects()) {
      if (quiet) {
        System.out.println(p[0]);
      } else {
        System.out.println(" - " + p[0] + " (key: " + p[1] + ")");
      }
    }
  }

  //
  // Constructor
  //

  /**
   * Constructor.
   *
   * @param actionName The name of the action to execute (e.g., "list-remote-projects").
   * @param conf The configuration map containing necessary parameters for the command.
   * @param arguments A list of command-line arguments passed to the command.
   * @throws KenetreException If an error occurs while initializing the command (e.g., invalid
   *     configuration).
   */
  public ListRemoteProjectsCommand(
      String actionName, Map<String, String> conf, List<String> arguments) throws KenetreException {

    super(actionName, conf, arguments, true);
  }
}
