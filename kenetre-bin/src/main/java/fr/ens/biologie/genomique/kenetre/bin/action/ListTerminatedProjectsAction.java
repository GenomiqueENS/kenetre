package fr.ens.biologie.genomique.kenetre.bin.action;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.commands.ListTerminatedProjectsCommand;
import java.util.List;
import java.util.Map;

public class ListTerminatedProjectsAction implements Action {

  @Override
  public String getName() {
    return "list-terminated-projects";
  }

  @Override
  public String getDescription() {
    return "List terminated projects at GenomiqueENS";
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public void action(Map<String, String> conf, List<String> arguments) throws KenetreException {

    new ListTerminatedProjectsCommand(this.getName(), conf).execute(arguments);
  }
}
