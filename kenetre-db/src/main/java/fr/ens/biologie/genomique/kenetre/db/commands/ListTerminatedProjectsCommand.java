package fr.ens.biologie.genomique.kenetre.db.commands;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.ProjectQuery;
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
    for (String p :
        ProjectQuery.getProjects(
            this.gensApiClient, "Terminé", null, this.sinceYear, false, null)) {
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

  public ListTerminatedProjectsCommand(String actionName, Map<String, String> conf)
      throws KenetreException {

    super(actionName, conf, true, false);
  }
}
