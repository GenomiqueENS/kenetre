package fr.ens.biologie.genomique.kenetre.db.commands;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.ProjectQuery;
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
    for (String p :
        ProjectQuery.getProjects(this.gensApiClient, "En cours", null, null, null, null)) {
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
