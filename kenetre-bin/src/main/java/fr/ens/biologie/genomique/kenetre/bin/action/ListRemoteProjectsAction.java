package fr.ens.biologie.genomique.kenetre.bin.action;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.commands.ListRemoteProjectsCommand;
import java.util.List;
import java.util.Map;

public class ListRemoteProjectsAction implements Action {

  @Override
  public String getName() {
    return "list-remote-projects";
  }

  @Override
  public String getDescription() {
    return "Lists GenomiqueENS remote projects at France Génomique SI";
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public void action(Map<String, String> conf, List<String> arguments) throws KenetreException {

    new ListRemoteProjectsCommand(this.getName(), conf).execute(arguments);
  }
}
