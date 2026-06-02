package fr.ens.biologie.genomique.kenetre.bin.action;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.commands.CheckProjectCommand;
import java.util.List;
import java.util.Map;

public class CheckProjectAction implements Action {

  @Override
  public String getName() {
    return "check-project";
  }

  @Override
  public String getDescription() {
    return "Generate JSON reporting data for a project for submission to France Génomique.";
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public void action(Map<String, String> conf, List<String> arguments) throws KenetreException {

    new CheckProjectCommand(getName(), conf, arguments).execute();
  }
}
