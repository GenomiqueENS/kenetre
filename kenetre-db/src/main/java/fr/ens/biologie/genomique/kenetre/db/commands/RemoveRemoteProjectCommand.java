package fr.ens.biologie.genomique.kenetre.db.commands;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Remove a remote project from the database.
 *
 * @author Laurent Jourdren
 * @since 0.41
 */
public class RemoveRemoteProjectCommand extends AbstractCommand {

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
      removeProject(arguments.get(0));
    } catch (IOException e) {
      throw new KenetreException(e.getMessage());
    }
  }

  /** Remove a remote project. */
  private void removeProject(String projectName) throws IOException {
    if (this.fgApiClient.isProjectExists(projectName)) {
      this.fgApiClient.removeProject(projectName);
    } else {
      throw new IllegalStateException("Project \"" + projectName + "\" does not exist");
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
   * @throws KenetreException If an error occurs while initializing the command (e.g., invalid
   *     configuration).
   */
  public RemoveRemoteProjectCommand(String actionName, Map<String, String> conf)
      throws KenetreException {

    super(actionName, conf, true, true);
  }
}
