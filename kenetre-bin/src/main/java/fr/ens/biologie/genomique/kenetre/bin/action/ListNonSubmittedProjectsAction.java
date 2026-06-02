package fr.ens.biologie.genomique.kenetre.bin.action;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.commands.ListNonSubmittedProjectsCommand;
import java.util.List;
import java.util.Map;

public class ListNonSubmittedProjectsAction implements Action {

  @Override
  public String getName() {
    return "list-non-submitted-projects";
  }

  @Override
  public String getDescription() {
    return "Lists non submitted projects at GenomiqueENS";
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public void action(Map<String, String> conf, List<String> arguments) throws KenetreException {

    new ListNonSubmittedProjectsCommand(this.getName(), conf, arguments).execute();
  }
}
