package fr.ens.biologie.genomique.kenetre.bin.action;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.commands.ListCurrentProjectsCommand;
import java.util.List;
import java.util.Map;

public class ListCurrentProjectsAction implements Action {

  @Override
  public String getName() {
    return "list-current-projects";
  }

  @Override
  public String getDescription() {
    return "Lists current projects at GenomiqueENS";
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public void action(Map<String, String> conf, List<String> arguments) throws KenetreException {

    new ListCurrentProjectsCommand(this.getName(), conf).execute(arguments);
  }
}
