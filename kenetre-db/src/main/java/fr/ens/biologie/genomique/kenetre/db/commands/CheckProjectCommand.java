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
