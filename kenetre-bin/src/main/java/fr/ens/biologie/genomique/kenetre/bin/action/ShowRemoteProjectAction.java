package fr.ens.biologie.genomique.kenetre.bin.action;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.commands.ShowRemoteProjectCommand;
import java.util.List;
import java.util.Map;

public class ShowRemoteProjectAction implements Action {

  @Override
  public String getName() {
    return "show-remote-project";
  }

  @Override
  public String getDescription() {
    return "Shows a remote project at France Génomique SI";
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public void action(Map<String, String> conf, List<String> arguments) throws KenetreException {

    new ShowRemoteProjectCommand(this.getName(), conf).execute(arguments);
  }
}
