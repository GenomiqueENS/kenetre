package fr.ens.biologie.genomique.kenetre.db.commands;

import static fr.ens.biologie.genomique.kenetre.db.fg.FgApiClient.removeJsonIntegerKeys;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.ens.biologie.genomique.kenetre.KenetreException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Show remote project details as JSON.
 *
 * @author Laurent Jourdren
 * @since 0.41
 */
public class ShowRemoteProjectCommand extends AbstractCommand {

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
      showRemoteProject(arguments.get(0));
    } catch (IOException e) {
      throw new KenetreException(e.getMessage());
    }
  }

  /** Show remote project details as JSON. */
  private void showRemoteProject(String projectName) throws IOException {
    JsonObject projectDict = this.fgApiClient.getProject(projectName);
    removeJsonIntegerKeys(projectDict);

    List<JsonObject> manips = this.fgApiClient.getManipsProject(projectName);
    JsonArray manipsArray = new JsonArray();
    for (JsonObject m : manips) {
      removeJsonIntegerKeys(m);
      manipsArray.add(m);
    }
    projectDict.add("manips", manipsArray);

    System.out.println(this.gson.toJson(projectDict));
  }

  //
  // Constructor
  //

  /**
   * Constructor.
   *
   * @param actionName The name of the action to execute (e.g., "list-remote-projects").
   * @param conf The configuration map containing necessary parameters for the command.
   * @param arguments The list of arguments passed to the command.
   * @throws KenetreException If an error occurs while initializing the command (e.g., invalid
   *     configuration).
   */
  public ShowRemoteProjectCommand(
      String actionName, Map<String, String> conf, List<String> arguments) throws KenetreException {

    super(actionName, conf, arguments, true);
  }
}
