package fr.ens.biologie.genomique.kenetre.bin.action;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.commands.SubmitProjectCommand;
import java.util.List;
import java.util.Map;

public class SubmitProjectAction implements Action {

  @Override
  public String getName() {
    return "submit-project";
  }

  @Override
  public String getDescription() {
    return "Submits a project to the France Génomique SI";
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public void action(Map<String, String> conf, List<String> arguments) throws KenetreException {

    new SubmitProjectCommand(this.getName(), conf, arguments).execute();
  }
}
