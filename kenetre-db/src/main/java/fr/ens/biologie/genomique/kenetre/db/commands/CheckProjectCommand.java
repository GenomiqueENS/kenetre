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
import fr.ens.biologie.genomique.kenetre.db.fg.FgProjectSubmissionBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Check a project's data and print as JSON.
 *
 * @author Laurent Jourdren
 * @since 0.41
 */
public class CheckProjectCommand extends AbstractCommand {

  @Override
  protected String syntax() {
    return "<project_name>";
  }

  @Override
  protected void internalExecute(List<String> arguments) throws KenetreException {

    if (arguments.size() != 1) {
      System.err.println("ERROR: Invalid number of arguments. Expected 1, got " + arguments.size());
      System.exit(1);
    }

    try {
      checkProject(arguments.get(0));
    } catch (IOException e) {
      e.printStackTrace();
      throw new KenetreException(e.getMessage());
    }
  }

  /**
   * Check a project's data and print as JSON.
   *
   * @param projectName The name of the project to check.
   * @throws IOException If an I/O error occurs while accessing the database.
   */
  private void checkProject(String projectName) throws IOException, KenetreException {

    FgProjectSubmissionBuilder.SubmissionData data =
        newSubmissionBuilder().createSubmissionData(this.gensApiClient, projectName);

    System.out.println();
    System.out.println(this.gson.toJson(data.projectDict()));
    for (Map<String, Object> m : data.manips()) {
      System.out.println("\n" + this.gson.toJson(m));
    }
  }

  //
  // Constructor
  //

  /**
   * Create a new CheckProjectCommand.
   *
   * @param actionName The name of the action to execute (e.g., "check-project").
   * @param conf The configuration map containing necessary parameters for the command.
   * @param arguments A list of command-line arguments passed to the command.
   * @throws KenetreException If an error occurs while initializing the command (e.g., invalid
   *     configuration).
   */
  public CheckProjectCommand(String actionName, Map<String, String> conf, List<String> arguments)
      throws KenetreException {

    super(actionName, conf, arguments, true);
  }
}
