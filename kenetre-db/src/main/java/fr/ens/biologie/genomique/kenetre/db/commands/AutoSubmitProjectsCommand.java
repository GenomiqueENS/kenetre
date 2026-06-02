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
            GenomiqueEnsProjectQuery.getProjects(
                this.gensApiClient, "Terminé", null, this.sinceYear, false, true));
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
