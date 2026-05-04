package fr.ens.biologie.genomique.kenetre.bin.action;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.commands.RemoveRemoteProjectCommand;
import java.util.List;
import java.util.Map;

public class RemoveRemoteProjectAction implements Action {

  @Override
  public String getName() {
    return "remove-remote-project";
  }

  @Override
  public String getDescription() {
    return "Remove a remote project at France Génomique SI";
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public void action(Map<String, String> conf, List<String> arguments) throws KenetreException {

    new RemoveRemoteProjectCommand(this.getName(), conf, arguments).execute();
  }
}
