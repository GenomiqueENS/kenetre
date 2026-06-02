package fr.ens.biologie.genomique.kenetre.db.commands;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.GenomiqueEnsApiClient;
import fr.ens.biologie.genomique.kenetre.db.fg.FgApiClient;
import fr.ens.biologie.genomique.kenetre.db.fg.FgProjectSubmissionBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Show remote project details as JSON.
 *
 * @author Laurent Jourdren
 * @since 0.41
 */
public class SubmitProjectCommand extends AbstractCommand {

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
      submitProject(
          this.fgApiClient,
          this.gensApiClient,
          newSubmissionBuilder(),
          arguments.get(0),
          this.force);
    } catch (IOException e) {
      throw new KenetreException(e);
    }
  }

  /** Submit a project. */
  static void submitProject(
      FgApiClient fgApiClient,
      GenomiqueEnsApiClient gensApiClient,
      FgProjectSubmissionBuilder submissionBuilder,
      String projectName,
      boolean force)
      throws IOException {

    if (fgApiClient.isProjectExists(projectName)) {
      if (force) {
        fgApiClient.removeProject(projectName);
      } else {
        throw new IllegalStateException(
            "Project \"" + projectName + "\" has already been submitted.");
      }
    }

    FgProjectSubmissionBuilder.SubmissionData data =
        submissionBuilder.createSubmissionData(gensApiClient, projectName);

    fgApiClient.addProject(data.projectDict());

    String cleFg = fgApiClient.getCleFgProject(projectName);
    for (Map<String, Object> m : data.manips()) {
      m.put("cle_projet_fg", cleFg);
      fgApiClient.addManip(m);
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
   * @param arguments The list of command-line arguments passed to the command.
   * @throws KenetreException If an error occurs while initializing the command (e.g., invalid
   *     configuration).
   */
  public SubmitProjectCommand(String actionName, Map<String, String> conf, List<String> arguments)
      throws KenetreException {

    super(actionName, conf, arguments, true);
  }
}
