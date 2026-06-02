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
